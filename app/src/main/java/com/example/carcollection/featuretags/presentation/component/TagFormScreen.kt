package com.example.carcollection.featuretags.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagFormScreen(
    title: String,
    tagName: String,
    onNameChange: (String) -> Unit,
    isNameValid: Boolean,
    tagColorHex: String,
    onColorSelected: (String) -> Unit,
    onSave: () -> Unit,
    onBackClick: () -> Unit
) {
    // 🔹 Mantener el color sincronizado con tagColorHex
    val currentColor = remember { mutableStateOf(tagColorHex) }

    LaunchedEffect(tagColorHex) {
        currentColor.value = tagColorHex
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            TagNameField(name = tagName, onNameChange = onNameChange, isError = !isNameValid)

            Text("Color del Tag", style = MaterialTheme.typography.titleMedium)

            TagColorPicker(
                colorHex = currentColor.value,
                onColorSelected = {
                    currentColor.value = it
                    onColorSelected(it)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isNameValid
            ) {
                Text("GUARDAR")
            }
        }
    }
}