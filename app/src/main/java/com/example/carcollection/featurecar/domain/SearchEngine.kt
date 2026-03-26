package com.example.carcollection.featurecar.domain

/**
 * Índice invertido en memoria para búsqueda rápida
 * Soporta búsqueda O(1) usando tokenización
 */
class InMemorySearchIndex<T>(
    private val items: List<T>,
    private val extractors: Map<String, (T) -> String>
) {
    private var index: Map<String, Set<Int>> = emptyMap()
    private var isBuilt = false

    fun buildIndex() {
        val invertedIndex = mutableMapOf<String, MutableSet<Int>>()

        items.forEachIndexed { idx, item ->
            extractors.forEach { (fieldName, extractor) ->
                val value = extractor(item).lowercase().trim()
                val tokens = tokenize(value)

                tokens.forEach { token ->
                    invertedIndex.getOrPut(token) { mutableSetOf() }
                        .add(idx)
                }
            }
        }

        index = invertedIndex
        isBuilt = true
    }

    fun search(query: String): List<T> {
        if (!isBuilt) buildIndex()

        if (query.isBlank()) return items

        val tokens = tokenize(query.lowercase())
        if (tokens.isEmpty()) return emptyList()

        // Intersecar sets de índices para cada token (AND logic)
        var resultIndices = index[tokens[0]]?.toMutableSet() ?: mutableSetOf()

        tokens.drop(1).forEach { token ->
            val tokenIndices = index[token] ?: setOf()
            resultIndices.retainAll(tokenIndices)
        }

        return resultIndices.map { items[it] }
    }

    /**
     * Tokeniza texto en palabras individuales para búsqueda
     * Filtra palabras muy cortas (<2 caracteres)
     */
    private fun tokenize(text: String): List<String> {
        return text
            .replace(Regex("[^a-záéíóúñ0-9\\s]"), "") // Remover caracteres especiales
            .split(Regex("[\\s\\-_]+"))
            .filter { it.isNotEmpty() && it.length > 1 } // Solo tokens > 1 char
            .distinct()
    }

    fun rebuild(newItems: List<T>) {
        val newIndex = InMemorySearchIndex(newItems, extractors)
        newIndex.buildIndex()
        this.index = newIndex.index
        this.isBuilt = true
    }
}

/**
 * Motor de búsqueda de carros on-device
 * Proporciona búsqueda rápida sin acceso a BD
 */
object SearchEngine {

    private var carIndex: InMemorySearchIndex<Car>? = null

    fun createCarIndex(cars: List<Car>) {
        carIndex = InMemorySearchIndex(
            items = cars,
            extractors = mapOf(
                "name" to { it.name.orEmpty() },
                "brand" to { it.brand.orEmpty() },
                "color" to { it.color.orEmpty() },
                "year" to { it.year.orEmpty() },
                "type" to { it.type.orEmpty() },
                "serie" to { it.serie.orEmpty() },
                "quality" to { it.quality.orEmpty() },
                "tags" to { it.tags.joinToString(" ") }
            )
        )
        carIndex?.buildIndex()
    }

    fun searchCars(query: String): List<Car> {
        return carIndex?.search(query) ?: emptyList()
    }

    fun isIndexReady(): Boolean = carIndex != null
}

