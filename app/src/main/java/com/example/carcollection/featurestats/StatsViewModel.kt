package com.example.carcollection.featurestats

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featurecar.data.CarMethods
import com.example.carcollection.featurecar.domain.CarStatsData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

// ─────────────────────────────────────────────────────────────────────────────
// MEJORA 5 – Datos de comparación con otros usuarios
// ─────────────────────────────────────────────────────────────────────────────
data class ComparisonStats(
    val myCarCount: Int,
    val totalUsers: Int,
    /** 0–100: "tienes más carros que el X% de usuarios" */
    val percentile: Int,
    val averageCarsPerUser: Int
)

class StatsViewModel(
    private val carMethods: CarMethods
) : ViewModel() {

    // ─── MEJORA 1: datos ligeros (sin photoUrl / backgroundUrl) ──────────────
    private val _statsData = MutableStateFlow<List<CarStatsData>>(emptyList())
    val statsData: StateFlow<List<CarStatsData>> = _statsData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // ─── MEJORA 2: caché de stats calculadas por categoría ───────────────────
    private val statsCache = mutableMapOf<StatsCategory, List<StatItem>>()

    /** Invalida la caché; llamar cuando se agreguen/eliminen carros */
    fun invalidateStatsCache() {
        statsCache.clear()
        _statsData.value = emptyList()
    }

    // ─── MEJORA 5: comparación con otros usuarios ────────────────────────────
    private val _comparisonStats = MutableStateFlow<ComparisonStats?>(null)
    val comparisonStats: StateFlow<ComparisonStats?> = _comparisonStats

    private val _comparisonLoading = MutableStateFlow(false)
    val comparisonLoading: StateFlow<Boolean> = _comparisonLoading

    init {
        loadCarsForStats()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MEJORA 1 + 2: Carga ligera y caché de resultados
    // ─────────────────────────────────────────────────────────────────────────

    fun loadCarsForStats(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            if (forceRefresh) statsCache.clear()
            val result = carMethods.getUserCarsForStats(forceRefresh)
            _statsData.value = result.getOrDefault(emptyList())
            _isLoading.value = false
        }
    }

    /** Alias de compatibilidad (usado antes). Delega a loadCarsForStats(). */
    fun loadCars() = loadCarsForStats()

    /**
     * Genera (o recupera de caché) las estadísticas para una categoría.
     * La caché se invalida automáticamente cuando cambia _statsData.
     */
    fun generateStats(category: StatsCategory): List<StatItem> {
        val data = _statsData.value
        if (data.isEmpty()) return emptyList()

        // Retornar de caché si existe
        statsCache[category]?.let { return it }

        val result = when (category) {
            StatsCategory.BRAND     -> generateBrandStats(data)
            StatsCategory.YEAR      -> generateYearStats(data)
            StatsCategory.COLOR     -> generateColorStats(data)
            StatsCategory.TYPE      -> generateTypeStats(data)
            StatsCategory.QUALITY   -> generateQualityStats(data)
            StatsCategory.TAGS      -> generateTagStats(data)
            StatsCategory.CREATED_AT -> generateCreatedAtStats(data)
        }

        statsCache[category] = result
        return result
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MEJORA 5: Comparación con otros usuarios
    // ─────────────────────────────────────────────────────────────────────────

    fun loadComparisonStats() {
        viewModelScope.launch {
            _comparisonLoading.value = true
            val result = carMethods.getUserComparisonStats()
            _comparisonStats.value = result.getOrNull()
            _comparisonLoading.value = false
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Generadores de stats (ahora reciben CarStatsData en lugar de Car)
    // ─────────────────────────────────────────────────────────────────────────

    private fun generateBrandStats(data: List<CarStatsData>): List<StatItem> =
        data.mapNotNull { it.brand }
            .filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { (brand, count) -> StatItem(brand, count, randomColor()) }

    private fun generateYearStats(data: List<CarStatsData>): List<StatItem> =
        data.mapNotNull { it.year }
            .filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { (year, count) -> StatItem(year, count, randomColor()) }

    private fun generateColorStats(data: List<CarStatsData>): List<StatItem> =
        data.mapNotNull { it.color }
            .filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { (color, count) -> StatItem(color, count, randomColor()) }

    private fun generateTypeStats(data: List<CarStatsData>): List<StatItem> =
        data.mapNotNull { it.type }
            .filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { (type, count) -> StatItem(type, count, randomColor()) }

    private fun generateQualityStats(data: List<CarStatsData>): List<StatItem> =
        data.mapNotNull { it.quality }
            .filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { (quality, count) -> StatItem(quality, count, randomColor()) }

    private fun generateTagStats(data: List<CarStatsData>): List<StatItem> =
        data.flatMap { it.tags }
            .filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { (tag, count) -> StatItem(tag, count, randomColor()) }

    private fun generateCreatedAtStats(data: List<CarStatsData>): List<StatItem> {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return data.mapNotNull { it.createdAt }
            .groupingBy { ts -> dateFormat.format(Date(ts)) }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { (month, count) -> StatItem(month, count, randomColor()) }
    }

    private fun randomColor(): Color = Color(
        Random.nextInt(100, 200),
        Random.nextInt(100, 200),
        Random.nextInt(100, 200)
    )
}

// ViewModelFactory for StatsViewModel
class StatsViewModelFactory(
    private val carMethods: CarMethods
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            return StatsViewModel(carMethods) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
