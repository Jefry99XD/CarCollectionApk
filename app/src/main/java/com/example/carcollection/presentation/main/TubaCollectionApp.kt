// TubaCollectionApp.kt
package com.example.carcollection.presentation.main

import android.app.Application
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.carcollection.R
import com.example.carcollection.utils.MusicPlayer
import com.example.carcollection.utils.MusicPreferences
import com.example.carcollection.utils.SongMetadata
import com.example.carcollection.utils.getSongMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TubaCollectionApp(
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    // Estado leído de DataStore
    val isPlayingFlow = remember { MusicPreferences.isPlayingFlow(context) }
    val isPlaying by isPlayingFlow.collectAsState(initial = true)

    var currentSongName by remember { mutableStateOf("") }
    var songMetadata by remember { mutableStateOf<SongMetadata?>(null) }

    // Cuando cambia el estado isPlaying, controlamos reproducción
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            if (!MusicPlayer.isPlaying()) {
                MusicPlayer.toggle(context)
            }
        } else {
            if (MusicPlayer.isPlaying()) {
                MusicPlayer.toggle(context)
            }
        }
    }

    LaunchedEffect(Unit) {
        val assetSongs = withContext(Dispatchers.IO) {
            context.assets.list("songs")?.filter { it.endsWith(".mp3") } ?: emptyList()
        }
        if (assetSongs.isNotEmpty()) {
            MusicPlayer.initialize(context, assetSongs)
            currentSongName = MusicPlayer.currentSongName()
            songMetadata = getSongMetadata(context, MusicPlayer.currentSongFile())
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0, 187, 187)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Logo a la izquierda
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo de Tuba Collection",
                        modifier = Modifier.height(120.dp)
                    )

                    // Contenido derecho (botones + info canción)
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        // Botones
                        Row(
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                // Cambiamos el estado y guardamos en DataStore
                                scope.launch {
                                    MusicPreferences.setIsPlaying(context, !isPlaying)
                                }
                            }) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                    contentDescription = "Toggle Music",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = {
                                MusicPlayer.playPrevious(context)
                                currentSongName = MusicPlayer.currentSongName()
                                songMetadata = getSongMetadata(context, MusicPlayer.currentSongFile())
                            }) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = Color.White)
                            }
                            IconButton(onClick = {
                                MusicPlayer.next(context)
                                currentSongName = MusicPlayer.currentSongName()
                                songMetadata = getSongMetadata(context, MusicPlayer.currentSongFile())
                            }) {
                                Icon(
                                    imageVector = Icons.Default.SkipNext,
                                    contentDescription = "Siguiente canción",
                                    tint = Color.White
                                )
                            }
                            IconButton(onClick = {
                                MusicPlayer.shuffle(context)
                                currentSongName = MusicPlayer.currentSongName()
                                songMetadata = getSongMetadata(context, MusicPlayer.currentSongFile())
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Shuffle,
                                    contentDescription = "Aleatorio",
                                    tint = Color.White
                                )
                            }
                        }

                        // Info canción justo debajo de los botones
                        SongInfo(metadata = songMetadata)
                    }
                }
            }
        }

        // Contenido principal
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        ) {
            content()
        }
    }
}

@Composable
fun SongInfo(metadata: SongMetadata?) {
    if (metadata == null) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        metadata.albumArt?.let { artBytes ->
            val bitmap = remember(artBytes) {
                BitmapFactory.decodeByteArray(artBytes, 0, artBytes.size)
            }
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Carátula del álbum",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column {
            Text(
                text = metadata.title ?: MusicPlayer.currentSongName(),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            metadata.album?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
