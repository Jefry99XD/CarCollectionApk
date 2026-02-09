package com.example.carcollection.featureAchievements.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.HorizontalDivider
import coil.compose.AsyncImage
import com.example.carcollection.featureAchievements.domain.AchievementGlobal
import com.example.carcollection.featureAchievements.domain.UserAchievement


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AchievementItem(
    achievement: AchievementGlobal,
    userAchievement: UserAchievement?,
    modifier: Modifier = Modifier,
    userCarNames: Set<String> = emptySet()
) {
    val isUnlocked = userAchievement?.unlocked == true
    val progress = userAchievement?.progress ?: 0
    val goal = achievement.goal
    val progressPercent = (progress.toFloat() / goal).coerceIn(0f, 1f)

    var isExpanded by remember { mutableStateOf(false) }
    val hasLongDescription = achievement.description.length > 80

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen del logro
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = achievement.iconUrl,
                        contentDescription = achievement.title,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(12.dp))

                // Info del logro
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = achievement.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (hasLongDescription) {
                            IconButton(
                                onClick = { isExpanded = !isExpanded },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }


                    Spacer(Modifier.height(6.dp))

                    if (!isUnlocked) {
                        LinearProgressIndicator(
                            progress = { progressPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$progress / $goal",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    } else {
                        Text(
                            text = "✅ Desbloqueado",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Show items list if there's a single condition with a concept that could be a comma-separated list
            // This is for achievements that require collecting specific named items
            val firstCondition = achievement.conditions.firstOrNull()
            if (firstCondition != null && firstCondition.concept.contains(",")) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.Gray.copy(alpha = 0.3f)
                )

                val itemsList = firstCondition.concept
                    .lowercase()
                    .split(",")
                    .map { it.trim().replace("\\s+".toRegex(), " ") }
                    .filter { it.isNotEmpty() }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Carros requeridos (${itemsList.size})",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (isExpanded) {
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsList.forEach { requiredName ->
                            val hasItem = userCarNames.contains(requiredName)
                            Text(
                                text = "• $requiredName",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasItem) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (hasItem) TextDecoration.LineThrough else null,
                                fontWeight = if (hasItem) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}