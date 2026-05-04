package com.example.carcollection.featureAchievements.domain

import com.google.firebase.firestore.PropertyName

/**
 * Rareza del logro que determina la cantidad de XP otorgada
 */
enum class AchievementRarity {
    COMUN,      // XP estándar (200 XP por defecto)
    RARO,       // XP media (400 XP)
    LEGENDARIO, // XP alta (800 XP)
    SPECIAL     // XP muy alta (1200 XP)
}

/**
 * Definición global de un logro.
 * Describe QUÉ se debe cumplir, no el progreso del usuario.
 */
data class AchievementGlobal(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val iconUrl: String = "",

    // Categoría lógica del logro
    val category: AchievementCategory = AchievementCategory.COLLECTION,

    // Rareza del logro (determina XP otorgada)
    val rarity: AchievementRarity = AchievementRarity.COMUN,

    // Condiciones que debe cumplir un carro
    // TODAS las condiciones deben cumplirse (AND)
    val conditions: List<AchievementCondition> = emptyList(),

    // Cantidad de carros que deben cumplir las condiciones
    val goal: Int = 0,

    // Reglas adicionales (tiempo, conteo, etc.)
    val rules: AchievementRules = AchievementRules(),

    // Visibilidad y estado
    val hidden: Boolean = false,
    val active: Boolean = true,

    // ── LOGROS EXCLUSIVOS ──
    // Si es true: logro privado/exclusivo
    // Si es false: logro público disponible para todos
    val isExclusive: Boolean = false,

    // Lista de IDs de usuarios a quienes se les da este logro exclusivo
    // Solo aplica si isExclusive == true
    val exclusiveUserIds: List<String> = emptyList(),

    @PropertyName("createdAt")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Categoría descriptiva del logro.
 * Determina la lógica de evaluación principal.
 */
enum class AchievementCategory {
    COLLECTION,     // Logros basados en carros (usa condiciones)
    TIME_BASED,     // Logros basados en tiempo (fecha de agregación)
    STREAK_BASED,   // Logros de racha (X carros en X días consecutivos)
    USER,           // Logros de nivel/progreso del usuario
    EXCLUSIVE       // Logros exclusivos para usuarios específicos (sin condiciones ni meta)
}

/**
 * Condición individual que un carro debe cumplir.
 * Un carro cuenta solo una vez aunque matchee varios campos.
 */
data class AchievementCondition(

    // Concepto principal (ej: "fantasia", "pontiac", "premium")
    val concept: String = "",

    // Sinónimos aceptados para el concepto
    val aliases: List<String> = emptyList(),

    // Campos del carro donde se debe buscar el concepto
    val matchFields: List<CarMatchField> = emptyList(),

    // Tipo de comparación
    val matchType: MatchType = MatchType.CONTAINS,

    // Si TRUE: permite contar múltiples carros por concepto (ej: 10 Ferrari + 10 Lamborghini)
    // Si FALSE: solo cuenta 1 carro por concepto (ej: 1 Ferrari + 1 Lamborghini)
    val allowMultiplePerConcept: Boolean = false
)

/**
 * Campos del documento de carro que pueden ser usados para logros.
 *
 * EXCLUYE explícitamente:
 * - id
 * - backgroundName
 * - photoUrl
 */
enum class CarMatchField {
    NAME,
    BRAND,
    SERIE,
    TYPE,
    QUALITY,
    COLOR,
    YEAR,
    TAGS
}

/**
 * Tipo de comparación para el matching de texto.
 */
enum class MatchType {
    EXACT,        // Coincidencia exacta (ideal para QUALITY)
    CONTAINS,     // Substring (ej: "pontiac" en "pontiac gto")
    STARTS_WITH   // Prefijo (opcional)
}

/**
 * Reglas adicionales del logro.
 */
data class AchievementRules(

    // Un carro solo puede contar una vez por logro
    val uniquePerCar: Boolean = true,

    // Ventana de tiempo para logros TIME_BASED
    // DAY: Agrega X carros en 1 día
    // MONTH: Agrega X carros en 1 mes
    // YEAR: Agrega X carros en 1 año
    val timeWindow: TimeWindow? = null,

    // Lógica de evaluación de condiciones
    // AND: Un carro debe cumplir TODAS las condiciones
    // OR: Un carro debe cumplir AL MENOS UNA condición
    val conditionLogic: ConditionLogic = ConditionLogic.AND
)

/**
 * Ventana de tiempo usada en logros TIME_BASED.
 * Define el rango de fecha sobre el cual evaluar.
 */
enum class TimeWindow {
    DAY,    // Carros agregados en 1 día (últimas 24 horas)
    MONTH,  // Carros agregados en 1 mes (últimos 30 días)
    YEAR    // Carros agregados en 1 año (últimos 365 días)
}

/**
 * Lógica de evaluación de condiciones.
 */
enum class ConditionLogic {
    AND,  // Un carro debe cumplir TODAS las condiciones (Red Ferrari)
    OR    // Un carro debe cumplir AL MENOS UNA condición (Lista de nombres)
}

