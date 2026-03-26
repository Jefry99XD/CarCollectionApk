package com.example.carcollection.featureuser.data

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import java.io.File

/**
 * Cache de avatares de usuario con soporte para:
 * - Caché en memoria (LRU)
 * - Caché en disco
 * - Redimensionamiento automático
 */
object AvatarCache {
    private const val CACHE_DIR_NAME = "user_avatars"
    private const val MAX_CACHE_SIZE = 50 * 1024 * 1024  // 50 MB

    /**
     * Obtener directorio de caché para avatares
     */
    fun getCacheDir(context: Context): File {
        val cacheDir = File(context.cacheDir, CACHE_DIR_NAME)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir
    }

    /**
     * Generar nombre de archivo de caché basado en URL
     */
    fun getCacheFileName(avatarUrl: String): String {
        return avatarUrl.hashCode().toString() + ".jpg"
    }

    /**
     * Limpiar caché si excede el tamaño máximo
     */
    fun cleanupCacheIfNeeded(context: Context) {
        val cacheDir = getCacheDir(context)
        val cacheSize = cacheDir.listFiles()?.sumOf { it.length() } ?: 0

        if (cacheSize > MAX_CACHE_SIZE) {
            SecureLogger.info("Cleaning up avatar cache (size: $cacheSize bytes)")
            // Eliminar archivos más antiguos primero
            cacheDir.listFiles()
                ?.sortedBy { it.lastModified() }
                ?.forEach { it.delete() }
        }
    }

    /**
     * Limpiar todo el caché de avatares
     */
    fun clearAllCache(context: Context) {
        val cacheDir = getCacheDir(context)
        cacheDir.listFiles()?.forEach { it.delete() }
        SecureLogger.success("Avatar cache cleared")
    }
}

/**
 * Composable para mostrar avatar en caché
 * Redimensiona automáticamente y usa caché de Coil
 */
@Composable
fun CachedAvatar(
    avatarUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Int = 120,  // Tamaño en pixels
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current

    // Limpiar caché si es necesario (sin bloquear UI)
    androidx.compose.runtime.LaunchedEffect(Unit) {
        AvatarCache.cleanupCacheIfNeeded(context)
    }

    if (avatarUrl.isNullOrBlank()) {
        // Placeholder cuando no hay URL
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(0.5f),
                color = MaterialTheme.colorScheme.primary
            )
        }
        return
    }

    // 🔹 AsyncImage con caché optimizado
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(avatarUrl)
            // 🔹 Redimensionar a tamaño de pantalla para ahorrar memoria
            .size(
                width = (size * 2),
                height = (size * 2)
            )
            // 🔹 Caché de Coil (automático)
            .crossfade(300)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        onLoading = {
            // UI de carga si es necesario
        }
    )
}

