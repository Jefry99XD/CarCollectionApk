package com.example.carcollection.presentation.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.carcollection.R
import com.example.carcollection.utils.MusicPlayer
import com.example.carcollection.utils.MusicPreferences
import kotlinx.coroutines.launch

@Composable
fun TubaCollectionApp(content: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    val isPlayingFlow = remember { MusicPreferences.isPlayingFlow(context) }
    val isPlaying by isPlayingFlow.collectAsState(initial = true)

    var currentSongName by remember { mutableStateOf(MusicPlayer.currentSongName()) }

    LaunchedEffect(isPlaying) {
        if (isPlaying && !MusicPlayer.isPlaying()) MusicPlayer.toggle()
        if (!isPlaying && MusicPlayer.isPlaying()) MusicPlayer.toggle()
    }

    LaunchedEffect(Unit) {
        MusicPlayer.initialize(context)
        currentSongName = MusicPlayer.currentSongName()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0, 187, 187)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo de Tuba Collection",
                    modifier = Modifier.height(120.dp)
                )
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Row(horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = {
                            scope.launch { MusicPreferences.setIsPlaying(context, !isPlaying) }
                        }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                contentDescription = "Toggle Music",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = {
                            MusicPlayer.playPrevious()
                            currentSongName = MusicPlayer.currentSongName()
                        }) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = Color.White)
                        }
                        IconButton(onClick = {
                            MusicPlayer.pause()
                        }) {
                            Icon(
                                Icons.Default.Pause, contentDescription = "Pausa", tint = Color.White
                            )
                        }
                        IconButton(onClick = {
                            MusicPlayer.next()
                            currentSongName = MusicPlayer.currentSongName()
                        }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Siguiente canción", tint = Color.White)
                        }
                        IconButton(onClick = {
                            MusicPlayer.shuffle()
                            currentSongName = MusicPlayer.currentSongName()
                        }) {
                            Icon(Icons.Default.Shuffle, contentDescription = "Aleatorio", tint = Color.White)
                        }
                    }

                    SongInfo(currentSongName)
                }
            }
        }

        Surface(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
            content()
        }
    }
}



@Composable
fun SongInfo(songTitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
        Column {
            Text(
                text = songTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }
    }
}
