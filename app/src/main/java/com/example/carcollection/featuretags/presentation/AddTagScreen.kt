package com.example.carcollection.featuretags.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.example.carcollection.featuretags.presentation.component.TagFormScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTagScreen(
    viewModel: TagViewModel,
    onTagAdded: () -> Unit,
    onBackClick: () -> Unit
) {
    val tagName = viewModel.tagState.value.name
    val tagColor = viewModel.tagState.value.color
    val isNameValid = tagName.isNotBlank()

    TagFormScreen(
        title = "Agregar Tag",
        tagName = tagName,
        onNameChange = { viewModel.onEvent(TagsEvent.OnNameChanged(it)) },
        isNameValid = isNameValid,
        tagColorHex = tagColor,
        onColorSelected = { viewModel.onEvent(TagsEvent.OnColorChanged(it)) },
        onSave = { viewModel.onEvent(TagsEvent.OnSaveClicked); onTagAdded() },
        onBackClick = onBackClick
    )
}