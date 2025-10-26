package com.example.carcollection.featuretags.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.example.carcollection.featuretags.presentation.component.TagFormScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTagScreen(
    tagId: String,
    viewModel: TagViewModel,
    onTagSaved: () -> Unit,
    onBackClick: () -> Unit
) {
    // 👇 Cargar el tag cuando se abre esta pantalla
    LaunchedEffect(tagId) {
        viewModel.loadTag(tagId)
    }

    val tagState by viewModel.tagState

    TagFormScreen(
        title = "Editar Tag",
        tagName = tagState.name,
        onNameChange = { viewModel.onEvent(TagsEvent.OnNameChanged(it)) },
        isNameValid = tagState.name.isNotBlank(),
        tagColorHex = tagState.color,
        onColorSelected = { viewModel.onEvent(TagsEvent.OnColorChanged(it)) },
        onSave = { viewModel.saveEdit { onTagSaved() } },
        onBackClick = onBackClick
    )
}

