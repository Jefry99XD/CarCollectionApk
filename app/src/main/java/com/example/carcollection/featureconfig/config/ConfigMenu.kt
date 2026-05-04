package com.example.carcollection.featureconfig.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carcollection.featureconfig.data.MusicManager
import com.example.carcollection.featuremenu.menu.MenuButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigMenu(
    onBackClick: () -> Unit,
    onNavigateToData: () -> Unit,
    onNavigateToAbout: () -> Unit,
) {
    val context = LocalContext.current
    val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    // Point 1: use singleton — same instance as MainActivity
    val musicManager = remember { MusicManager.getInstance(context) }
    val isMusicEnabled by musicManager.isMusicEnabled.collectAsState(initial = false)
    // Point 4: observe saved volume for slider
    val savedVolume by musicManager.musicVolume.collectAsState(initial = 0.3f)
    // Local slider state (mirrors savedVolume; updates instantly for smooth drag)
    var sliderVolume by rememberSaveable(savedVolume) { mutableFloatStateOf(savedVolume) }
    // Current track name — refreshes when skip/previous is pressed
    var currentTrack by remember { mutableStateOf(musicManager.currentTrackName()) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Music settings card: toggle + volume slider ──────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ── On / Off row ─────────────────────────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMusicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Column {
                                    Text(
                                        text = "Música",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = if (isMusicEnabled) "Reproduciendo" else "Desactivada",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Switch(
                                checked = isMusicEnabled,
                                onCheckedChange = { enabled ->
                                    coroutineScope.launch {
                                        musicManager.setMusicEnabled(enabled)
                                    }
                                }
                            )
                        }

                        // ── Volume slider (visible when enabled) ─────────────────
                        if (isMusicEnabled) {
                            // ── Playback controls row ─────────────────────────────
                            if (currentTrack.isNotBlank()) {
                                Text(
                                    text = currentTrack,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledIconButton(
                                    onClick = {
                                        musicManager.skipToPrevious()
                                        currentTrack = musicManager.currentTrackName()
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior")
                                }
                                FilledIconButton(
                                    onClick = {
                                        musicManager.skipToNext()
                                        currentTrack = musicManager.currentTrackName()
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Icon(Icons.Default.SkipNext, contentDescription = "Siguiente")
                                }
                            }

                            // ── Volume slider ─────────────────────────────────────
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                                Slider(
                                    value = sliderVolume,
                                    onValueChange = { newVol ->
                                        sliderVolume = newVol
                                        // Apply live while dragging (no DataStore write yet)
                                        musicManager.currentVolume = newVol
                                        // Best-effort: update MediaPlayer immediately
                                    },
                                    onValueChangeFinished = {
                                        // Persist to DataStore once drag ends
                                        coroutineScope.launch {
                                            musicManager.setMusicVolume(sliderVolume)
                                        }
                                    },
                                    valueRange = 0f..1f,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = "Volumen: ${(sliderVolume * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                MenuButton(
                    text = "Respaldos",
                    icon = Icons.Default.Cloud,
                    description = "Gestionar copias de seguridad",
                    onClick = onNavigateToData
                )
            }

            item {
                MenuButton(
                    text = "Actualizar",
                    icon = Icons.Default.SystemUpdate,
                    description = "Buscar actualizaciones",
                    onClick = {
                        checkForUpdateAndDownload(context, versionName.toString())
                    }
                )
            }

            item {
                MenuButton(
                    text = "Acerca de...",
                    icon = Icons.Default.Android,
                    description = "Información de la app",
                    onClick = onNavigateToAbout
                )
            }
        }
    }

}