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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.carcollection.featurecar.domain.CarViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    achievementViewModel: AchievementViewModel,
    carViewModel: CarViewModel,
    onBackClick: () -> Unit
) {
    val achievements by achievementViewModel.achievements.collectAsState()
    val isLoading by achievementViewModel.isLoading.collectAsState()
    val errorMessage by achievementViewModel.errorMessage.collectAsState()
    val userCars by carViewModel.cars.collectAsState()

    // Normalizar nombres de carros del usuario (igual que en AchievementMethods)
    val userCarNames = remember(userCars) {
        userCars
            .mapNotNull { car ->
                car.name
                    ?.lowercase()
                    ?.trim()
                    ?.replace("\\s+".toRegex(), " ")
            }
            .toSet()
    }

    // Cargar carros y logros al inicio (asegura datos frescos)
    LaunchedEffect(Unit) {
        carViewModel.loadUserCars() // Recargar carros primero
        achievementViewModel.fetchAchievements() // Luego cargar logros
    }

    // Recargar logros cuando cambia la colección de carros
    LaunchedEffect(userCars.size) {
        if (userCars.isNotEmpty()) {
            achievementViewModel.fetchAchievements()
        }
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
            modifier = Modifier.padding(innerPadding),
            userCarNames = userCarNames
        )
    }
}