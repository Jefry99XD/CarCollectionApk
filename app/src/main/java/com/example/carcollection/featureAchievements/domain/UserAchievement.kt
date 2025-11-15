package com.example.carcollection.featureAchievements.domain

data class UserAchievement(
    val achievementId: String = "",
    val progress: Int = 0,
    val unlocked: Boolean = false,
    val unlockedAt: Long? = null
){
    constructor() : this(
        achievementId = "",
        progress = 0,
        unlocked = false,
        unlockedAt = null
    )
}

