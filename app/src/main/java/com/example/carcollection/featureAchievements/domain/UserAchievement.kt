package com.example.carcollection.featureAchievements.domain

/**
 * Estado del logro para un usuario específico.
 * Guarda PROGRESO, no la lógica del logro.
 */
data class UserAchievement(

    // Referencia al AchievementGlobal.id
    val achievementId: String = "",

    // Progreso actual (cantidad de carros válidos)
    val progress: Int = 0,

    // Meta copiada para lectura rápida (opcional, pero útil)
    val goal: Int = 0,

    // Estado del logro
    val unlocked: Boolean = false,

    // Timestamp cuando se desbloqueó
    val unlockedAt: Long? = null,

    // IDs de carros que ya contaron para este logro
    // Evita duplicados y soporta edición
    val countedCarIds: List<String> = emptyList(),

    // Para logros por tiempo (DAY / MONTH)
    // Ej: "2026-01-20" o "2026-01"
    val timeKey: String? = null,

    // Índices de condiciones que ya se han matcheado (para logros OR con lista)
    // Ej: [0, 2, 5] significa que las condiciones 0, 2 y 5 ya tienen al menos un carro
    val matchedConditionIndices: List<Int> = emptyList(),

    // Última vez que se evaluó el logro
    val lastEvaluatedAt: Long = System.currentTimeMillis()
) {
    constructor() : this(
        achievementId = "",
        progress = 0,
        goal = 0,
        unlocked = false,
        unlockedAt = null,
        countedCarIds = emptyList(),
        timeKey = null,
        matchedConditionIndices = emptyList(),
        lastEvaluatedAt = System.currentTimeMillis()
    )
}
