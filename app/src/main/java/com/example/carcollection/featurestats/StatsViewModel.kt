package com.example.carcollection.featurestats

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featurecar.data.CarMethods
import com.example.carcollection.featurecar.domain.Car
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class StatsViewModel(
    private val carMethods: CarMethods
) : ViewModel() {

    private val _cars = MutableStateFlow<List<Car>>(emptyList())
    val cars: StateFlow<List<Car>> = _cars

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadCars()
    }

    fun loadCars() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = carMethods.getUserCars()
            _cars.value = result.getOrDefault(emptyList())
            _isLoading.value = false
        }
    }

    fun generateStats(category: StatsCategory): List<StatItem> {
        val carsList = _cars.value

        if (carsList.isEmpty()) {
            return emptyList()
        }

        return when (category) {
            StatsCategory.BRAND -> generateBrandStats(carsList)
            StatsCategory.YEAR -> generateYearStats(carsList)
            StatsCategory.COLOR -> generateColorStats(carsList)
            StatsCategory.TYPE -> generateTypeStats(carsList)
            StatsCategory.QUALITY -> generateQualityStats(carsList)
            StatsCategory.TAGS -> generateTagStats(carsList)
            StatsCategory.CREATED_AT -> generateCreatedAtStats(carsList)
        }
    }

    private fun generateBrandStats(cars: List<Car>): List<StatItem> {
        val brandCounts = cars
            .mapNotNull { it.brand }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }

        return brandCounts.map { (brand, count) ->
            StatItem(
                label = brand,
                value = count,
                color = randomColor()
            )
        }
    }

    private fun generateYearStats(cars: List<Car>): List<StatItem> {
        val yearCounts = cars
            .mapNotNull { it.year }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }

        return yearCounts.map { (year, count) ->
            StatItem(
                label = year,
                value = count,
                color = randomColor()
            )
        }
    }

    private fun generateColorStats(cars: List<Car>): List<StatItem> {
        val colorCounts = cars
            .mapNotNull { it.color }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }

        return colorCounts.map { (color, count) ->
            StatItem(
                label = color,
                value = count,
                color = randomColor()
            )
        }
    }

    private fun generateTypeStats(cars: List<Car>): List<StatItem> {
        val typeCounts = cars
            .mapNotNull { it.type }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }

        return typeCounts.map { (type, count) ->
            StatItem(
                label = type,
                value = count,
                color = randomColor()
            )
        }
    }

    private fun generateQualityStats(cars: List<Car>): List<StatItem> {
        val qualityCounts = cars
            .mapNotNull { it.quality }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }

        return qualityCounts.map { (quality, count) ->
            StatItem(
                label = quality,
                value = count,
                color = randomColor()
            )
        }
    }

    private fun generateTagStats(cars: List<Car>): List<StatItem> {
        val allTags = cars.flatMap { it.tags }
        val tagCounts = allTags
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }

        return tagCounts.map { (tag, count) ->
            StatItem(
                label = tag,
                value = count,
                color = randomColor()
            )
        }
    }

    private fun generateCreatedAtStats(cars: List<Car>): List<StatItem> {
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        val monthCounts = cars
            .mapNotNull { it.createdAt }
            .groupingBy { timestamp ->
                val date = Date(timestamp)
                dateFormat.format(date)
            }
            .eachCount()
            .entries
            .sortedByDescending { it.value }

        return monthCounts.map { (month, count) ->
            StatItem(
                label = month,
                value = count,
                color = randomColor()
            )
        }
    }

    private fun randomColor(): Color {
        return Color(
            Random.nextInt(100, 200),
            Random.nextInt(100, 200),
            Random.nextInt(100, 200)
        )
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

