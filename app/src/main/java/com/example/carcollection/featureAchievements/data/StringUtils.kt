package com.example.carcollection.featureAchievements.data

import java.text.Normalizer
import kotlin.math.max

object StringUtils {

    // Caché de normalización para evitar recalcular el mismo string
    private val normalizeCache = mutableMapOf<String, String>()
    private val synonymCache = mutableMapOf<String, String>()
    private const val MAX_CACHE_SIZE = 500 // Limitar tamaño de caché

    /**
     * Limpia las cachés si se excede el tamaño máximo
     */
    private fun cleanCacheIfNeeded() {
        if (normalizeCache.size > MAX_CACHE_SIZE) {
            normalizeCache.clear()
        }
        if (synonymCache.size > MAX_CACHE_SIZE) {
            synonymCache.clear()
        }
    }

    /**
     * Limpia todas las cachés manualmente
     * Útil para liberar memoria o cuando el usuario cierra sesión
     */
    fun clearAllCaches() {
        normalizeCache.clear()
        synonymCache.clear()
    }

    /**
     * Diccionario de sinónimos y traducciones comunes
     * Mapea variaciones al término canónico (normalizado)
     */
    private val synonymMap = mapOf(
        // Fantasía - Fantasy
        "fantasy" to "fantasia",
        "fantasia" to "fantasia",

        // Tipos de vehículos
        "sedan" to "sedan",
        "sedán" to "sedan",
        "suv" to "suv",
        "camioneta" to "truck",
        "truck" to "truck",
        "pickup" to "truck",
        "camion" to "truck",
        "deportivo" to "sports",
        "sports" to "sports",
        "sport" to "sports",
        "coupe" to "coupe",
        "coupé" to "coupe",
        "convertible" to "convertible",
        "descapotable" to "convertible",
        "van" to "van",
        "minivan" to "van",
        "hatchback" to "hatchback",

        // Colores comunes
        "rojo" to "red",
        "red" to "red",
        "azul" to "blue",
        "blue" to "blue",
        "verde" to "green",
        "green" to "green",
        "amarillo" to "yellow",
        "yellow" to "yellow",
        "negro" to "black",
        "black" to "black",
        "blanco" to "white",
        "white" to "white",
        "gris" to "gray",
        "gray" to "gray",
        "grey" to "gray",
        "plateado" to "silver",
        "silver" to "silver",
        "dorado" to "gold",
        "gold" to "gold",
        "naranja" to "orange",
        "orange" to "orange",
        "morado" to "purple",
        "purple" to "purple",
        "violeta" to "purple",
        "rosa" to "pink",
        "pink" to "pink",

        // Marcas comunes con variaciones
        "hotwheels" to "hot wheels",
        "hot wheels" to "hot wheels",
        "matchbox" to "matchbox",

        // Calidades
        "basico" to "basico",
        "básico" to "basico",
        "basic" to "basico",
        "premium" to "premium",
        "premiun" to "premium", // Error común

        // Otros términos
        "clasico" to "clasico",
        "clásico" to "clasico",
        "classic" to "clasico",
        "moderno" to "moderno",
        "modern" to "moderno",
        "antiguo" to "antiguo",
        "antique" to "antiguo",
        "vintage" to "vintage"
    )

    /**
     * Normaliza un string y aplica mapeo de sinónimos si existe
     * OPTIMIZADO: Usa caché para evitar recalcular
     */
    private fun normalizeWithSynonyms(text: String?): String {
        if (text == null) return ""

        // Verificar caché de sinónimos primero
        synonymCache[text]?.let { return it }

        cleanCacheIfNeeded()

        val normalized = normalize(text)

        // Buscar en el diccionario de sinónimos
        val result = synonymMap[normalized] ?: normalized

        // Guardar en caché
        synonymCache[text] = result

        return result
    }

    /**
     * Normaliza un string para comparación:
     * - Convierte a minúsculas
     * - Elimina acentos y diacríticos
     * - Elimina caracteres especiales (excepto letras, números y espacios)
     * - Normaliza espacios múltiples a uno solo
     * - Trim
     *
     * OPTIMIZADO: Usa caché para evitar recalcular el mismo string
     */
    fun normalize(text: String?): String {
        if (text == null) return ""

        // Verificar caché primero
        normalizeCache[text]?.let { return it }

        cleanCacheIfNeeded()

        val result = text
            .lowercase()
            // Eliminar acentos usando NFD (Normalization Form Decomposition)
            .let { Normalizer.normalize(it, Normalizer.Form.NFD) }
            .replace("\\p{M}".toRegex(), "") // Elimina marcas diacríticas
            // Eliminar caracteres especiales comunes que usuarios pueden agregar
            .replace("[^a-z0-9\\s]".toRegex(), "") // Solo letras, números y espacios
            // Normalizar espacios
            .replace("\\s+".toRegex(), " ")
            .trim()

        // Guardar en caché
        normalizeCache[text] = result

        return result
    }

    /**
     * Calcula la distancia de Levenshtein entre dos strings
     * (número de ediciones necesarias para convertir una en otra)
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length

        if (len1 == 0) return len2
        if (len2 == 0) return len1

        val matrix = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) matrix[i][0] = i
        for (j in 0..len2) matrix[0][j] = j

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                matrix[i][j] = minOf(
                    matrix[i - 1][j] + 1,      // deletion
                    matrix[i][j - 1] + 1,      // insertion
                    matrix[i - 1][j - 1] + cost // substitution
                )
            }
        }

        return matrix[len1][len2]
    }

    /**
     * Calcula la similitud entre dos strings (0.0 a 1.0)
     * 1.0 = idénticos, 0.0 = completamente diferentes
     */
    fun similarity(s1: String, s2: String): Float {
        val normalized1 = normalize(s1)
        val normalized2 = normalize(s2)

        if (normalized1 == normalized2) return 1.0f
        if (normalized1.isEmpty() || normalized2.isEmpty()) return 0.0f

        val maxLen = max(normalized1.length, normalized2.length)
        val distance = levenshteinDistance(normalized1, normalized2)

        return 1.0f - (distance.toFloat() / maxLen.toFloat())
    }

    /**
     * Verifica si dos strings son "similares" basándose en un umbral
     * @param threshold umbral de similitud (0.0 a 1.0), por defecto 0.85 (85% similar)
     * @param useSynonyms si debe usar el diccionario de sinónimos (true por defecto)
     * Threshold recomendados:
     * - 0.95: muy estricto (1-2 errores de tipeo)
     * - 0.85: balanceado (2-3 errores de tipeo) ← RECOMENDADO
     * - 0.75: más permisivo (3-4 errores de tipeo)
     */
    fun areSimilar(s1: String?, s2: String?, threshold: Float = 0.85f, useSynonyms: Boolean = true): Boolean {
        if (s1 == null || s2 == null) return false

        val normalized1 = if (useSynonyms) normalizeWithSynonyms(s1) else normalize(s1)
        val normalized2 = if (useSynonyms) normalizeWithSynonyms(s2) else normalize(s2)

        // Exact match después de normalización (incluyendo sinónimos)
        if (normalized1 == normalized2) return true

        // Si alguno está vacío, no son similares
        if (normalized1.isEmpty() || normalized2.isEmpty()) return false

        // Comparación por similitud
        return similarity(s1, s2) >= threshold
    }

    /**
     * Verifica si un string contiene a otro de manera "fuzzy" (tolerante a errores)
     * Útil para búsquedas parciales con tolerancia a errores
     */
    fun containsFuzzy(text: String?, searchTerm: String?, threshold: Float = 0.85f): Boolean {
        if (text == null || searchTerm == null) return false

        val normalizedText = normalize(text)
        val normalizedSearch = normalize(searchTerm)

        if (normalizedSearch.isEmpty()) return false

        // Si el término de búsqueda es corto, usar coincidencia exacta
        if (normalizedSearch.length <= 3) {
            return normalizedText.contains(normalizedSearch)
        }

        // Para términos más largos, buscar por similitud
        // Dividir el texto en palabras y buscar en cada una
        val words = normalizedText.split(" ")

        // Verificar si alguna palabra es similar al término de búsqueda
        return words.any { word ->
            similarity(word, normalizedSearch) >= threshold
        } || normalizedText.contains(normalizedSearch)
    }
}

