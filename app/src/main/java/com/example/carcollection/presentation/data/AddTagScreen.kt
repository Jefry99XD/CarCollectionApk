package com.example.carcollection.presentation.data

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun AddTagScreen(viewModel: AddEditTagViewModel,
                 onTagAdded: () -> Unit,
                 onBackClick: () -> Unit
) {
    val tagName = viewModel.tagName.value



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = { onBackClick() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Using AutoMirrored for better LTR/RTL support
                contentDescription = "Volver"
            )
        }
        OutlinedTextField(
            value = tagName,
            onValueChange = { viewModel.tagName.value = it },
            label = { Text("Nombre del Tag") },
            modifier = Modifier.fillMaxWidth()

        )

        Button(onClick = {
            viewModel.saveTag {
                onTagAdded()
            }
        }) {
            Text("Guardar")
            }

    }

}
