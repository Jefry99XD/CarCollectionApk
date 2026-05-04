package com.example.carcollection.featureuser.publicUser

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
import com.example.carcollection.featureAchievements.presentation.AchievementList
import com.example.carcollection.featureAchievements.presentation.AchievementViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicUserAchievements(
    uid: String,
    username: String = "Usuario",
    achievementViewModel: AchievementViewModel,
    onBackClick: () -> Unit
) {
    val achievements by achievementViewModel.achievements.collectAsState()
    val isLoading by achievementViewModel.isLoading.collectAsState()
    val errorMessage by achievementViewModel.errorMessage.collectAsState()

    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }

    // Fetch achievements for the specific user
    LaunchedEffect(uid) {
        achievementViewModel.fetchPublicUserAchievements(uid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logros de $username") },
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
            onRetry = { achievementViewModel.fetchPublicUserAchievements(uid) },
            modifier = Modifier.padding(innerPadding),
            currentUserId = currentUserId,
            profileUsername = username  // perfil público
        )
    }
}