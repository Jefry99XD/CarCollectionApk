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
import com.example.carcollection.featureAchievements.domain.AchievementCondition
import com.example.carcollection.featurecar.domain.Car

// Helper function para contar carros que coinciden con una condición
private fun countCarsMatchingCondition(
    cars: List<Car>,
    concept: String
): Int {
    if (concept.isBlank()) return cars.size

    return cars.count { car ->
        car.name?.lowercase()?.contains(concept.lowercase()) == true ||
        car.brand?.lowercase()?.contains(concept.lowercase()) == true ||
        car.serie?.lowercase()?.contains(concept.lowercase()) == true ||
        car.type?.lowercase()?.contains(concept.lowercase()) == true ||
        car.quality?.lowercase()?.contains(concept.lowercase()) == true
    }
}

// Helper function para verificar si un carro coincide con una condición
private fun carMatchesConditionForDisplay(
    car: Car,
    condition: AchievementCondition
): Boolean {
    if (condition.concept.isBlank()) return true

    return car.name?.lowercase()?.contains(condition.concept.lowercase()) == true ||
            car.brand?.lowercase()?.contains(condition.concept.lowercase()) == true ||
            car.serie?.lowercase()?.contains(condition.concept.lowercase()) == true ||
            car.type?.lowercase()?.contains(condition.concept.lowercase()) == true ||
            car.quality?.lowercase()?.contains(condition.concept.lowercase()) == true
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AchievementItem(
    achievement: AchievementGlobal,
    userAchievement: UserAchievement?,
    modifier: Modifier = Modifier,
    userCars: List<Car> = emptyList(),
    userCarNames: Set<String> = emptySet(),
    currentUserId: String? = null,
    profileUsername: String? = null  // null = perfil propio, valor = perfil público
) {
    val isUnlocked = userAchievement?.unlocked == true
    val progress = userAchievement?.progress ?: 0
    val goal = achievement.goal
    // Evitar división por cero (logros exclusivos tienen goal = 0)
    val progressPercent = if (goal > 0) (progress.toFloat() / goal).coerceIn(0f, 1f) else if (isUnlocked) 1f else 0f
    val isExclusiveAchievement = achievement.isExclusive || achievement.goal == 0

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

                    if (isExclusiveAchievement) {
                        // Determinar si el usuario actual es el dueño del logro exclusivo
                        val isOwner = currentUserId != null &&
                            achievement.exclusiveUserIds.contains(currentUserId)
                        val ownerName = profileUsername ?: "este usuario"

                        if (isUnlocked && isOwner) {
                            // Perfil propio con logro exclusivo desbloqueado
                            Text(
                                text = "✅ Desbloqueado",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "⭐ Tu logro exclusivo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else if (isUnlocked && !isOwner) {
                            // Perfil público — el dueño tiene este logro
                            Text(
                                text = "✅ Desbloqueado por $ownerName",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "🔒 Solo $ownerName puede tener este logro",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        } else {
                            // Logro exclusivo no desbloqueado (raro, pero mostrar info)
                            Text(
                                text = "⭐ Logro exclusivo de $ownerName",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "🔒 Solo $ownerName puede tener este logro",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                            )
                        }
                    } else if (!isUnlocked) {
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
                        // Para logros de 1 carro bloqueados, mostrar cuál es el carro requerido
                        if (goal == 1) {
                            val concept = achievement.conditions.firstOrNull()?.concept
                            if (!concept.isNullOrBlank() && !concept.contains(",")) {
                                val displayConcept = concept.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase() else it.toString()
                                }
                                Text(
                                    text = "🚗 Se necesita: $displayConcept",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "✅ Desbloqueado",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                        // Mostrar información adicional según el tipo de logro
                        if (achievement.id == "car_of_the_day") {
                            // Para Car of the Day, mostrar cuántas veces ha coincidido
                            Text(
                                text = "🎯 Obtenido $progress veces",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (goal == 1 && userAchievement.countedCarIds.isNotEmpty()) {
                            // Para logros de 1 carro, mostrar cuál es
                            val carId = userAchievement.countedCarIds.firstOrNull()
                            val car = userCars.find { it.id == carId }
                            if (car != null) {
                                Text(
                                    text = "🚗 ${car.name ?: "Desconocido"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (progress > 0 && goal > 1) {
                            // Para logros multi-carro, mostrar el total de carros que cumplen
                            Text(
                                text = "🚗 $progress carros totales",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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

                // Contar condiciones completadas (tanto single como multiple)
                val completedCount = achievement.conditions.count { condition ->
                    if (condition.allowMultiplePerConcept) {
                        // Para condiciones múltiples: contar si hay al menos 1 carro que cuenta
                        userAchievement?.countedCarIds?.any { carId ->
                            val car = userCars.find { it.id == carId }
                            car?.let { carMatchesConditionForDisplay(it, condition) } == true
                        } ?: false
                    } else {
                        // Para condiciones únicas: usar matchedConditionIndices
                        achievement.conditions.indexOf(condition) in matchedIndices
                    }
                }
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
                            // Determinar si la condición está completada
                            val isCompleted = if (condition.allowMultiplePerConcept) {
                                // Para condiciones múltiples: verificar si hay al menos 1 carro que cuenta para esta condición
                                userAchievement?.countedCarIds?.any { carId ->
                                    val car = userCars.find { it.id == carId }
                                    car?.let { carMatchesConditionForDisplay(it, condition) } == true
                                } ?: false
                            } else {
                                // Para condiciones únicas: usar matchedConditionIndices
                                index in matchedIndices
                            }

                            val displayName = condition.concept.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            }

                            // Contar carros que coinciden con esta condición
                            val carCount = if (condition.concept.isNotEmpty()) {
                                countCarsMatchingCondition(userCars, condition.concept)
                            } else {
                                userCars.size
                            }

                            // Mostrar siempre todas las condiciones (incluso si el usuario no tiene el carro)
                            val countText = if (carCount > 0) " ($carCount)" else ""
                            Text(
                                text = if (isCompleted) "✅ $displayName$countText" else "• $displayName$countText",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCompleted) Color(0xFF4CAF50)
                                        else if (carCount == 0) Color.Gray.copy(alpha = 0.5f)
                                        else MaterialTheme.colorScheme.onSurface,
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