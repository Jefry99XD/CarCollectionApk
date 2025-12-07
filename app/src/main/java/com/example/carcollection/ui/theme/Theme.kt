package com.example.carcollection.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Cyan80,              // Cyan claro para tema oscuro
    secondary = CyanGrey80,        // Cyan grisáceo para secundario
    tertiary = Teal80,             // Teal para terciario
    background = SurfaceDark,      // Fondo oscuro
    surface = DarkGrey,            // Superficie oscura
    error = Error,                 // Rojo para errores
    onPrimary = DarkGrey,          // Texto sobre primary
    onSecondary = DarkGrey,        // Texto sobre secondary
    onTertiary = DarkGrey,         // Texto sobre tertiary
    onBackground = Cyan80,         // Texto sobre background
    onSurface = Cyan80             // Texto sobre surface
)

private val LightColorScheme = lightColorScheme(
    primary = Cyan40,              // Cyan medio para tema claro
    secondary = CyanGrey40,        // Cyan grisáceo oscuro para secundario
    tertiary = Teal40,             // Teal oscuro para terciario
    background = SurfaceLight,     // Fondo claro
    surface = LightGrey,           // Superficie clara
    error = Error,                 // Rojo para errores
    onPrimary = SurfaceLight,      // Texto blanco sobre primary
    onSecondary = SurfaceLight,    // Texto blanco sobre secondary
    onTertiary = SurfaceLight,     // Texto blanco sobre tertiary
    onBackground = DarkGrey,       // Texto oscuro sobre background
    onSurface = DarkGrey           // Texto oscuro sobre surface
)

@Composable
fun CarCollectionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}