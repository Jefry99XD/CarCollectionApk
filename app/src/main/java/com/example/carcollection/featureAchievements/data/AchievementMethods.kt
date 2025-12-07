package com.example.carcollection.featureAchievements.data

import com.example.carcollection.featureAchievements.domain.AchievementGlobal
import com.example.carcollection.featureAchievements.domain.AchievementType
import com.example.carcollection.featureAchievements.domain.UserAchievement
import com.example.carcollection.featurecar.domain.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AchievementMethods {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
    suspend fun checkAndUpdateAchievements(userCars: List<Car>,onAchievementUnlocked: ((AchievementGlobal) -> Unit)? = null) {
        ensureUserAchievementsExist()
        val achievements = getAllAchievements()


        for ((achievement, userAchievement) in achievements) {
            var progressCount = 0

            // Filtrar los carros según el tipo de logro
            when (achievement.type) {
                AchievementType.GENERAL -> {
                    // Por ejemplo, logros del tipo “agrega tu primer carro”
                    progressCount = userCars.size
                }
                AchievementType.TAG -> {
                    val tag = achievement.condition.tag?.lowercase()?.trim()
                    if (!tag.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            car.tags.any { it.lowercase().trim() == tag }
                        }
                    }
                }
                AchievementType.SERIE -> {
                    val serie = achievement.condition.serie?.lowercase()?.trim()
                    if (!serie.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            car.serie?.lowercase()?.trim() == serie
                        }
                    }
                }
                AchievementType.BRAND -> {
                    val brand = achievement.condition.brand?.lowercase()?.trim()
                    if (!brand.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            car.brand?.lowercase()?.trim() == brand
                        }
                    }
                }
                AchievementType.COLOR -> {
                    val color = achievement.condition.color?.lowercase()?.trim()
                    if (!color.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            car.color?.lowercase()?.trim() == color
                        }
                    }
                }
                AchievementType.YEAR -> {
                    val year = achievement.condition.year?.lowercase()?.trim()
                    if (!year.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            car.year?.lowercase()?.trim() == year
                        }
                    }
                }
                AchievementType.NAME -> {
                    val name = achievement.condition.name?.lowercase()?.trim()
                    if (!name.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            car.name?.lowercase()?.trim()?.contains(name) == true
                        }
                    }
                }
                AchievementType.QUALITY -> {
                    val quality = achievement.condition.quality?.trim()
                    if (!quality.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            car.quality?.trim() == quality
                        }
                    }
                }
                AchievementType.TYPE -> {
                    val type = achievement.condition.type?.lowercase()?.trim()
                    if (!type.isNullOrEmpty()) {
                        progressCount = userCars.count { car ->
                            car.type?.lowercase()?.trim() == type
                        }
                    }
                }
                AchievementType.LIST_BY_NAME -> {
                    val namesList = achievement.condition.namesList
                        ?.lowercase()
                        ?.split(",")
                        ?.map { it.trim().replace("\\s+".toRegex(), " ") } // Normalizar espacios múltiples
                        ?.filter { it.isNotEmpty() }
                        ?.toSet() // Eliminamos duplicados si hay

                    println("🎯 [LIST_BY_NAME] Logro: ${achievement.title}")
                    println("📋 Lista requerida del logro: $namesList")

                    if (!namesList.isNullOrEmpty()) {
                        // Convertimos los nombres de los carros del usuario en set, normalizando espacios
                        val userCarNames = userCars
                            .mapNotNull { car ->
                                car.name
                                    ?.lowercase()
                                    ?.trim()
                                    ?.replace("\\s+".toRegex(), " ") // Normalizar espacios múltiples
                            }
                            .toSet()

                        println("🚗 Nombres del usuario (${userCarNames.size}): $userCarNames")

                        // Progreso = cuántos nombres de la lista aparecen EXACTAMENTE en la colección del usuario
                        // Usamos coincidencia exacta, no parcial
                        progressCount = namesList.count { requiredName ->
                            val found = userCarNames.any { userName -> userName == requiredName }
                            if (found) {
                                println("✅ Coincidencia encontrada: '$requiredName'")
                            } else {
                                println("❌ NO encontrado: '$requiredName'")
                            }
                            found
                        }

                        println("📊 Progreso: $progressCount de ${namesList.size}")
                    }
                }

                AchievementType.MIXED -> {
                    // Ejemplo: podrías mezclar condiciones
                    progressCount = userCars.count { car ->
                        (achievement.condition.brand == null || car.brand == achievement.condition.brand) &&
                                (achievement.condition.color == null || car.color == achievement.condition.color)
                    }
                }
            }


            val currentProgress = userAchievement?.progress ?: 0
            val goal = achievement.goal
            val isUnlocked = userAchievement?.unlocked ?: false

            println("🔍 [${achievement.type}] ${achievement.title}")
            println("   📊 Progreso calculado: $progressCount | Progreso actual: $currentProgress | Meta: $goal | Desbloqueado: $isUnlocked")

            // Solo actualizar si el progreso calculado es diferente al actual
            if (progressCount != currentProgress) {
                println("   ⬆️ Actualizando progreso de $currentProgress a $progressCount")
                setProgress(achievement.id, progressCount)
            }

            if (progressCount >= goal && !isUnlocked) {
                println("   🎉 ¡LOGRO DESBLOQUEADO! ${achievement.title}")
                unlockAchievement(achievement.id)
                onAchievementUnlocked?.invoke(achievement)
            }

            println("   ───────────────────────────────────")

        }
    }

}
