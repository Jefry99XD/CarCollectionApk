package com.example.carcollection.featureAchievements.data

import android.util.Log
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

            Log.d(
                "ACH_DEBUG",
                "📥 Loaded: ${global.title} | progress=${userProgress?.progress} | unlocked=${userProgress?.unlocked}"
            )

            global to userProgress
        }
    }

    // ─── Incrementar progreso de logro ───────────────────────────────────────────
    suspend fun incrementProgress(achievementId: String, increment: Int) {
        val ref = userAchievementsCollection().document(achievementId)

        try {
            ref.update("progress", FieldValue.increment(increment.toLong())).await()
            Log.d("Achievements", "🔥 Incremented $achievementId → +$increment")
        } catch (e: Exception) {
            Log.e("Achievements", "❌ Error al incrementar progreso $achievementId", e)
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
            Log.d("Achievements", "🏆 Logro desbloqueado: $achievementId")
        } catch (e: Exception) {
            Log.e("Achievements", "❌ Error al desbloquear logro $achievementId", e)
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
                "year" to achievement.condition.year
            ),
            "createdAt" to achievement.createdAt
        )

        collection.document(docId).set(data).await()
        Log.d("Achievements", "🆕 Logro global agregado: ${achievement.title}")
    }

    // ─── Crear documentos vacíos de usuario si no existen ───────────────────────
    suspend fun ensureUserAchievementsExist() {
        val globalAchievements = globalAchievementsCollection().get().await().documents
        val userCollection = userAchievementsCollection()

        for (doc in globalAchievements) {
            val achievementId = doc.id
            val userDoc = userCollection.document(achievementId).get().await()
            if (!userDoc.exists()) {
                Log.d("Achievements", "🆕 Creando logro vacío: $achievementId")
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
        Log.d("Achievements", "🚗 checkAndUpdateAchievements → userCars=${userCars.size}")
        userCars.forEach { car ->
            Log.d("Achievements", "   • ${car.name} | ${car.brand} | ${car.tags}")
        }

        Log.d("Achievements", "▶ checkAndUpdateAchievements con ${userCars.size} carros")

        ensureUserAchievementsExist()
        val achievements = getAllAchievements()
        Log.d("Achievements", "🔍 Revisando ${achievements.size} logros...")


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

            Log.d(
                "Achievements",
                "🎯 [${achievement.title}] actual=$currentProgress nuevo=$progressCount meta=$goal unlocked=$isUnlocked"
            )

            if (progressCount > currentProgress) {
                val increment = progressCount - currentProgress
                Log.d("Achievements", "⬆️ Incrementando ${achievement.id} en +$increment")
                incrementProgress(achievement.id, increment)
            }

            if (progressCount >= goal && !isUnlocked) {
                Log.d("Achievements", "🏆 Cumple meta → Desbloqueando ${achievement.id}")
                unlockAchievement(achievement.id)
                onAchievementUnlocked?.invoke(achievement)
            }

        }

        Log.d("Achievements", "✅ Revisión de logros completada")
    }

}
