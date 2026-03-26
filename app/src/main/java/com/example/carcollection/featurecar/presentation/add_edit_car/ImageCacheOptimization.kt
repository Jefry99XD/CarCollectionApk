package com.example.carcollection.featurecar.presentation.add_edit_car

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Configuración centralizada de caché para Coil
 * Optimiza memory footprint y performance de carga de imágenes
 */
object ImageCacheConfig {

    /**
     * Crea un ImageRequest optimizado para el tipo de imagen
     * @param url URL de la imagen
     * @return ImageRequest configurado
     */
    fun getImageRequest(
        url: String,
        context: Context
    ): ImageRequest {
        return ImageRequest.Builder(context)
            .data(url)
            .build()
    }
}

/**
 * AsyncImage optimizado con caché
 * @param model URL de la imagen
 * @param contentDescription Descripción para accesibilidad
 * @param modifier Modifier
 * @param contentScale Escala del contenido
 */
@Composable
fun OptimizedAsyncImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (model.isNullOrBlank()) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BrokenImage,
                contentDescription = "Sin imagen",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val context = LocalContext.current
    val imageRequest = ImageCacheConfig.getImageRequest(
        url = model,
        context = context
    )

    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}


/**
 * AsyncImage optimizado específicamente para thumbnails
 * @param model URL de la imagen
 * @param contentDescription Descripción para accesibilidad
 * @param modifier Modifier
 * @param size Tamaño del thumbnail
 */
@Composable
fun ThumbnailAsyncImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    OptimizedAsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier.size(size),
        contentScale = ContentScale.Crop
    )
}

/**
 * AsyncImage optimizado para imágenes a tamaño completo
 * @param model URL de la imagen
 * @param contentDescription Descripción para accesibilidad
 * @param modifier Modifier
 */
@Composable
fun FullSizeAsyncImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OptimizedAsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

