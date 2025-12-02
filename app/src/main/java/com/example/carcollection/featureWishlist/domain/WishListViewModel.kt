package com.example.carcollection.featureWishlist.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featureWishlist.data.WishlistMethods
import com.example.carcollection.featurecar.domain.Car
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WishListViewModel(
    private val wishlistMethods: WishlistMethods = WishlistMethods()
) : ViewModel() {

    // Lista de items en wishlist
    private val _wishlist = MutableStateFlow<List<Car>>(emptyList())
    val wishlist: StateFlow<List<Car>> = _wishlist.asStateFlow()

    // Indicador de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mensajes de error / info (UI puede mostrar y luego limpiar con clearMessage)
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        // Cargar inicialmente
        loadWishlist()
    }

    fun loadWishlist() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = wishlistMethods.retrieveWishlist()
                if (result.isSuccess) {
                    _wishlist.value = result.getOrDefault(emptyList())
                } else {
                    _message.value = result.exceptionOrNull()?.message ?: "Failed to load wishlist"
                }
            } catch (e: Exception) {
                _message.value = e.message ?: "Failed to load wishlist"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToWishlist(car: Car) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = wishlistMethods.addToWishlist(car)
                if (result.isSuccess) {
                    // Recargar lista al agregar
                    loadWishlist()
                } else {
                    _message.value = result.exceptionOrNull()?.message ?: "Failed to add to wishlist"
                }
            } catch (e: Exception) {
                _message.value = e.message ?: "Failed to add to wishlist"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeFromWishlist(wishlistItemId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = wishlistMethods.removeFromWishlist(wishlistItemId)
                if (result.isSuccess) {
                    // Recargar lista al eliminar
                    loadWishlist()
                } else {
                    _message.value = result.exceptionOrNull()?.message ?: "Failed to remove from wishlist"
                }
            } catch (e: Exception) {
                _message.value = e.message ?: "Failed to remove from wishlist"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}