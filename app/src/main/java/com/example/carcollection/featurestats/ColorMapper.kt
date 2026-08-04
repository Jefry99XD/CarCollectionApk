package com.example.carcollection.featurestats

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * Deterministic color mapper for statistics categories.
 * Assigns consistent colors based on label hash, ensuring colors remain
 * the same across recompositions and sessions.
 */
object ColorMapper {
    
    // Predefined palette of visually distinct colors
    private val COLOR_PALETTE = listOf(
        Color(0xFF4A90E2),  // Blue
        Color(0xFFF5A623),  // Orange
        Color(0xFF7ED321),  // Green
        Color(0xFFBD10E0),  // Purple
        Color(0xFF50E3C2),  // Teal
        Color(0xFFFF6B6B),  // Red
        Color(0xFFFFD700),  // Gold
        Color(0xFF00CED1),  // Dark Turquoise
        Color(0xFF9370DB),  // Medium Purple
        Color(0xFF20B2AA),  // Light Sea Green
        Color(0xFFFF69B4),  // Hot Pink
        Color(0xFF32CD32),  // Lime Green
        Color(0xFF1E90FF),  // Dodger Blue
        Color(0xFFFF8C00),  // Dark Orange
        Color(0xFF3CB371),  // Medium Sea Green
        Color(0xFF8A2BE2),  // Blue Violet
    )
    
    /**
     * Get a deterministic color for a label.
     * The same label will always return the same color.
     *
     * @param label The label/category name
     * @return A Color from the predefined palette
     */
    fun getColorForLabel(label: String): Color {
        if (label.isBlank()) return COLOR_PALETTE.first()
        
        // Use hash of the label to select from palette
        val hash = abs(label.hashCode())
        val index = hash % COLOR_PALETTE.size
        return COLOR_PALETTE[index]
    }
    
    /**
     * Get a list of deterministic colors for multiple labels.
     * Ensures no duplicate colors if possible.
     *
     * @param labels List of labels to assign colors to
     * @return List of colors, one per label
     */
    fun getColorsForLabels(labels: List<String>): List<Color> {
        if (labels.isEmpty()) return emptyList()
        
        // If we have fewer labels than colors, ensure no duplicates
        if (labels.size <= COLOR_PALETTE.size) {
            val usedIndices = mutableSetOf<Int>()
            return labels.map { label ->
                if (label.isBlank()) {
                    COLOR_PALETTE.first()
                } else {
                    var index = abs(label.hashCode()) % COLOR_PALETTE.size
                    // If this index is already used, find the next available
                    var attempts = 0
                    while (usedIndices.contains(index) && attempts < COLOR_PALETTE.size) {
                        index = (index + 1) % COLOR_PALETTE.size
                        attempts++
                    }
                    usedIndices.add(index)
                    COLOR_PALETTE[index]
                }
            }
        } else {
            // More labels than colors, just cycle through palette
            return labels.map { getColorForLabel(it) }
        }
    }
}
