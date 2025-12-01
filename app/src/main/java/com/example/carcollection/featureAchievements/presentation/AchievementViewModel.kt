package com.example.carcollection.featureAchievements.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featureAchievements.data.AchievementMethods
import com.example.carcollection.featureAchievements.domain.AchievementGlobal
import com.example.carcollection.featureAchievements.domain.UserAchievement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class AchievementViewModel  : ViewModel(){
    private val achievementMethods: AchievementMethods = AchievementMethods()

    private val _achievements =
        MutableStateFlow<List<Pair<AchievementGlobal, UserAchievement?>>>(emptyList())
    val achievements: StateFlow<List<Pair<AchievementGlobal, UserAchievement?>>> = _achievements

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchAchievements() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = achievementMethods.getAllAchievements()
                _achievements.value = data
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addProgress(achievementId: String, increment: Int = 1) {
        viewModelScope.launch {
            try {
                achievementMethods.incrementProgress(achievementId, increment)
                fetchAchievements()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun unlockAchievement(achievementId: String) {
        viewModelScope.launch {
            try {
                achievementMethods.unlockAchievement(achievementId)
                fetchAchievements()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun addAchievement(achievement: AchievementGlobal) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                achievementMethods.addGlobalAchievement(achievement)
                fetchAchievements()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchPublicUserAchievements(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val data = achievementMethods.getPublicUserAchievements(userId)
                _achievements.value = data
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

}