package com.example.carcollection.featuretags.data

import androidx.core.graphics.toColorInt

fun String.toColorIntSafe(default: Int = 0xFF000000.toInt()): Int {
    return try {
        this.toColorInt()
    } catch (e: Exception) {
        default
    }
}