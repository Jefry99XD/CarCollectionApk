package com.example.carcollection.featureAchievements.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.carcollection.featureAchievements.domain.AchievementGlobal
import com.example.carcollection.featureAchievements.domain.UserAchievement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement


@Composable
fun AchievementList(
    achievements: List<Pair<AchievementGlobal, UserAchievement?>>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    userCarNames: Set<String> = emptySet()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showOnlyUnlocked by remember { mutableStateOf(false) }
    var showOnlyLocked by remember { mutableStateOf(false) }

    // Filter achievements based on search and filters
    val filteredAchievements = remember(achievements, searchQuery, showOnlyUnlocked, showOnlyLocked) {
        achievements.filter { (achievement, progress) ->
            val matchesSearch = searchQuery.isEmpty() ||
                achievement.title.contains(searchQuery, ignoreCase = true) ||
                achievement.description.contains(searchQuery, ignoreCase = true)

            val isUnlocked = progress?.unlocked == true
            val matchesFilter = when {
                showOnlyUnlocked -> isUnlocked
                showOnlyLocked -> !isUnlocked
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    when {
        isLoading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        errorMessage != null -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Error: $errorMessage", color = Color.Red)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRetry) {
                        Text("Reintentar")
                    }
                }
            }
        }

        achievements.isEmpty() -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay logros disponibles.",
                    color = Color.Gray
                )
            }
        }

        else -> {
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Buscar logros...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Limpiar búsqueda"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                // Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = showOnlyUnlocked,
                        onClick = {
                            showOnlyUnlocked = !showOnlyUnlocked
                            if (showOnlyUnlocked) showOnlyLocked = false
                        },
                        label = { Text("Desbloqueados") }
                    )
                    FilterChip(
                        selected = showOnlyLocked,
                        onClick = {
                            showOnlyLocked = !showOnlyLocked
                            if (showOnlyLocked) showOnlyUnlocked = false
                        },
                        label = { Text("Bloqueados") }
                    )
                }

                // Achievement count
                Text(
                    text = "Mostrando ${filteredAchievements.size} de ${achievements.size} logros",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                // Achievement List
                if (filteredAchievements.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron logros con los filtros aplicados",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item { Spacer(Modifier.height(4.dp)) }

                        items(filteredAchievements) { (achievement, progress) ->
                            AchievementItem(
                                achievement = achievement,
                                userAchievement = progress,
                                userCarNames = userCarNames
                            )
                        }

                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}