package com.example.carcollection.presentation.data

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.graphics.toColorInt


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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    FilledIconButton(onClick = onNavigateToAddTag) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Agregar Tag"
                        )
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
            /** Lista de tags **/
            items(tags.value) { tag ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Nombre del tag
                    Text(
                        tag.name,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Swatch de color
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = runCatching { Color(tag.color.toColorInt()) }
                                    .getOrElse { Color.Transparent },
                                shape = CircleShape
                            )
                    )
                    IconButton(onClick = { viewModel.deleteTag(tag) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar Tag")
                    }
                }
                Divider()
            }
        }
    }
}

