package com.example.carcollection.featureAchievements.domain

data class AchievementGlobal(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val type: AchievementType = AchievementType.GENERAL, // Nuevo
    val condition: AchievementCondition = AchievementCondition(),
    val goal: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AchievementType {
    GENERAL, TAG, SERIE, COLOR, BRAND, YEAR, MIXED
}

data class AchievementCondition(
    val tag: String? = null,
    val serie: String? = null,
    val color: String? = null,
    val brand: String? = null,
    val year: String? = null
)




