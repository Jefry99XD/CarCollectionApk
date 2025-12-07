package com.example.carcollection.presentation.consultas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featurecar.presentation.add_edit_car.CarLibraryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.client.call.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class CarLibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val _allCars = MutableStateFlow<List<CarLibraryEntry>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _paginatedCars = MutableStateFlow<List<CarLibraryEntry>>(emptyList())
    val paginatedCars: StateFlow<List<CarLibraryEntry>> = _paginatedCars

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedCar = MutableStateFlow<CarLibraryEntry?>(null)
    val selectedCar: StateFlow<CarLibraryEntry?> = _selectedCar

    private val itemsPerPage = 20

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            gson()
        }
    }

    private val cacheFile = File(application.filesDir, "car_library_cache.json.gz")
    private val versionFile = File(application.filesDir, "car_library_version.txt")
    private val currentVersion = "1.0" // Incrementa esto cuando quieras forzar actualización

    init {
        loadCarsWithCache()
    }

    private fun loadCarsWithCache() {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Intentar cargar desde caché primero
                val cachedData = loadFromCache()
                if (cachedData != null) {
                    println("📦 Cargando desde caché (${cachedData.size} items)")
                    _allCars.value = cachedData
                    updatePagination()
                    _isLoading.value = false

                    // Actualizar en segundo plano si es necesario
                    checkAndUpdateCache()
                } else {
                    // No hay caché, descargar
                    println("🌐 Descargando desde web...")
                    downloadAndCache()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadFromCache(): List<CarLibraryEntry>? = withContext(Dispatchers.IO) {
        try {
            if (!cacheFile.exists()) return@withContext null

            val cachedVersion = if (versionFile.exists()) {
                versionFile.readText()
            } else {
                ""
            }

            // Si la versión no coincide, invalidar caché
            if (cachedVersion != currentVersion) {
                println("🔄 Versión de caché obsoleta")
                return@withContext null
            }

            // Leer y descomprimir
            FileInputStream(cacheFile).use { fis ->
                GZIPInputStream(fis).use { gzis ->
                    val json = gzis.bufferedReader().readText()
                    val gson = Gson()
                    val type = object : TypeToken<List<CarLibraryEntry>>() {}.type
                    gson.fromJson<List<CarLibraryEntry>>(json, type)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun saveToCache(data: List<CarLibraryEntry>) = withContext(Dispatchers.IO) {
        try {
            val gson = Gson()
            val json = gson.toJson(data)

            // Comprimir y guardar
            FileOutputStream(cacheFile).use { fos ->
                GZIPOutputStream(fos).use { gzos ->
                    gzos.bufferedWriter().use { writer ->
                        writer.write(json)
                    }
                }
            }

            // Guardar versión
            versionFile.writeText(currentVersion)

            val originalSize = json.length / 1024 / 1024
            val compressedSize = cacheFile.length() / 1024 / 1024
            println("💾 Caché guardado: ${originalSize}MB → ${compressedSize}MB (compresión: ${100 - (compressedSize * 100 / originalSize)}%)")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun downloadAndCache() {
        try {
            _isLoading.value = true
            val url = "https://raw.githubusercontent.com/Jefry99XD/CarCollectionApk/refs/heads/main/app/src/main/assets/diecast_images.json"

            val response = client.get(url)
            val json: String = response.body()

            val gson = Gson()
            val carLibraryEntries = withContext(Dispatchers.Default) {
                try {
                    val typeArray = object : TypeToken<List<CarLibraryEntry>>() {}.type
                    gson.fromJson(json, typeArray)
                } catch (_: Exception) {
                    val typeSingle = object : TypeToken<CarLibraryEntry>() {}.type
                    val singleEntry = gson.fromJson<CarLibraryEntry>(json, typeSingle)
                    listOf(singleEntry)
                }
            }

            _allCars.value = carLibraryEntries
            updatePagination()

            // Guardar en caché
            saveToCache(carLibraryEntries)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    private suspend fun checkAndUpdateCache() {
        // Verificar si el caché tiene más de 24 horas
        val cacheAge = System.currentTimeMillis() - cacheFile.lastModified()
        val oneDayInMillis = 24 * 60 * 60 * 1000

        if (cacheAge > oneDayInMillis) {
            println("🔄 Caché antiguo, actualizando en segundo plano...")
            withContext(Dispatchers.IO) {
                try {
                    downloadAndCache()
                } catch (e: Exception) {
                    println("⚠️ Error al actualizar caché: ${e.message}")
                }
            }
        }
    }

    fun forceRefresh() {
        viewModelScope.launch {
            println("🔄 Forzando actualización...")
            downloadAndCache()
        }
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                cacheFile.delete()
                versionFile.delete()
                println("🗑️ Caché eliminado")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectCar(car: CarLibraryEntry) {
        _selectedCar.value = car
    }

    fun clearSelection() {
        _selectedCar.value = null
    }


    fun updateSearch(query: String) {
        _searchQuery.value = query
        _currentPage.value = 0
        updatePagination()
    }

    fun nextPage() {
        if ((_currentPage.value + 1) * itemsPerPage < filteredCars().size) {
            _currentPage.value++
            updatePagination()
        }
    }

    fun prevPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
            updatePagination()
        }
    }

    private fun filteredCars(): List<CarLibraryEntry> {
        return if (_searchQuery.value.isBlank()) {
            _allCars.value
        } else {
            _allCars.value.filter {
                it.name?.contains(_searchQuery.value, ignoreCase = true) == true
            }
        }
    }

    private fun updatePagination() {
        val filtered = filteredCars()
        val start = _currentPage.value * itemsPerPage
        val end = minOf(start + itemsPerPage, filtered.size)
        _paginatedCars.value = if (filtered.isEmpty()) emptyList() else filtered.subList(start, end)
    }

    fun getTotalPages(): Int {
        val filtered = filteredCars()
        return if (filtered.isEmpty()) 0 else (filtered.size + itemsPerPage - 1) / itemsPerPage
    }
}
