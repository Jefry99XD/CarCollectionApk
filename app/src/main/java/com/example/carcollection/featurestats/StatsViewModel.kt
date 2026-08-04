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

// ─────────────────────────────────────────────────────────────────────────────
// TREND METRICS – Datos de crecimiento y tendencias
// ─────────────────────────────────────────────────────────────────────────────
data class GrowthMetrics(
    val growthRate: Float,          // Porcentaje de crecimiento respecto al mes anterior
    val averagePerMonth: Float,     // Promedio de carros añadidos por mes
    val totalCars: Int,             // Total de carros en la colección
    val trendMonths: Int            // Cantidad de meses con datos
)

class StatsViewModel(
    private val carMethods: CarMethods
) : ViewModel() {

    // ─── MEJORA 1: datos ligeros (sin photoUrl / backgroundUrl) ──────────────
    private val _statsData = MutableStateFlow<List<CarStatsData>>(emptyList())
    val statsData: StateFlow<List<CarStatsData>> = _statsData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // ─── Error state for better UX ──────────────────────────────────────────
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // ─── MEJORA 2: caché de stats calculadas por categoría ───────────────────
    private val statsCache = mutableMapOf<StatsCategory, List<StatItem>>()

    /** Invalida la caché; llamar cuando se agreguen/eliminen carros */
    fun invalidateStatsCache() {
        statsCache.clear()
        _statsData.value = emptyList()
        _errorMessage.value = null
    }

    // ─── MEJORA 5: comparación con otros usuarios ────────────────────────────
    private val _comparisonStats = MutableStateFlow<ComparisonStats?>(null)
    val comparisonStats: StateFlow<ComparisonStats?> = _comparisonStats

    private val _comparisonLoading = MutableStateFlow(false)
    val comparisonLoading: StateFlow<Boolean> = _comparisonLoading

    private val _comparisonError = MutableStateFlow<String?>(null)
    val comparisonError: StateFlow<String?> = _comparisonError

    init {
        loadCarsForStats()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MEJORA 1 + 2: Carga ligera y caché de resultados
    // ─────────────────────────────────────────────────────────────────────────

    fun loadCarsForStats(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            if (forceRefresh) statsCache.clear()
            val result = carMethods.getUserCarsForStats(forceRefresh)
            result.onSuccess { data ->
                _statsData.value = data
                _errorMessage.value = null
            }.onFailure { error ->
                _statsData.value = emptyList()
                _errorMessage.value = error.message ?: "Error loading stats"
            }
            _isLoading.value = false
        }
    }

    /** Alias de compatibilidad (usado antes). Delega a loadCarsForStats(). */
    fun loadCars() = loadCarsForStats()

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Genera (o recupera de caché) las estadísticas para una categoría.
     * La caché se invalida automáticamente cuando cambia _statsData.
     * Opcionalmente filtra por rango de tiempo.
     */
    fun generateStats(category: StatsCategory, timeRange: TimeRange = TimeRange.ALL_TIME): List<StatItem> {
        val data = _statsData.value
        if (data.isEmpty()) return emptyList()

        val filteredData = filterDataByTimeRange(data, timeRange)
        if (filteredData.isEmpty()) return emptyList()

        // Generar key de caché que incluya el timeRange
        val cacheKey = "$category-$timeRange"
        
        val result = when (category) {
            StatsCategory.BRAND     -> generateBrandStats(filteredData)
            StatsCategory.YEAR      -> generateYearStats(filteredData)
            StatsCategory.COLOR     -> generateColorStats(filteredData)
            StatsCategory.TYPE      -> generateTypeStats(filteredData)
            StatsCategory.QUALITY   -> generateQualityStats(filteredData)
            StatsCategory.TAGS      -> generateTagStats(filteredData)
            StatsCategory.CREATED_AT -> generateCreatedAtStats(filteredData)
        }

        return result
    }

    /**
     * Filtra datos de carros por rango de tiempo basado en createdAt.
     */
    private fun filterDataByTimeRange(data: List<CarStatsData>, timeRange: TimeRange): List<CarStatsData> {
        if (timeRange == TimeRange.ALL_TIME) return data
        
        val days = timeRange.days ?: return data
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        
        return data.filter { car ->
            car.createdAt != null && car.createdAt >= cutoffTime
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TREND ANALYSIS: Análisis de tendencias
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Get monthly trend data showing cars added per month
     */
    fun getMonthlyTrend(): List<TrendItem> {
        return TrendAnalyzer.generateMonthlyTrend(_statsData.value)
    }

    /**
     * Get quality trend data showing distribution over time
     */
    fun getQualityTrend(): Map<String, List<TrendItem>> {
        return TrendAnalyzer.generateQualityTrend(_statsData.value)
    }

    /**
     * Get growth rate information
     */
    fun getGrowthMetrics(): GrowthMetrics {
        val trend = getMonthlyTrend()
        val growthRate = TrendAnalyzer.calculateGrowthRate(trend)
        val avgPerMonth = TrendAnalyzer.getAveragePerMonth(trend)
        val totalAdded = _statsData.value.size
        
        return GrowthMetrics(
            growthRate = growthRate,
            averagePerMonth = avgPerMonth,
            totalCars = totalAdded,
            trendMonths = trend.size
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MEJORA 5: Comparación con otros usuarios
    // ─────────────────────────────────────────────────────────────────────────

    fun loadComparisonStats() {
        viewModelScope.launch {
            _comparisonLoading.value = true
            _comparisonError.value = null
            val result = carMethods.getUserComparisonStats()
            result.onSuccess { stats ->
                _comparisonStats.value = stats
                _comparisonError.value = null
            }.onFailure { error ->
                _comparisonStats.value = null
                _comparisonError.value = error.message ?: "Error loading comparison stats"
            }
            _comparisonLoading.value = false
        }
    }

    fun clearComparisonError() {
        _comparisonError.value = null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Generadores de stats (ahora reciben CarStatsData en lugar de Car)
    // ─────────────────────────────────────────────────────────────────────────

    private fun generateBrandStats(data: List<CarStatsData>): List<StatItem> {
        val stats = data.mapNotNull { it.brand }
            .filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { (brand, count) -> StatItem(brand, count, Color.Unspecified) }
        return assignDeterministicColors(stats)
    }

    private fun generateYearStats(data: List<CarStatsData>): List<StatItem> {
        val stats = data.mapNotNull { it.year }
            .filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { (year, count) -> StatItem(year, count, Color.Unspecified) }
        return assignDeterministicColors(stats)
    }

    private fun generateColorStats(data: List<CarStatsData>): List<StatItem> {
        val stats = data.mapNotNull { it.color }
            .filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { (color, count) -> StatItem(color, count, Color.Unspecified) }
        return assignDeterministicColors(stats)
    }

    private fun generateTypeStats(data: List<CarStatsData>): List<StatItem> {
        val stats = data.mapNotNull { it.type }
            .filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { (type, count) -> StatItem(type, count, Color.Unspecified) }
        return assignDeterministicColors(stats)
    }

    private fun generateQualityStats(data: List<CarStatsData>): List<StatItem> {
        val stats = data.mapNotNull { it.quality }
            .filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { (quality, count) -> StatItem(quality, count, Color.Unspecified) }
        return assignDeterministicColors(stats)
    }

    private fun generateTagStats(data: List<CarStatsData>): List<StatItem> {
        val stats = data.flatMap { it.tags }
            .filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { (tag, count) -> StatItem(tag, count, Color.Unspecified) }
        return assignDeterministicColors(stats)
    }

    private fun generateCreatedAtStats(data: List<CarStatsData>): List<StatItem> {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val stats = data.mapNotNull { it.createdAt }
            .groupingBy { ts -> dateFormat.format(Date(ts)) }.eachCount()
            .entries.sortedByDescending { it.value }
            .map { (month, count) -> StatItem(month, count, Color.Unspecified) }
        return assignDeterministicColors(stats)
    }

    /**
     * Assign deterministic colors to stats based on their labels.
     * Ensures consistent colors across recompositions.
     */
    private fun assignDeterministicColors(stats: List<StatItem>): List<StatItem> {
        val labels = stats.map { it.label }
        val colors = ColorMapper.getColorsForLabels(labels)
        return stats.mapIndexed { index, stat ->
            stat.copy(color = colors.getOrNull(index) ?: ColorMapper.getColorForLabel(stat.label))
        }
    }
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
