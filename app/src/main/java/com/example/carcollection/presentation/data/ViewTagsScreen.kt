package com.example.carcollection.presentation.data

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carcollection.presentation.data.ViewTagsViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.runtime.collectAsState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTagsScreen(
    viewModel: ViewTagsViewModel,
    onBackClick: () -> Unit,
    onNavigateToAddTag: () -> Unit
) {
    val tags = viewModel.tags.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tags") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            item {
                Button(onClick = onNavigateToAddTag) {
                    Text("Agregar Tag")
                }
            }
            items(tags.value) { tag ->
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillParentMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text(tag.name)
                    Button(onClick = {
                        viewModel.deleteTag(tag)
                    }) {
                        Text("Eliminar")
                    }
                }
                Divider()
            }
        }
    }
}

