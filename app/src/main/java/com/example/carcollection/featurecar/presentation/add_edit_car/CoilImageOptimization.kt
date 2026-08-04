package com.example.carcollection.featurecar.presentation.add_edit_car

import android.content.Context
import coil.Coil
import coil.annotation.ExperimentalCoilApi

/**
 * Configuración de Coil para featurecar
 * Coil maneja automáticamente:
 * - Caché de memoria (LRU)
 * - Caché de disco (~50 MB default)
 * - Decodificación optimizada
 */

/**
 * Información del caché actual
 * Para debug y monitoreo de memoria
 */
data class CacheStats(
    val memoryCacheSizeMB: Float,
    val diskCacheSizeMB: Float,
    val totalCacheMB: Float
)

/**
 * Limpiar caché de Coil
 * Usar con cuidado - solo en situaciones de memoria baja
 */
@OptIn(ExperimentalCoilApi::class)
suspend fun clearCoilCache(context: Context) {
    try {
        val imageLoader = Coil.imageLoader(context)
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


