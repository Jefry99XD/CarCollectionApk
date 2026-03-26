package com.example.carcollection.featurecar.presentation.add_edit_car

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.carcollection.featuretags.domain.Tag

/**
 * Diálogo para seleccionar múltiples tags y asignarlos a varios carros
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MassAddTagDialog(
    selectedCarsCount: Int,
    allTags: List<Tag>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Asignar Tags",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 📊 Información de carros seleccionados
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "Asignando tags a $selectedCarsCount carro${if (selectedCarsCount != 1) "s" else ""}",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 🏷️ Selector de tags
                Text(
                    "Selecciona los tags a asignar:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                if (allTags.isEmpty()) {
                    Text(
                        "No hay tags disponibles",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allTags.forEach { tag ->
                            TagSelectionChip(
                                tag = tag,
                                isSelected = selectedTags.contains(tag.name),
                                onToggle = {
                                    selectedTags = if (selectedTags.contains(tag.name)) {
                                        selectedTags - tag.name
                                    } else {
                                        selectedTags + tag.name
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 📋 Tags seleccionados
                if (selectedTags.isNotEmpty()) {
                    Text(
                        "Tags a asignar: ${selectedTags.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedTags.forEach { tagName ->
                            val tag = allTags.find { it.name == tagName }
                            if (tag != null) {
                                val tagColor = try {
                                    Color((tag.color ?: "#888888").toColorInt().toLong() or 0xFF000000L)
                                } catch (_: Exception) {
                                    Color.Gray
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = tagColor,
                                            shape = MaterialTheme.shapes.extraSmall
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tagName,
                                        color = if (tagColor.luminance() > 0.5) Color.Black else Color.White,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedTags.toList())
                },
                enabled = selectedTags.isNotEmpty()
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Asignar (${selectedTags.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Chip seleccionable para tags
 */
@Composable
private fun TagSelectionChip(
    tag: Tag,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val tagColor = try {
        Color((tag.color ?: "#888888").toColorInt().toLong() or 0xFF000000L)
    } catch (_: Exception) {
        Color.Gray
    }

    val textColor = if (tagColor.luminance() > 0.5) Color.Black else Color.White

    // 🔹 Color de fondo cuando está seleccionado (overlay del primario)
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    } else {
        Color.Transparent
    }

    Surface(
        modifier = Modifier
            .padding(0.dp)
            .then(
                if (isSelected) {
                    // 🔹 Borde más grueso y visible cuando está seleccionado
                    Modifier.border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    )
                } else {
                    Modifier
                }
            ),
        color = tagColor,
        shape = MaterialTheme.shapes.medium,
        // 🔹 Mayor sombra cuando está seleccionado
        shadowElevation = if (isSelected) 16.dp else 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // 🔹 Agregar overlay de fondo cuando está seleccionado
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = tag.name,
                color = textColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
            )
        }
    }
}

// Extension para calcular luminancia del color
fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}

