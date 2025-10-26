package com.example.carcollection.featuretags.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carcollection.featuretags.domain.Tag
import com.example.carcollection.featuretags.presentation.component.DeleteTagDialog
import com.example.carcollection.featuretags.presentation.component.TagItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewTagsScreen(
    viewModel: TagViewModel,
    onBackClick: () -> Unit,
    onNavigateToAddTag: () -> Unit,
    onNavigateToEditTag: (String) -> Unit
) {
    val tags by viewModel.tags.collectAsState()
    var tagToDelete by remember { mutableStateOf<Tag?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Tags")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAddTag) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Tag")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(tags.sortedBy { it.name.lowercase() }) { tag ->
                TagItem(
                    tag = tag,
                    onEdit = { onNavigateToEditTag(tag.id.toString()) },
                    onDelete = { tagToDelete = it }
                )
            }
        }
    }

    tagToDelete?.let {
        DeleteTagDialog(
            tag = it,
            onConfirm = {
                viewModel.deleteTag(it)
                tagToDelete = null
            },
            onDismiss = { tagToDelete = null }
        )
    }
}
