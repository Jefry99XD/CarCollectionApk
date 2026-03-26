package com.example.carcollection.featurecar.presentation.add_edit_car

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tamaños de pantalla disponibles
 */
enum class ScreenSize {
    SMALL,    // < 360dp (phones viejos)
    MEDIUM,   // 360-600dp (phones normales)
    LARGE,    // 600-840dp (tablets 7")
    XLARGE    // > 840dp (tablets 10"+, landscape)
}

/**
 * Métricas de pantalla para diseño responsivo
 */
data class ScreenMetrics(
    val width: Dp,
    val height: Dp,
    val isTablet: Boolean = width > 600.dp,
    val isLandscape: Boolean = width > height,
    val size: ScreenSize = when {
        width < 360.dp -> ScreenSize.SMALL
        width < 600.dp -> ScreenSize.MEDIUM
        width < 840.dp -> ScreenSize.LARGE
        else -> ScreenSize.XLARGE
    }
) {
    // Útiles para layout responsivo
    val contentMaxWidth: Dp
        get() = when (size) {
            ScreenSize.SMALL -> width * 0.95f
            ScreenSize.MEDIUM -> width * 0.9f
            ScreenSize.LARGE -> minOf(width * 0.8f, 600.dp)
            ScreenSize.XLARGE -> minOf(width * 0.7f, 900.dp)
        }

    val columnCount: Int
        get() = when {
            isLandscape && isTablet -> 3
            isTablet -> 2
            else -> 1
        }

    val padding: Dp
        get() = when (size) {
            ScreenSize.SMALL -> 8.dp
            ScreenSize.MEDIUM -> 12.dp
            ScreenSize.LARGE -> 16.dp
            ScreenSize.XLARGE -> 24.dp
        }

    val cornerRadius: Dp
        get() = when (size) {
            ScreenSize.SMALL -> 4.dp
            ScreenSize.MEDIUM -> 8.dp
            ScreenSize.LARGE -> 12.dp
            ScreenSize.XLARGE -> 16.dp
        }
}

/**
 * Composable para obtener métricas de pantalla actual
 */
@Composable
fun rememberScreenMetrics(): ScreenMetrics {
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        ScreenMetrics(
            width = configuration.screenWidthDp.dp,
            height = configuration.screenHeightDp.dp
        )
    }
}

/**
 * Extensión para Modifier responsivo - padding
 */
fun Modifier.responsivePadding(
    metrics: ScreenMetrics
): Modifier =
    this.then(
        padding(metrics.padding)
    )

/**
 * Extensión para Modifier responsivo - ancho
 */
fun Modifier.responsiveWidth(
    metrics: ScreenMetrics
): Modifier =
    this.then(
        fillMaxWidth(
            when {
                metrics.isTablet && metrics.isLandscape -> 0.45f
                metrics.isTablet -> 0.9f
                else -> 0.95f
            }
        )
    )

/**
 * Extensión para Modifier responsivo - altura
 */
fun Modifier.responsiveHeight(
    metrics: ScreenMetrics,
    percentOfScreen: Float = 0.5f
): Modifier {
    val heightValue = metrics.height * percentOfScreen
    return this.then(
        height(heightValue)
    )
}

