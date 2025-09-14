package com.example.carcollection.presentation.lateralMenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.carcollection.utils.MusicPlayer
import com.example.carcollection.utils.MusicPreferences
import kotlinx.coroutines.launch

@Composable
fun SidebarMusicPlayer() {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    val isPlayingFlow = remember { MusicPreferences.isPlayingFlow(context) }
    val isPlaying by isPlayingFlow.collectAsState(initial = true)

    LaunchedEffect(isPlaying) {
        if (isPlaying && !MusicPlayer.isPlaying()) MusicPlayer.toggle()
        if (!isPlaying && MusicPlayer.isPlaying()) MusicPlayer.toggle()
    }

    LaunchedEffect(Unit) {
        MusicPlayer.initialize(context)
    }

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
                    tint = Color.Black
                )
            }
            IconButton(onClick = {
                MusicPlayer.playPrevious()
            }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = Color.Black)
            }
            IconButton(onClick = {
                MusicPlayer.pause()
            }) {
                Icon(
                    Icons.Default.Pause, contentDescription = "Pausa", tint = Color.Black
                )
            }
            IconButton(onClick = {
                MusicPlayer.next()
            }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Siguiente canción", tint = Color.Black)
            }
            IconButton(onClick = {
                MusicPlayer.shuffle()
            }) {
                Icon(Icons.Default.Shuffle, contentDescription = "Aleatorio", tint = Color.Black)
            }
        }
    }
}