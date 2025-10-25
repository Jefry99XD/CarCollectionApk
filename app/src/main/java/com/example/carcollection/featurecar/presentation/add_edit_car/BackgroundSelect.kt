package com.example.carcollection.featurecar.presentation.add_edit_car

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson

@Composable
fun BackgroundSelector(
    availableCategories: List<BackgroundCategory>,
    selectedBackground: String,
    onBackgroundSelected: (String) -> Unit
) {

    Column(modifier = Modifier.fillMaxWidth()) {
        availableCategories.forEach { category ->
            Text(
                text = category.category,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(category.backgrounds) { background ->
                    val thumbResId = backgroundResourceMap["${background.resource}_thumb"]
                    val fullResId = backgroundResourceMap[background.resource]
                    val resId = thumbResId ?: fullResId // usa el thumbnail si existe

                    if (resId != null) {
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onBackgroundSelected(background.resource) }
                                .background(
                                    if (background.resource == selectedBackground)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else Color.Transparent
                                )
                        ) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = background.name,
                                modifier = Modifier.size(80.dp),
                                contentScale = ContentScale.Crop
                            )

                            if (background.resource == selectedBackground) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun loadBackgroundCategories(context: Context): List<BackgroundCategory> {
    return try {
        val json = context.assets.open("backgrounds.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<BackgroundCategory>>() {}.type
        Gson().fromJson(json, type)
    } catch (_: Exception) {
        emptyList()
    }
}
