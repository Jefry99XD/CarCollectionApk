package com.example.carcollection.featurecar.presentation.add_edit_car.carDetailScreen

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil.compose.SubcomposeAsyncImage
import com.example.carcollection.R
import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featurecar.presentation.add_edit_car.carDetailScreen.AutoSizeText
import com.example.carcollection.featuretags.domain.Tag
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ✅ Enum para fondos
enum class CarBackground(val drawableId: Int, val displayName: String) {
    FONDO1(R.drawable.fondo, "Fondo 1"),
    FONDO2(R.drawable.fondo2, "Fondo 2"),
    FONDO3(R.drawable.fondo3, "Fondo 3"),
    FONDO4(R.drawable.fondo4, "Fondo 4"),
    FONDO5(R.drawable.fondo5, "Fondo 5"),
    FONDO6(R.drawable.fondo6, "Fondo 6"),
    FONDO7(R.drawable.fondo7, "Fondo 7"),
    FONDO8(R.drawable.fondo8, "Fondo 8"),
    FONDO10(R.drawable.fondo10, "Fondo 10"),
    FONDO15(R.drawable.fondo15, "Fondo 15"),
    FONDO20(R.drawable.fondo20, "Fondo 20"),
    FONDO23(R.drawable.fondo23, "Fondo 23"),
    FONDO24(R.drawable.fondo24, "Fondo 24"),
    FONDO26(R.drawable.fondo26, "Fondo 26");

    companion object {
        fun getDrawableId(backgroundName: String?): Int {
            return values().find { it.name == backgroundName }?.drawableId ?: FONDO1.drawableId
        }
    }
}

private fun Color.isLightColor(): Boolean {
    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255
    val luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255
    return luminance > 0.5
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CarDetailScreenNew(
    car: Car?,
    allTags: List<Tag>,
    isFavorite: Boolean = false,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit = {},
    onDeleteClick: (String) -> Unit = {},
    onShareClick: () -> Unit = {},
    onToggleFavorite: () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showImageDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showFullscreenGallery by remember { mutableStateOf(false) }
    var localIsFavorite by remember(isFavorite) { mutableStateOf(isFavorite) }

    // ✅ Detectar orientación
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    if (car == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cargando detalles del carro...")
            }
        }
        return
    }

    val primaryTag = car.tags.firstOrNull()
    val primaryTagColor = allTags.find { it.name == primaryTag }?.color ?: "#CCCCCC"
    val parsedColor = try {
        Color(primaryTagColor.toColorInt())
    } catch (_: Exception) {
        Color.Gray
    }

    val resId = CarBackground.getDrawableId(car.backgroundName)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val createdDate = car.createdAt?.let { dateFormat.format(Date(it)) } ?: "Desconocida"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${car.brand.orEmpty()} ${car.name.orEmpty()}") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        localIsFavorite = !localIsFavorite
                        onToggleFavorite()
                    }) {
                        Icon(
                            imageVector = if (localIsFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (localIsFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onShareClick) {
                        Icon(Icons.Default.Send, contentDescription = "Compartir")
                    }
                    IconButton(onClick = { onEditClick(car.id.orEmpty()) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .animateContentSize()
        ) {
            if ((isLandscape || isTablet) && !isLandscape) {
                // Tablet Vertical
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CarImageSection(car, { showImageDialog = true }, { showFullscreenGallery = true }, Modifier.weight(1f))
                    CarInfoSection(car, allTags, createdDate, Modifier.weight(1f))
                }
            } else if (isLandscape) {
                // Móvil Horizontal
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CarImageSection(car, { showImageDialog = true }, { showFullscreenGallery = true }, Modifier.weight(1f))
                    CarInfoSection(car, allTags, createdDate, Modifier.weight(1f))
                }
            } else {
                // Móvil Vertical
                Card(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .fillMaxSize()
                        .animateContentSize(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = null,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.FillBounds
                        )

                        Row(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = "Logo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                CarImageSection(car, { showImageDialog = true }, { showFullscreenGallery = true },
                                    Modifier
                                        .fillMaxWidth()
                                        .height(180.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                CarInfoSection(car, allTags, createdDate, Modifier
                                    .fillMaxWidth()
                                    .weight(1f))
                            }
                            Box(
                                modifier = Modifier
                                    .width(48.dp)
                                    .fillMaxHeight()
                                    .background(parsedColor),
                                contentAlignment = Alignment.Center
                            ) {
                                AutoSizeText(
                                    text = primaryTag?.map { "$it\n" }?.joinToString("") ?: "",
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    backgroundColor = parsedColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFullscreenGallery) {
        FullscreenImageDialog(
            imageUrl = car.photoUrl,
            title = "${car.brand.orEmpty()} ${car.name.orEmpty()}",
            onDismiss = { showFullscreenGallery = false },
            onCopyUrl = {
                clipboardManager.setText(AnnotatedString(car.photoUrl.orEmpty()))
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("URL copiada")
                }
            }
        )
    }

    if (showImageDialog && car.photoUrl != null) {
        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            confirmButton = { TextButton(onClick = { showImageDialog = false }) { Text("Cerrar") } },
            text = {
                SubcomposeAsyncImage(
                    model = car.photoUrl,
                    contentDescription = "Ampliar",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit,
                    loading = { CircularProgressIndicator() }
                )
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar Carro") },
            text = { Text("¿Eliminar ${car.name.orEmpty()}?") },
            confirmButton = {
                Button(
                    onClick = { onDeleteClick(car.id.orEmpty()); showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun CarImageSection(
    car: Car,
    showImageDialog: () -> Unit,
    showFullscreenGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        SubcomposeAsyncImage(
            model = car.photoUrl ?: "",
            contentDescription = "${car.brand.orEmpty()} ${car.name.orEmpty()}",
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(200.dp)
                .clip(CircleShape)
                .clickable { showFullscreenGallery() },
            contentScale = ContentScale.Fit,
            loading = { CircularProgressIndicator() },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) { Text("Imagen no disponible") }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        IconButton(onClick = showImageDialog) {
            Icon(Icons.Default.ImageSearch, contentDescription = "Ampliar", modifier = Modifier.size(32.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CarInfoSection(
    car: Car,
    allTags: List<Tag>,
    createdDate: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(car.name.orEmpty(), style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text("${car.brand.orEmpty()} · ${car.year.orEmpty()}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            DetailRow("Color:", car.color.orEmpty())
            DetailRow("Tipo:", car.type.orEmpty())
            DetailRow("Serie:", car.serie.orEmpty())
            DetailRow("Fecha Agregado:", createdDate)
            Spacer(modifier = Modifier.height(8.dp))

            if (car.tags.isNotEmpty()) {
                Text("Tags:", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    car.tags.forEach { tagName ->
                        val tagColor = allTags.find { it.name == tagName }?.color ?: "#888888"
                        val chipColor = try { Color(tagColor.toColorInt()) } catch (_: Exception) { Color.Gray }
                        val textColor = if (chipColor.isLightColor()) Color.Black else Color.White

                        Box(modifier = Modifier
                            .background(color = chipColor, shape = RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(text = tagName, color = textColor, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.bodySmall, color = Color.White)
        }
    }
}

@Composable
private fun FullscreenImageDialog(
    imageUrl: String?,
    title: String,
    onDismiss: () -> Unit,
    onCopyUrl: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                contentScale = ContentScale.Fit,
                loading = { CircularProgressIndicator(color = Color.White) },
                error = { Text("Error al cargar", color = Color.White) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCopyUrl, modifier = Modifier.weight(1f)) { Text("Copiar URL", color = Color.White) }
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cerrar", color = Color.White) }
            }
        }
    }
}
