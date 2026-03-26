package com.example.carcollection.featurecar.domain

/**
 * Estado de paginación para carros
 * @param items Lista de carros actuales (página actual)
 * @param currentPage Número de página actual (0-indexed)
 * @param pageSize Cantidad de items por página
 * @param totalFetched Total de items fetched hasta ahora (acumulativo)
 * @param isLoading Si se está cargando una página
 * @param hasMore Si hay más páginas por cargar
 * @param error Mensaje de error si ocurrió alguno
 */
data class PaginationState<T>(
    val items: List<T> = emptyList(),
    val currentPage: Int = 0,
    val pageSize: Int = 50,
    val totalFetched: Int = 0,
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null
) {
    val totalPages: Int
        get() = if (totalFetched == 0) 1 else (totalFetched + pageSize - 1) / pageSize

    val canLoadNext: Boolean
        get() = !isLoading && hasMore

    val isEmpty: Boolean
        get() = items.isEmpty() && totalFetched == 0
}

/**
 * Resultado de operación de caché
 */
sealed class CacheResult<T> {
    data class Hit<T>(val data: T) : CacheResult<T>()
    class Miss<T> : CacheResult<T>()
    data class Expired<T>(val data: T? = null) : CacheResult<T>()
}

