package com.example.carcollection.featureuser.domain

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.serialization.Serializable

@Serializable
@IgnoreExtraProperties
data class User(
    @get:Exclude
    val uid: String = "", // No se guarda como campo, solo se usa como ID del documento

    // ─── Datos básicos ───
    val username: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val bio: String? = null,

    // ─── Datos de registro ───
    val createdAt: Long = System.currentTimeMillis(),

    // ─── Estadísticas básicas ───
    @get:Exclude
    var totalCars: Int = 0,

    @get:Exclude
    var totalTags: Int = 0,

    @get:Exclude
    var totalSeries: Int = 0,

    // ─── Sistema de Niveles ───
    val level: Int = 1,
    val totalXP: Long = 0,
    val xpFromCars: Long = 0,           // XP ganada de carros
    val xpFromAchievements: Long = 0,   // XP ganada de logros

    // ─── Insignias ───
    val badges: List<String> = emptyList(), // Ejemplo: ["Coleccionista", "Veterano"]

    // ─── Carros Favoritos y Showcase ───
    val favoriteCars: List<String> = emptyList(),      // IDs de carros favoritos (sin límite)
    val showcaseCars: List<String> = emptyList(),      // IDs de carros en showcase (máximo 10, debe estar en favoriteCars)

    // ─── Derechos de administrador ───
    val adminRights: Boolean = false, // Solo puede ser modificado en Firebase

    // ─── Flags de migración ───
    val backgroundsMigrated: Boolean = false, // Flag para migración de fondos (backgroundName → backgroundUrl)
    val backgroundsMigratedAt: Long? = null,  // Timestamp de cuándo se hizo la migración

    // ─── Última actividad ───
    val lastActive: Long = System.currentTimeMillis(),
) {
    constructor() : this("", null, null, null, null, 0L, 0, 0, 0, 1, 0L, 0L, 0L, emptyList(), emptyList(), emptyList(), false, false, null, 0L)

    // Propiedad computada: días desde que se unió
    @get:Exclude
    val memberDays: Int
        get() = ((System.currentTimeMillis() - createdAt) / (1000 * 60 * 60 * 24)).toInt()

    // Propiedad computada: XP actual en el nivel
    @get:Exclude
    val currentLevelXP: Long
        get() {
            var xpNeeded = 0L
            var currentLvl = 1
            while (currentLvl < level) {
                xpNeeded += calculateXPForLevel(currentLvl)
                currentLvl++
            }
            return totalXP - xpNeeded
        }

    // Propiedad computada: XP necesaria para el siguiente nivel
    @get:Exclude
    val xpForNextLevel: Long
        get() = calculateXPForLevel(level)

    // Propiedad computada: Progreso hacia el siguiente nivel (0.0 - 1.0)
    @get:Exclude
    val levelProgress: Float
        get() {
            val needed = xpForNextLevel
            return if (needed > 0) {
                (currentLevelXP.toFloat() / needed.toFloat()).coerceIn(0f, 1f)
            } else 0f
        }

    // Método para verificar si el usuario es admin
    @get:Exclude
    val isAdmin: Boolean
        get() = adminRights

    // Método para actualizar las estadísticas
    fun updateStats(cars: Int, tags: Int, series: Int = 0): User {
        return this.copy().apply {
            totalCars = cars
            totalTags = tags
            totalSeries = series
        }
    }

    companion object {
        // Cálculo de XP necesaria para un nivel específico
        // Usa BigDecimal para niveles altos para evitar pérdida de precisión
        fun calculateXPForLevel(level: Int): Long {
            return when {
                level <= 0 -> 100L
                level <= 20 -> {
                    // 🔹 Niveles bajos: usar Math.pow normal
                    (100 * Math.pow(1.25, level - 1.0)).toLong()
                }
                level <= 50 -> {
                    // 🔹 Niveles medios: usar BigDecimal
                    val base = java.math.BigDecimal("100")
                    val multiplier = java.math.BigDecimal("1.25")
                    val xp20 = base * multiplier.pow(19)

                    val xp20Value = xp20.toLong()
                    val additionalMultiplier = java.math.BigDecimal("1.20")
                    (xp20Value * additionalMultiplier.pow(level - 20).toLong())
                }
                else -> {
                    // 🔹 Niveles altos: usar BigDecimal para máxima precisión
                    val base = java.math.BigDecimal("100")
                    val multiplier125 = java.math.BigDecimal("1.25")
                    val multiplier120 = java.math.BigDecimal("1.20")
                    val multiplier115 = java.math.BigDecimal("1.15")

                    val xp20 = base * multiplier125.pow(19)
                    val xp50 = xp20 * multiplier120.pow(30)
                    (xp50 * multiplier115.pow(level - 50)).toLong()
                }
            }
        }

        // Calcular nivel basado en XP total
        fun calculateLevelFromXP(totalXP: Long): Int {
            var level = 1
            var xpAccumulated = 0L

            while (xpAccumulated + calculateXPForLevel(level) <= totalXP) {
                xpAccumulated += calculateXPForLevel(level)
                level++

                // Límite de seguridad
                if (level > 10000) break
            }

            return level
        }

        // Calcular XP total necesaria hasta un nivel
        fun calculateTotalXPForLevel(level: Int): Long {
            var total = 0L
            for (lvl in 1 until level) {
                total += calculateXPForLevel(lvl)
            }
            return total
        }
    }
}
