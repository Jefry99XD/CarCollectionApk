package com.example.carcollection.featureAchievements.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    achievementViewModel: AchievementViewModel,
    onBackClick: () -> Unit
) {
    val achievements by achievementViewModel.achievements.collectAsState()
    val isLoading by achievementViewModel.isLoading.collectAsState()
    val errorMessage by achievementViewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        achievementViewModel.fetchAchievements()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logros") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        AchievementList(
            achievements = achievements,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onRetry = { achievementViewModel.fetchAchievements() },
            modifier = Modifier.padding(innerPadding)
        )
    }
}