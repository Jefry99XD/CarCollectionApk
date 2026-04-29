package com.example.carcollection.featureAchievements.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featureAchievements.data.AchievementMethods
import com.example.carcollection.featureAchievements.domain.AchievementGlobal
import com.example.carcollection.featureAchievements.domain.UserAchievement
import com.example.carcollection.featureuser.data.UserMethods
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AchievementViewModel : ViewModel() {

    private val methods = AchievementMethods()
    private val userMethods = UserMethods()

    private val _achievements =
        MutableStateFlow<List<Pair<AchievementGlobal, UserAchievement?>>>(emptyList())
    val achievements: StateFlow<List<Pair<AchievementGlobal, UserAchievement?>>> = _achievements

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // ─────────────────────────────────────────────
    // Obtener todos los logros globales
    // ─────────────────────────────────────────────
    fun fetchAchievements() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _achievements.value = methods.getAllAchievements()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─────────────────────────────────────────────
    // Agregar o actualizar logro
    // ─────────────────────────────────────────────
    fun addOrUpdateGlobalAchievement(achievement: AchievementGlobal) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                methods.addOrUpdateGlobalAchievement(achievement)
                fetchAchievements()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─────────────────────────────────────────────
    // Borrar logro
    // ─────────────────────────────────────────────
    fun deleteAchievement(achievementId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                methods.deleteGlobalAchievement(achievementId)
                fetchAchievements()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun addGlobalAchievement(achievementGlobal: AchievementGlobal) {
        viewModelScope.launch {

            _isLoading.value = true

            try {
                methods.addOrUpdateGlobalAchievement(achievementGlobal)
                fetchAchievements()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─────────────────────────────────────────────
    // Obtener logro por ID
    // ─────────────────────────────────────────────
    suspend fun getAchievementById(achievementId: String): AchievementGlobal? {
        return try {
            methods.getAchievementById(achievementId)
        } catch (e: Exception) {
            _errorMessage.value = e.message
            null
        }
    }

    // ─────────────────────────────────────────────
    // Actualizar logro existente
    // ─────────────────────────────────────────────
    fun updateGlobalAchievement(achievement: AchievementGlobal) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                methods.addOrUpdateGlobalAchievement(achievement)
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
                val data = methods.getPublicUserAchievements(userId)
                _achievements.value = data
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ─────────────────────────────────────────────
    // Obtener lista de usuarios para dropdown
    // ─────────────────────────────────────────────
    suspend fun getAllUsers(): List<Pair<String, String>> {
        return try {
            userMethods.getAllUsers().getOrNull() ?: emptyList()
        } catch (e: Exception) {
            _errorMessage.value = e.message
            emptyList()
        }
    }
}
