package com.example.carcollection.featurecar.presentation.add_edit_car

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// Este archivo ahora actúa como un proxy que importa el nuevo componente
@Composable
fun BackgroundSelector(
    availableCategories: List<BackgroundCategory>,
    selectedBackground: String,
    onBackgroundSelected: (String) -> Unit
) {
    BackgroundSelectorFromUrl(
        availableCategories = availableCategories,
        selectedBackgroundId = selectedBackground,
        onBackgroundSelected = onBackgroundSelected
    )
}