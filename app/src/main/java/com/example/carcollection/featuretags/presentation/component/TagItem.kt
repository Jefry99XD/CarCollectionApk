package com.example.carcollection.featuretags.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.carcollection.featuretags.domain.Tag

@Composable
fun TagItem(
    tag: Tag,
    onEdit: (Tag) -> Unit,
    onDelete: (Tag) -> Unit
) {
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
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(color = tag.toColor(), shape = CircleShape)
            )
            IconButton(onClick = { onEdit(tag) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar Tag")
            }
            IconButton(onClick = { onDelete(tag) }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar Tag")
            }
        }
    }
}

fun Tag.toColor(): Color {
    val colorString = this.color
    return if (colorString != null && colorString.startsWith("#")) {
        try {
            // Compose's Color can parse a hex string directly.
            // Remove the '#' and parse the rest as a long.
            val hexColor = colorString.replace("#", "FF") // Ensure it has an alpha component
            Color(hexColor.toLong(16))
        } catch (e: Exception) {
            // Fallback for malformed hex strings
            Color.Gray
        }
    } else {
        // Fallback for null or invalid color strings
        Color.Gray
    }
}