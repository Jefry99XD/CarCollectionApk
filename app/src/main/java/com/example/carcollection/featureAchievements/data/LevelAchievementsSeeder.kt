package com.example.carcollection.featureAchievements.data

/*
 * ════════════════════════════════════════════════════════════════
 * ESTE ARCHIVO NO SE UTILIZA
 * ════════════════════════════════════════════════════════════════
 *
 * Los logros de nivel se agregarán manualmente en Firebase Console.
 *
 * 📖 Para instrucciones completas, consulta:
 * LOGROS_DE_NIVEL_MANUAL.md
 *
 * ════════════════════════════════════════════════════════════════
 */

// TODO el código está comentado porque no se usará el seeder automático

/*
import com.example.carcollection.featureAchievements.domain.*
import kotlinx.coroutines.runBlocking

class LevelAchievementsSeeder {

    private val achievementMethods = AchievementMethods()

    private val levelAchievements = listOf(
        LevelAchievementData(
            id = "level_5",
            title = "Coleccionista Principiante",
            description = "Alcanza el nivel 5",
            requiredLevel = 5,
            iconUrl = "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_5.png?alt=media"
        ),
        LevelAchievementData(
            id = "level_10",
            title = "Coleccionista Experimentado",
            description = "Alcanza el nivel 10",
            requiredLevel = 10,
            iconUrl = "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_10.png?alt=media"
        ),
        LevelAchievementData(
            id = "level_15",
            title = "Coleccionista Dedicado",
            description = "Alcanza el nivel 15",
            requiredLevel = 15,
            iconUrl = "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_15.png?alt=media"
        ),
        LevelAchievementData(
            id = "level_20",
            title = "Coleccionista Avanzado",
            description = "Alcanza el nivel 20",
            requiredLevel = 20,
            iconUrl = "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_20.png?alt=media"
        ),
        LevelAchievementData(
            id = "level_25",
            title = "Coleccionista Elite",
            description = "Alcanza el nivel 25",
            requiredLevel = 25,
            iconUrl = "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_25.png?alt=media"
        ),
        LevelAchievementData(
            id = "level_30",
            title = "Coleccionista Experto",
            description = "Alcanza el nivel 30",
            requiredLevel = 30,
            iconUrl = "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_30.png?alt=media"
        ),
        LevelAchievementData(
            id = "level_40",
            title = "Coleccionista Maestro",
            description = "Alcanza el nivel 40",
            requiredLevel = 40,
            iconUrl = "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_40.png?alt=media"
        ),
        LevelAchievementData(
            id = "level_50",
            title = "Maestro Coleccionista",
            description = "Alcanza el nivel 50",
            requiredLevel = 50,
            iconUrl = "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_50.png?alt=media"
        ),
        LevelAchievementData(
            id = "level_75",
            title = "Leyenda Viviente",
            description = "Alcanza el nivel 75",
            requiredLevel = 75,
            iconUrl = "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_75.png?alt=media"
        ),
        LevelAchievementData(
            id = "level_100",
            title = "Leyenda Inmortal",
            description = "Alcanza el nivel 100 - El pináculo de la colección",
            requiredLevel = 100,
            iconUrl = "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_100.png?alt=media"
        )
    )

    fun seedLevelAchievements() = runBlocking {
        println("🌱 Iniciando seeding de logros de nivel...")

        for (data in levelAchievements) {
            try {
                val achievement = createLevelAchievement(data)
                achievementMethods.addOrUpdateGlobalAchievement(achievement)
                println("✅ Logro agregado: ${data.id} - ${data.title}")
            } catch (e: Exception) {
                println("❌ Error al agregar logro ${data.id}: ${e.message}")
            }
        }

        println("🎉 Seeding completado! ${levelAchievements.size} logros de nivel agregados.")
    }

    private fun createLevelAchievement(data: LevelAchievementData): AchievementGlobal {
        return AchievementGlobal(
            id = data.id,
            title = data.title,
            description = data.description,
            iconUrl = data.iconUrl,
            goal = data.requiredLevel,
            conditions = emptyList(),
            rules = AchievementRules(
                conditionLogic = ConditionLogic.AND,
                timeWindow = null
            ),
            category = AchievementCategory.COLLECTION,
            active = true,
            createdAt = System.currentTimeMillis()
        )
    }

    data class LevelAchievementData(
        val id: String,
        val title: String,
        val description: String,
        val requiredLevel: Int,
        val iconUrl: String
    )
}
*/

