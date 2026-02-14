package com.example.carcollection.featureuser.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Badge compacto que muestra el nivel del usuario
 */
@Composable
fun LevelBadge(
    level: Int,
    modifier: Modifier = Modifier,
    size: BadgeSize = BadgeSize.MEDIUM
) {
    val dimensions = when (size) {
        BadgeSize.SMALL -> BadgeDimensions(40.dp, 14.sp, 10.sp)
        BadgeSize.MEDIUM -> BadgeDimensions(56.dp, 18.sp, 12.sp)
        BadgeSize.LARGE -> BadgeDimensions(72.dp, 24.sp, 14.sp)
    }

    Box(
        modifier = modifier
            .size(dimensions.containerSize)
            .clip(CircleShape)
            .background(
                color = getLevelColor(level),
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = level.toString(),
            fontSize = dimensions.numberSize,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

/**
 * Barra de progreso de XP con información detallada
 */
@Composable
fun XPProgressBar(
    currentXP: Long,
    neededXP: Long,
    level: Int,
    modifier: Modifier = Modifier,
    showDetailedInfo: Boolean = true
) {
    val progress = if (neededXP > 0) {
        (currentXP.toFloat() / neededXP.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header con nivel y XP
        if (showDetailedInfo) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LevelBadge(level = level, size = BadgeSize.SMALL)
                    Text(
                        text = "Nivel $level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "$currentXP / $neededXP XP",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Barra de progreso
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = getLevelColor(level),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        // Info adicional
        if (showDetailedInfo) {
            Text(
                text = "Faltan ${neededXP - currentXP} XP para nivel ${level + 1}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Card completo de nivel con estadísticas de XP
 */
@Composable
fun LevelCard(
    level: Int,
    totalXP: Long,
    currentLevelXP: Long,
    xpForNextLevel: Long,
    xpFromCars: Long,
    xpFromAchievements: Long,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header con badge grande
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Nivel $level",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${String.format(Locale.US, "%,d", totalXP)} XP Total",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LevelBadge(level = level, size = BadgeSize.LARGE)
            }

            // Barra de progreso
            XPProgressBar(
                currentXP = currentLevelXP,
                neededXP = xpForNextLevel,
                level = level,
                showDetailedInfo = false
            )

            // Breakdown de XP
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Fuentes de XP",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                XPSourceRow(
                    label = "🚗 Carros agregados",
                    xp = xpFromCars,
                    color = MaterialTheme.colorScheme.primary
                )

                XPSourceRow(
                    label = "🏆 Logros desbloqueados",
                    xp = xpFromAchievements,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun XPSourceRow(
    label: String,
    xp: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "${String.format(Locale.US, "%,d", xp)} XP",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/**
 * Obtener color según el nivel
 */
private fun getLevelColor(level: Int): Color {
    return when {
        level < 5 -> Color(0xFF9E9E9E)      // Gris - Novato
        level < 10 -> Color(0xFF4CAF50)     // Verde - Principiante
        level < 20 -> Color(0xFF2196F3)     // Azul - Intermedio
        level < 35 -> Color(0xFF9C27B0)     // Púrpura - Avanzado
        level < 50 -> Color(0xFFFF9800)     // Naranja - Experto
        level < 75 -> Color(0xFFF44336)     // Rojo - Maestro
        else -> Color(0xFFFFD700)           // Dorado - Leyenda
    }
}

enum class BadgeSize {
    SMALL, MEDIUM, LARGE
}

private data class BadgeDimensions(
    val containerSize: Dp,
    val numberSize: TextUnit,
    val labelSize: TextUnit
)

