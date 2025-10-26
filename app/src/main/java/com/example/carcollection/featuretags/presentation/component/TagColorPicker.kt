package com.example.carcollection.featuretags.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.carcollection.featuretags.data.toColorIntSafe
import com.example.carcollection.utils.ColorPickerDialog

@Composable
fun TagColorPicker(
    colorHex: String,
    onColorSelected: (String) -> Unit
) {
    val currentColor = remember { mutableStateOf(Color(colorHex.toColorIntSafe())) }
    val showColorPicker = remember { mutableStateOf(false) }

    // 🔹 sincronizar siempre que cambie colorHex
    LaunchedEffect(colorHex) {
        currentColor.value = Color(colorHex.toColorIntSafe())
    }

    if (showColorPicker.value) {
        ColorPickerDialog(
            initialColor = currentColor.value,
            onDismissRequest = { showColorPicker.value = false },
            onColorSelected = {
                currentColor.value = it
                onColorSelected("#%02X%02X%02X".format(
                    (it.red * 255).toInt(),
                    (it.green * 255).toInt(),
                    (it.blue * 255).toInt()
                ))
                showColorPicker.value = false
            }
        )
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .background(currentColor.value, shape = RoundedCornerShape(12.dp))
            .border(2.dp, Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
            .clickable { showColorPicker.value = true }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Toca para cambiar",
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}