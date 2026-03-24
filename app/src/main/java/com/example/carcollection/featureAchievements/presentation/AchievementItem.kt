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
import com.example.carcollection.featureAchievements.domain.AchievementRarity
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
            containerColor = when (achievement.rarity) {
                AchievementRarity.COMUN -> Color(0xFF90CAF9).copy(alpha = 0.3f) // Azul
                AchievementRarity.RARO -> Color(0xFFBA68C8).copy(alpha = 0.3f)  // Morado
                AchievementRarity.LEGENDARIO -> Color(0xFFFFD54F).copy(alpha = 0.3f) // Dorado
                AchievementRarity.SPECIAL -> Color(0xFFFFC107).copy(alpha = 0.4f) // Dorado más brillante
            }
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

            // Show items list for OR achievements (list-based achievements)
            // Each condition represents a required item/concept
            if (achievement.rules.conditionLogic == com.example.carcollection.featureAchievements.domain.ConditionLogic.OR
                && achievement.conditions.size > 1
                && achievement.conditions.all { it.concept.isNotEmpty() }) {

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = Color.Gray.copy(alpha = 0.3f)
                )

                val matchedIndices = userAchievement?.matchedConditionIndices?.toSet() ?: emptySet()
                val completedCount = matchedIndices.size
                val totalCount = achievement.conditions.size

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Items requeridos ($completedCount / $totalCount)",
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
                        achievement.conditions.forEachIndexed { index, condition ->
                            val isCompleted = index in matchedIndices
                            val displayName = condition.concept.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            }

                            Text(
                                text = if (isCompleted) "✅ $displayName" else "• $displayName",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                                fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
            // Fallback: Show old format if single condition with comma-separated list
            else if (achievement.conditions.size == 1) {
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
}