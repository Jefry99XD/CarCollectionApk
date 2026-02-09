package com.example.carcollection.featureAchievements.data

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.carcollection.featureAchievements.domain.*
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featureNotification.data.NotificationMethods
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
        val userDocs = userAchievementsCollection().get().await().documents
        val userMap = userDocs.associateBy { it.id }

        return globalDocs.map { doc ->
            val global = doc.toObject(AchievementGlobal::class.java)!!.copy(id = doc.id)
            val user = userMap[doc.id]?.toObject(UserAchievement::class.java)
            global to user
        }
    }

    suspend fun getPublicUserAchievements(userId: String): List<Pair<AchievementGlobal, UserAchievement?>> {
        val globalDocs = globalAchievementsCollection().get().await().documents
        val userDocs = db.collection("users")
            .document(userId)
            .collection("achievements")
            .get()
            .await()
            .documents
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
    suspend fun evaluateAchievements(userCars: List<Car>) {
        // Check API level at runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            evaluateAchievementsInternal(userCars)
        }
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun evaluateAchievementsInternal(userCars: List<Car>) = withContext(Dispatchers.Default) {

        val achievements = getAllAchievements()

        for ((global, userState) in achievements) {
            if (!global.active) continue
            if (userState?.unlocked == true) continue

            val updatedState = evaluateSingleAchievement(
                global = global,
                userState = userState,
                cars = userCars
            )

            if (updatedState != null) {
                userAchievementsCollection()
                    .document(global.id)
                    .set(updatedState)
                    .await()

                if (updatedState.unlocked) {
                    // Crear notificación de logro desbloqueado
                    notificationMethods.createAchievementNotification(
                        achievementTitle = global.title,
                        achievementId = global.id,
                        iconUrl = global.iconUrl
                    )
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
        cars: List<Car>
    ): UserAchievement? {

        val previous = userState ?: UserAchievement(
            achievementId = global.id,
            goal = global.goal
        )

        val countedIds = previous.countedCarIds.toMutableSet()

        // TIME BASED
        if (global.rules.timeWindow != null) {
            return evaluateTimeBasedAchievement(global, previous, cars)
        }

        for (car in cars) {
            val carId = car.id ?: continue
            if (countedIds.contains(carId)) continue

            if (carMatchesConditions(car, global.conditions, global.rules.conditionLogic)) {
                countedIds.add(carId)
            }
        }

        val progress = countedIds.size
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
            valuesToMatch.add(condition.concept.lowercase())
        }
        valuesToMatch.addAll(condition.aliases.map { it.lowercase() })

        // Si no hay valores para matchear, matchea cualquier carro
        if (valuesToMatch.isEmpty()) {
            return true
        }

        for (field in condition.matchFields) {
            val fieldValue = getCarFieldValue(car, field) ?: continue

            for (target in valuesToMatch) {
                if (matches(fieldValue, target, condition.matchType)) {
                    return true
                }
            }
        }

        return false
    }

    private fun getCarFieldValue(car: Car, field: CarMatchField): String? {
        return when (field) {
            CarMatchField.NAME -> car.name
            CarMatchField.BRAND -> car.brand
            CarMatchField.SERIE -> car.serie
            CarMatchField.TYPE -> car.type
            CarMatchField.QUALITY -> car.quality
            CarMatchField.COLOR -> car.color
            CarMatchField.YEAR -> car.year
            CarMatchField.TAGS -> car.tags.joinToString(" ")
        }?.lowercase()
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



}
