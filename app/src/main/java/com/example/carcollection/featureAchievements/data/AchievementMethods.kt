package com.example.carcollection.featureAchievements.data

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.carcollection.featureAchievements.domain.*
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featureNotification.data.NotificationMethods
import com.example.carcollection.featureuser.data.UserMethods
import com.example.carcollection.featureuser.domain.User
import com.example.carcollection.featureuser.domain.XPSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AchievementMethods {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notificationMethods = NotificationMethods()
    private val userMethods = UserMethods()

    private fun userAchievementsCollection() =
        db.collection("users")
            .document(auth.currentUser?.uid ?: "")
            .collection("achievements")

    private fun globalAchievementsCollection() =
        db.collection("achievements")

    // ─────────────────────────────────────────────────────────────
    // OBTENER LOGROS (GLOBAL + USUARIO)
    // ─────────────────────────────────────────────────────────────

    suspend fun getAllAchievements(): List<Pair<AchievementGlobal, UserAchievement?>> {
        val globalDocs = globalAchievementsCollection().get().await().documents

        // Intentar obtener logros del usuario, si falla (permisos), usar lista vacía
        val userDocs = try {
            userAchievementsCollection().get().await().documents
        } catch (e: Exception) {
            // Si no hay permisos o no existen logros, retornar lista vacía
            emptyList()
        }

        val userMap = userDocs.associateBy { it.id }

        return globalDocs.map { doc ->
            val global = doc.toObject(AchievementGlobal::class.java)!!.copy(id = doc.id)
            val user = userMap[doc.id]?.toObject(UserAchievement::class.java)
            global to user
        }
    }

    suspend fun getPublicUserAchievements(userId: String): List<Pair<AchievementGlobal, UserAchievement?>> {
        val globalDocs = globalAchievementsCollection().get().await().documents

        // Intentar obtener logros del usuario, si falla (permisos), usar lista vacía
        val userDocs = try {
            db.collection("users")
                .document(userId)
                .collection("achievements")
                .get()
                .await()
                .documents
        } catch (e: Exception) {
            // Si no hay permisos, retornar lista vacía
            emptyList()
        }

        val userMap = userDocs.associateBy { it.id }

        return globalDocs.map { doc ->
            val global = doc.toObject(AchievementGlobal::class.java)!!.copy(id = doc.id)
            val user = userMap[doc.id]?.toObject(UserAchievement::class.java)
            global to user
        }
    }

    // ─────────────────────────────────────────────────────────────
    // FUNCIÓN PRINCIPAL (NUEVO MOTOR)
    // ─────────────────────────────────────────────────────────────

    @SuppressLint("NewApi")
    suspend fun evaluateAchievements(userCars: List<Car>, currentUser: User? = null) {
        // Check API level at runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            evaluateAchievementsInternal(userCars, currentUser)
        }
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun evaluateAchievementsInternal(userCars: List<Car>, currentUser: User?) = withContext(Dispatchers.Default) {

        val achievements = getAllAchievements()

        // Early exit si no hay logros activos
        val activeAchievements = achievements.filter { it.first.active && it.second?.unlocked != true }
        if (activeAchievements.isEmpty()) {
            return@withContext
        }

        for ((global, userState) in activeAchievements) {
            val updatedState = evaluateSingleAchievement(
                global = global,
                userState = userState,
                cars = userCars,
                currentUser = currentUser
            )

            if (updatedState != null) {
                userAchievementsCollection()
                    .document(global.id)
                    .set(updatedState)
                    .await()

                if (updatedState.unlocked) {
                    // Crear notificación de logro desbloqueado
                    withContext(Dispatchers.Main) {
                        notificationMethods.createAchievementNotification(
                            achievementTitle = global.title,
                            achievementId = global.id,
                            iconUrl = global.iconUrl
                        )
                    }

                    // 🎮 Otorgar XP por desbloquear logro (basado en rareza)
                    try {
                        val xpAmount = when (global.rarity) {
                            AchievementRarity.COMUN -> 200
                            AchievementRarity.RARO -> 400
                            AchievementRarity.LEGENDARIO -> 800
                            AchievementRarity.SPECIAL -> 1200
                        }
                        userMethods.addXP(
                            amount = xpAmount,
                            source = XPSource.ACHIEVEMENT_UNLOCKED,
                            sourceId = global.id
                        )
                    } catch (_: Exception) {
                        // No fallar la operación completa si solo falla la XP
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // EVALUAR UN LOGRO
    // ─────────────────────────────────────────────────────────────

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun evaluateSingleAchievement(
        global: AchievementGlobal,
        userState: UserAchievement?,
        cars: List<Car>,
        currentUser: User?
    ): UserAchievement? {

        val previous = userState ?: UserAchievement(
            achievementId = global.id,
            goal = global.goal
        )

        // 🎮 LOGROS DE NIVEL - Nueva lógica
        if (global.id.startsWith("level_")) {
            return evaluateLevelAchievement(global, previous, currentUser)
        }

        val countedIds = previous.countedCarIds.toMutableSet()

        // TIME BASED
        if (global.rules.timeWindow != null) {
            return evaluateTimeBasedAchievement(global, previous, cars)
        }

        // Para logros con lógica OR, rastreamos qué condiciones se han matcheado
        val matchedConditionIndices = previous.matchedConditionIndices.toMutableSet()

        for (car in cars) {
            val carId = car.id ?: continue

            // Si ya contamos este carro, saltarlo
            if (countedIds.contains(carId)) continue

            // ✅ Early exit: si ya alcanzamos la meta, no seguir evaluando
            val currentProgress = when {
                global.rules.conditionLogic == ConditionLogic.OR && global.conditions.isNotEmpty() -> {
                    // Para OR: contar solo las condiciones que tienen allowMultiplePerConcept=false
                    // y sumar los carros que tienen allowMultiplePerConcept=true
                    var singleCountConditions = 0
                    var multiCountCars = 0

                    for ((condIndex, condition) in global.conditions.withIndex()) {
                        if (condition.allowMultiplePerConcept) {
                            // Contar cuántos carros matchean esta condición
                            multiCountCars += cars.count { c ->
                                c.id in countedIds && carMatchesCondition(c, condition)
                            }
                        } else {
                            // Contar si hay al menos un carro para esta condición
                            if (condIndex in matchedConditionIndices) {
                                singleCountConditions++
                            }
                        }
                    }
                    singleCountConditions + multiCountCars
                }
                else -> countedIds.size
            }

            if (currentProgress >= global.goal) {
                break // Ya alcanzamos la meta, no necesitamos seguir
            }

            // Verificar qué condición(es) matchea este carro
            if (global.rules.conditionLogic == ConditionLogic.OR) {
                // Para OR: encontrar la primera condición que matchee
                val matchedIndex = global.conditions.indexOfFirst { condition ->
                    carMatchesCondition(car, condition)
                }

                if (matchedIndex >= 0) {
                    val condition = global.conditions[matchedIndex]

                    if (condition.allowMultiplePerConcept) {
                        // Si permite múltiples: contar este carro
                        countedIds.add(carId)
                    } else {
                        // Si no permite múltiples: solo marcar la condición como encontrada
                        matchedConditionIndices.add(matchedIndex)
                        countedIds.add(carId) // Marcar el carro como contado para evitar re-procesar
                    }
                }
            } else {
                // Para AND: mantener lógica original
                if (carMatchesConditions(car, global.conditions, global.rules.conditionLogic)) {
                    countedIds.add(carId)
                }
            }
        }

        // Calcular progreso final
        val progress = when {
            global.rules.conditionLogic == ConditionLogic.OR && global.conditions.isNotEmpty() -> {
                // Para OR: contar condiciones con allowMultiplePerConcept=false + carros con allowMultiplePerConcept=true
                var singleCountConditions = 0
                var multiCountCars = 0

                for ((condIndex, condition) in global.conditions.withIndex()) {
                    if (condition.allowMultiplePerConcept) {
                        // Contar cuántos carros matchean esta condición
                        multiCountCars += cars.count { c ->
                            c.id in countedIds && carMatchesCondition(c, condition)
                        }
                    } else {
                        // Contar si hay al menos un carro para esta condición
                        if (condIndex in matchedConditionIndices) {
                            singleCountConditions++
                        }
                    }
                }
                singleCountConditions + multiCountCars
            }
            else -> countedIds.size
        }

        val unlocked = progress >= global.goal


        if (
            progress == previous.progress &&
            unlocked == previous.unlocked
        ) {
            return null // no cambios
        }

        return previous.copy(
            progress = progress,
            unlocked = unlocked,
            unlockedAt = if (unlocked && previous.unlockedAt == null)
                System.currentTimeMillis() else previous.unlockedAt,
            countedCarIds = countedIds.toList(),
            matchedConditionIndices = matchedConditionIndices.sorted(),
            lastEvaluatedAt = System.currentTimeMillis()
        )
    }

    // ─────────────────────────────────────────────────────────────
    // LOGROS DE NIVEL
    // ─────────────────────────────────────────────────────────────

    private fun evaluateLevelAchievement(
        global: AchievementGlobal,
        previous: UserAchievement,
        currentUser: User?
    ): UserAchievement? {

        if (currentUser == null) {
            return null
        }

        val currentLevel = currentUser.level
        val requiredLevel = global.goal

        // El progreso es el nivel actual (máximo el requerido)
        val progress = minOf(currentLevel, requiredLevel)
        val unlocked = currentLevel >= requiredLevel

        // Si no hay cambios, retornar null
        if (progress == previous.progress && unlocked == previous.unlocked) {
            return null
        }


        return previous.copy(
            progress = progress,
            unlocked = unlocked,
            unlockedAt = if (unlocked && previous.unlockedAt == null)
                System.currentTimeMillis() else previous.unlockedAt,
            lastEvaluatedAt = System.currentTimeMillis()
        )
    }

    // ─────────────────────────────────────────────────────────────
    // LOGROS POR TIEMPO (DÍA / MES)
    // ─────────────────────────────────────────────────────────────

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun evaluateTimeBasedAchievement(
        global: AchievementGlobal,
        previous: UserAchievement,
        cars: List<Car>
    ): UserAchievement {

        val formatter = when (global.rules.timeWindow) {
            TimeWindow.DAY -> DateTimeFormatter.ofPattern("yyyy-MM-dd")
            TimeWindow.MONTH -> DateTimeFormatter.ofPattern("yyyy-MM")
            else -> return previous
        }

        val grouped = cars.groupBy { car ->
            formatter.format(
                Instant.ofEpochMilli(car.createdAt ?: 0)
                    .atZone(ZoneId.systemDefault())
            )
        }

        val max = grouped.maxOfOrNull { it.value.size } ?: 0
        val unlocked = max >= global.goal

        return previous.copy(
            progress = max,
            unlocked = unlocked,
            unlockedAt = if (unlocked && previous.unlockedAt == null)
                System.currentTimeMillis() else previous.unlockedAt,
            lastEvaluatedAt = System.currentTimeMillis()
        )
    }

    // ─────────────────────────────────────────────────────────────
    // MATCHING DE CONDICIONES
    // ─────────────────────────────────────────────────────────────

    private fun carMatchesConditions(
        car: Car,
        conditions: List<AchievementCondition>,
        logic: ConditionLogic
    ): Boolean {
        return when (logic) {
            ConditionLogic.AND -> conditions.all { condition ->
                carMatchesCondition(car, condition)
            }
            ConditionLogic.OR -> conditions.any { condition ->
                carMatchesCondition(car, condition)
            }
        }
    }

    private fun carMatchesCondition(
        car: Car,
        condition: AchievementCondition
    ): Boolean {

        // Si el concepto está vacío Y no hay aliases, matchea cualquier carro
        if (condition.concept.isEmpty() && condition.aliases.isEmpty()) {
            return true
        }

        val valuesToMatch = mutableListOf<String>()
        if (condition.concept.isNotEmpty()) {
            valuesToMatch.add(normalizeString(condition.concept))
        }
        valuesToMatch.addAll(condition.aliases.map { normalizeString(it) })

        // Si no hay valores para matchear, matchea cualquier carro
        if (valuesToMatch.isEmpty()) {
            return true
        }

        for (field in condition.matchFields) {
            val fieldValue = getCarFieldValue(car, field) ?: continue

            for (target in valuesToMatch) {
                val matched = matches(fieldValue, target, condition.matchType)
                if (matched) {
                    return true
                }
            }
        }

        return false
    }

    private fun getCarFieldValue(car: Car, field: CarMatchField): String? {
        val rawValue = when (field) {
            CarMatchField.NAME -> car.name
            CarMatchField.BRAND -> car.brand
            CarMatchField.SERIE -> car.serie
            CarMatchField.TYPE -> car.type
            CarMatchField.QUALITY -> car.quality
            CarMatchField.COLOR -> car.color
            CarMatchField.YEAR -> car.year
            CarMatchField.TAGS -> car.tags.joinToString(" ")
        }
        return rawValue?.let { normalizeString(it) }
    }

    /**
     * Normaliza una string para búsqueda:
     * - Convierte a minúsculas
     * - Remueve espacios al inicio y final
     * - Remueve caracteres especiales (manteniendo espacios internos)
     * - Reemplaza múltiples espacios por uno solo
     */
    private fun normalizeString(input: String): String {
        return input
            .lowercase()
            .trim()
            .replace(Regex("[^a-z0-9\\s]"), "") // Remover caracteres especiales
            .replace(Regex("\\s+"), " ") // Reemplazar múltiples espacios por uno
    }

    private fun matches(value: String, target: String, type: MatchType): Boolean {
        return when (type) {
            MatchType.EXACT -> value == target
            MatchType.CONTAINS -> value.contains(target)
            MatchType.STARTS_WITH -> value.startsWith(target)
        }
    }

    suspend fun addOrUpdateGlobalAchievement(
        achievement: AchievementGlobal
    ) {
        require(achievement.id.isNotBlank()) {
            "Achievement id no puede estar vacío"
        }

        FirebaseFirestore.getInstance()
            .collection("achievements")
            .document(achievement.id)
            .set(achievement)
            .await()
    }

    suspend fun deleteGlobalAchievement(
        achievementId: String
    ) {
        require(achievementId.isNotBlank()) {
            "achievementId no puede estar vacío"
        }

        FirebaseFirestore.getInstance()
            .collection("achievements")
            .document(achievementId)
            .delete()
            .await()
    }

    suspend fun getAllGlobalAchievements(): List<AchievementGlobal> {
        return FirebaseFirestore.getInstance()
            .collection("achievements")
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject(AchievementGlobal::class.java)
                    ?.copy(id = doc.id)
            }
    }

    suspend fun getAchievementById(achievementId: String): AchievementGlobal? {
        require(achievementId.isNotBlank()) {
            "achievementId no puede estar vacío"
        }

        return FirebaseFirestore.getInstance()
            .collection("achievements")
            .document(achievementId)
            .get()
            .await()
            .toObject(AchievementGlobal::class.java)
            ?.copy(id = achievementId)
    }


}
