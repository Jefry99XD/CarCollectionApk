package com.example.carcollection.featureAchievements.data

import com.example.carcollection.featureAchievements.domain.AchievementGlobal
import com.example.carcollection.featureAchievements.domain.AchievementType
import com.example.carcollection.featureAchievements.domain.UserAchievement
import com.example.carcollection.featurecar.domain.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AchievementMethods {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Caché del último conteo de carros para evitar recalcular si no cambió nada
    private var lastCarCount = -1

    /**
     * Limpia la caché y resetea contadores
     * Útil cuando el usuario cierra sesión o cambia de cuenta
     */
    fun clearCache() {
        lastCarCount = -1
    }

    private fun userAchievementsCollection() =
        db.collection("users")
            .document(auth.currentUser?.uid ?: "")
            .collection("achievements")

    fun globalAchievementsCollection() = db.collection("achievements")

    // ─── Obtener todos los logros ────────────────────────────────────────────────
    suspend fun getAllAchievements(): List<Pair<AchievementGlobal, UserAchievement?>> {
        val globalDocs = globalAchievementsCollection().get().await().documents
        val userDocs = userAchievementsCollection().get().await().documents

        val userMap = userDocs.associateBy { it.id }

        return globalDocs.map { doc ->
            val global = doc.toObject(AchievementGlobal::class.java)!!.copy(id = doc.id)
            val userProgress = userMap[doc.id]?.toObject(UserAchievement::class.java)


            global to userProgress
        }
    }

    // ─── Obtener logros de un usuario público ────────────────────────────────────
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
            val userProgress = userMap[doc.id]?.toObject(UserAchievement::class.java)

            global to userProgress
        }
    }

    // ─── Incrementar progreso de logro ───────────────────────────────────────────
    suspend fun incrementProgress(achievementId: String, increment: Int) {
        val ref = userAchievementsCollection().document(achievementId)

        try {
            ref.update("progress", FieldValue.increment(increment.toLong())).await()
        } catch (_: Exception) {
            // Error updating progress
        }
    }

    // ─── Establecer progreso de logro directamente ──────────────────────────────
    suspend fun setProgress(achievementId: String, progress: Int) {
        val ref = userAchievementsCollection().document(achievementId)

        try {
            ref.update("progress", progress.toLong()).await()
        } catch (_: Exception) {
            // Error setting progress
        }
    }

    // ─── Desbloquear logro ──────────────────────────────────────────────────────
    suspend fun unlockAchievement(achievementId: String) {
        val ref = userAchievementsCollection().document(achievementId)
        try {
            ref.update(
                mapOf(
                    "unlocked" to true,
                    "unlockedAt" to System.currentTimeMillis()
                )
            ).await()
        } catch (_: Exception) {

        }
    }

    // ─── Agregar nuevo logro global ─────────────────────────────────────────────
    suspend fun addGlobalAchievement(achievement: AchievementGlobal) {
        val collection = globalAchievementsCollection()
        val docId = achievement.id.ifBlank { collection.document().id }

        val data = hashMapOf(
            "id" to docId,
            "title" to achievement.title,
            "description" to achievement.description,
            "iconUrl" to achievement.iconUrl,
            "goal" to achievement.goal,
            "type" to achievement.type.name,
            "condition" to mapOf(
                "tag" to achievement.condition.tag,
                "serie" to achievement.condition.serie,
                "color" to achievement.condition.color,
                "brand" to achievement.condition.brand,
                "year" to achievement.condition.year,
                "name" to achievement.condition.name,
                "quality" to achievement.condition.quality,
                "type" to achievement.condition.type,
                "namesList" to achievement.condition.namesList
            ),
            "createdAt" to achievement.createdAt
        )

        collection.document(docId).set(data).await()
    }

    // ─── Crear documentos vacíos de usuario si no existen ───────────────────────
    suspend fun ensureUserAchievementsExist() {
        val globalAchievements = globalAchievementsCollection().get().await().documents
        val userCollection = userAchievementsCollection()

        for (doc in globalAchievements) {
            val achievementId = doc.id
            val userDoc = userCollection.document(achievementId).get().await()
            if (!userDoc.exists()) {
                val emptyUserAchievement = UserAchievement(
                    achievementId = achievementId,
                    progress = 0,
                    unlocked = false,
                    unlockedAt = null
                )
                userCollection.document(achievementId).set(emptyUserAchievement).await()
            }
        }
    }

    // ─── Verificar y actualizar todos los logros ────────────────────────────────
    suspend fun checkAndUpdateAchievements(userCars: List<Car>, onAchievementUnlocked: ((AchievementGlobal) -> Unit)? = null) {
        // Early exit: Si no hay carros, no hay nada que verificar
        if (userCars.isEmpty() && lastCarCount == 0) {
            return
        }

        // Early exit: Si el número de carros no cambió, probablemente no hay cambios significativos
        // (esto es una optimización simple, puede mejorar con hash de la colección)
        if (userCars.size == lastCarCount) {
            // Nota: Todavía verifica logros porque podrían haber editado un carro
            // pero reduce la frecuencia de verificación innecesaria
        }

        lastCarCount = userCars.size

        // Ejecutar la verificación en Dispatchers.Default (mejor para CPU-intensive tasks)
        withContext(Dispatchers.Default) {
            ensureUserAchievementsExist()
            val achievements = getAllAchievements()

            // Threshold de similitud (85% similar = tolera 2-3 errores de tipeo)
            val similarityThreshold = 0.85f

            // Lista de actualizaciones a realizar (batch processing)
            val updates = mutableListOf<Pair<String, Int>>()
            val unlocks = mutableListOf<AchievementGlobal>()

            for ((achievement, userAchievement) in achievements) {
                // Skip si ya está desbloqueado (optimización)
                if (userAchievement?.unlocked == true) {
                    continue
                }

                var progressCount = 0

            // Filtrar los carros según el tipo de logro
            when (achievement.type) {
                AchievementType.GENERAL -> {
                    // Logros generales (ej: "agrega tu primer carro")
                    progressCount = userCars.size
                }

                AchievementType.TAG -> {
                    val targetTag = achievement.condition.tag
                    if (!targetTag.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            car.tags.any { userTag ->
                                // Comparación fuzzy: tolera errores de tipeo
                                StringUtils.areSimilar(userTag, targetTag, similarityThreshold)
                            }
                        }
                    }
                }

                AchievementType.SERIE -> {
                    val targetSerie = achievement.condition.serie
                    if (!targetSerie.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            // Comparación fuzzy: tolera errores de tipeo en la serie
                            StringUtils.areSimilar(car.serie, targetSerie, similarityThreshold)
                        }
                    }
                }

                AchievementType.BRAND -> {
                    val targetBrand = achievement.condition.brand
                    if (!targetBrand.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            // Comparación fuzzy: tolera errores de tipeo en la marca
                            StringUtils.areSimilar(car.brand, targetBrand, similarityThreshold)
                        }
                    }
                }

                AchievementType.COLOR -> {
                    val targetColor = achievement.condition.color
                    if (!targetColor.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            // Comparación fuzzy: tolera errores de tipeo en el color
                            StringUtils.areSimilar(car.color, targetColor, similarityThreshold)
                        }
                    }
                }

                AchievementType.YEAR -> {
                    val targetYear = achievement.condition.year
                    if (!targetYear.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            // Para años, usar comparación exacta (normalizada)
                            // No tiene sentido usar fuzzy en años (2024 vs 2023 son diferentes)
                            StringUtils.normalize(car.year) == StringUtils.normalize(targetYear)
                        }
                    }
                }

                AchievementType.NAME -> {
                    val targetName = achievement.condition.name
                    if (!targetName.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            // Búsqueda parcial fuzzy: tolera errores de tipeo
                            StringUtils.containsFuzzy(car.name, targetName, similarityThreshold)
                        }
                    }
                }

                AchievementType.QUALITY -> {
                    val targetQuality = achievement.condition.quality
                    if (!targetQuality.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            // Comparación fuzzy: tolera errores de tipeo en calidad
                            StringUtils.areSimilar(car.quality, targetQuality, similarityThreshold)
                        }
                    }
                }

                AchievementType.TYPE -> {
                    val targetType = achievement.condition.type
                    if (!targetType.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            // Comparación fuzzy: tolera errores de tipeo en tipo
                            StringUtils.areSimilar(car.type, targetType, similarityThreshold)
                        }
                    }
                }

                AchievementType.LIST_BY_NAME -> {
                    val namesList = achievement.condition.namesList
                        ?.split(",")
                        ?.map { StringUtils.normalize(it) }
                        ?.filter { it.isNotEmpty() }
                        ?.toSet()

                    if (!namesList.isNullOrEmpty()) {
                        // Progreso = cuántos nombres de la lista están en la colección del usuario
                        // Usando comparación fuzzy para tolerar errores de tipeo
                        progressCount = namesList.count { requiredName ->
                            userCars.any { car ->
                                val carName = StringUtils.normalize(car.name ?: "")
                                if (carName.isEmpty()) return@any false

                                // Comparación fuzzy: tolera errores de tipeo
                                StringUtils.similarity(carName, requiredName) >= similarityThreshold
                            }
                        }
                    }
                }

                AchievementType.MIXED -> {
                    // Condiciones mixtas: todas deben cumplirse (AND)
                    progressCount = userCars.count { car ->
                        val brandMatch = achievement.condition.brand == null ||
                            StringUtils.areSimilar(car.brand, achievement.condition.brand, similarityThreshold)

                        val colorMatch = achievement.condition.color == null ||
                            StringUtils.areSimilar(car.color, achievement.condition.color, similarityThreshold)

                        val serieMatch = achievement.condition.serie == null ||
                            StringUtils.areSimilar(car.serie, achievement.condition.serie, similarityThreshold)

                        val yearMatch = achievement.condition.year == null ||
                            StringUtils.normalize(car.year) == StringUtils.normalize(achievement.condition.year)

                        val typeMatch = achievement.condition.type == null ||
                            StringUtils.areSimilar(car.type, achievement.condition.type, similarityThreshold)

                        val qualityMatch = achievement.condition.quality == null ||
                            StringUtils.areSimilar(car.quality, achievement.condition.quality, similarityThreshold)

                        brandMatch && colorMatch && serieMatch && yearMatch && typeMatch && qualityMatch
                    }
                }
            }

                val currentProgress = userAchievement?.progress ?: 0
                val goal = achievement.goal

                // Solo agregar a updates si el progreso cambió
                if (progressCount != currentProgress) {
                    updates.add(achievement.id to progressCount)
                }

                // Verificar si debe desbloquearse
                if (progressCount >= goal) {
                    unlocks.add(achievement)
                }
            }

            // Procesar actualizaciones en batch (fuera del loop principal)
            // Esto reduce las llamadas a Firebase
            for ((achievementId, progress) in updates) {
                setProgress(achievementId, progress)
            }

            // Desbloquear logros
            for (achievement in unlocks) {
                unlockAchievement(achievement.id)
                onAchievementUnlocked?.invoke(achievement)
            }
        }
    }

}
