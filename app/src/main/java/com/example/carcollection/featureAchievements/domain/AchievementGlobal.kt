package com.example.carcollection.featureAchievements.domain

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

    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Categoría descriptiva del logro.
 * No depende directamente de un campo del carro.
 */
enum class AchievementCategory {
    COLLECTION,     // Conteo general de carros
    BRAND,          // Marca específica
    FANTASY,        // Fantasía / Fantasy
    PREMIUM,        // Calidad premium
    TIME_BASED,     // Día / Mes
    MIXED           // Condiciones compuestas
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
    val matchType: MatchType = MatchType.CONTAINS
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

    // Ventana de tiempo para logros por actividad
    val timeWindow: TimeWindow? = null,

    // Lógica de evaluación de condiciones
    // AND: Un carro debe cumplir TODAS las condiciones
    // OR: Un carro debe cumplir AL MENOS UNA condición
    val conditionLogic: ConditionLogic = ConditionLogic.AND
)

/**
 * Ventana de tiempo usada en logros basados en fecha.
 */
enum class TimeWindow {
    DAY,
    MONTH
}

/**
 * Lógica de evaluación de condiciones.
 */
enum class ConditionLogic {
    AND,  // Un carro debe cumplir TODAS las condiciones (Red Ferrari)
    OR    // Un carro debe cumplir AL MENOS UNA condición (Lista de nombres)
}

