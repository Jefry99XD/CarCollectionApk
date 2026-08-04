package com.example.carcollection.featurestats

import java.text.SimpleDateFormat
import java.util.*

/**
 * Data class representing trend data for a specific month.
 */
data class TrendItem(
    val month: String,
    val count: Int,
    val quality: String = ""
)

/**
 * Generates trend data showing cars added per month.
 */
object TrendAnalyzer {
    
    /**
     * Generate monthly trend data for cars added over time.
     * 
     * @param data List of CarStatsData to analyze
     * @return List of TrendItem sorted by date
     */
    fun generateMonthlyTrend(data: List<com.example.carcollection.featurecar.domain.CarStatsData>): List<TrendItem> {
        if (data.isEmpty()) return emptyList()
        
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val monthMap = mutableMapOf<String, Int>()
        
        data.forEach { car ->
            car.createdAt?.let { timestamp ->
                val monthKey = dateFormat.format(Date(timestamp))
                monthMap[monthKey] = (monthMap[monthKey] ?: 0) + 1
            }
        }
        
        return monthMap.entries
            .sortedBy { it.key }
            .map { (month, count) -> TrendItem(month, count) }
    }
    
    /**
     * Generate quality trend showing distribution of quality levels over time.
     * 
     * @param data List of CarStatsData to analyze
     * @return Map of quality -> list of TrendItems
     */
    fun generateQualityTrend(data: List<com.example.carcollection.featurecar.domain.CarStatsData>): Map<String, List<TrendItem>> {
        if (data.isEmpty()) return emptyMap()
        
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val qualityMap = mutableMapOf<String, MutableMap<String, Int>>()
        
        data.forEach { car ->
            val quality = car.quality ?: "Unknown"
            val createdAt = car.createdAt
            
            if (createdAt != null) {
                val monthKey = dateFormat.format(Date(createdAt))
                val monthData = qualityMap.getOrPut(quality) { mutableMapOf() }
                monthData[monthKey] = (monthData[monthKey] ?: 0) + 1
            }
        }
        
        return qualityMap.mapValues { (_, monthData) ->
            monthData.entries
                .sortedBy { it.key }
                .map { (month, count) -> TrendItem(month, count) }
        }
    }
    
    /**
     * Calculate growth rate between two months.
     * 
     * @param trend List of TrendItems
     * @return Percentage growth (can be negative)
     */
    fun calculateGrowthRate(trend: List<TrendItem>): Float {
        if (trend.size < 2) return 0f
        
        val lastMonth = trend.last().count.toFloat()
        val previousMonth = trend[trend.size - 2].count.toFloat()
        
        if (previousMonth == 0f) return if (lastMonth > 0f) 100f else 0f
        
        return ((lastMonth - previousMonth) / previousMonth) * 100
    }
    
    /**
     * Get average cars per month.
     */
    fun getAveragePerMonth(trend: List<TrendItem>): Float {
        if (trend.isEmpty()) return 0f
        return trend.sumOf { it.count }.toFloat() / trend.size
    }
}
