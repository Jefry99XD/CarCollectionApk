package com.example.carcollection.presentation.data

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.graphics.toColorInt

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import com.example.carcollection.data.local.Tag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTagsScreen(
    viewModel: ViewTagsViewModel,
    onBackClick: () -> Unit,
    onNavigateToAddTag: () -> Unit,
    onNavigateToEditTag: (Int) -> Unit
) {
    val tags = viewModel.tags.collectAsState()

    // Estado para tag a eliminar y mostrar el diálogo
    var tagToDelete by remember { mutableStateOf<Tag?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tags") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    FilledIconButton(onClick = onNavigateToAddTag) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Tag")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tags.value.sortedBy { it.name.lowercase() }) { tag ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        tag.name,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = typography.bodyLarge
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = runCatching { Color(tag.color.toColorInt()) }
                                            .getOrElse { Color.Transparent },
                                        shape = CircleShape
                                    )
                            )
                        }

                        IconButton(onClick = { onNavigateToEditTag(tag.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar Tag")
                        }

                        IconButton(onClick = { tagToDelete = tag }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Tag")
                        }
                    }
                }
            }
        }

        // Diálogo de confirmación
        tagToDelete?.let { tag ->
            AlertDialog(
                onDismissRequest = { tagToDelete = null },
                title = { Text("¿Eliminar tag?") },
                text = { Text("¿Estás seguro de que deseas eliminar el tag \"${tag.name}\"?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteTag(tag)
                        tagToDelete = null
                    }) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { tagToDelete = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
