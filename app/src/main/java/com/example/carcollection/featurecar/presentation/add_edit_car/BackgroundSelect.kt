package com.example.carcollection.featurecar.presentation.add_edit_car

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Selector de fondos - Interfaz principal
 * Muestra categorías de fondos en filas horizontales
 *
 * @param availableCategories Lista de categorías de fondos disponibles
 * @param selectedBackground ID del fondo actualmente seleccionado
 * @param onBackgroundSelected Callback cuando se selecciona un nuevo fondo
 */
@Composable
fun BackgroundSelector(
    availableCategories: List<BackgroundCategory>,
    selectedBackground: String,
    onBackgroundSelected: (String) -> Unit
) {
    BackgroundCategoriesRowView(
        categories = availableCategories,
        selectedId = selectedBackground,
        onSelect = onBackgroundSelected,
        modifier = Modifier.fillMaxWidth()
    )
}