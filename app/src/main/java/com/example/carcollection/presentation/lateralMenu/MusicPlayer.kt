package com.example.carcollection.presentation.lateralMenu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.carcollection.ui.theme.sidebarBackground
import com.example.carcollection.utils.MusicPlayer
import com.example.carcollection.utils.MusicPreferences
import kotlinx.coroutines.delay
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

    var currentSong by remember { mutableStateOf(MusicPlayer.currentSong()) }
    LaunchedEffect(Unit) {
        while (true) {
            val newSong = MusicPlayer.currentSong()
            if (newSong != currentSong) {
                currentSong = newSong
            }
            delay(500)
        }
    }

    currentSong?.let { song ->
        val coverSize = 60.dp
        val playerHeight = coverSize * 2
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .background(sidebarBackground)
                .clip(MaterialTheme.shapes.medium)
                .height(playerHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.albumCover,
                contentDescription = "Album Cover",
                modifier = Modifier
                    .size(coverSize)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = song.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        color = Color.White // Change text color here
                    )
                    Text(
                        text = song.album,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {

                    IconButton(
                        onClick = { scope.launch { MusicPreferences.setIsPlaying(context, !isPlaying) } },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.MusicNote else Icons.Default.MusicOff,
                            contentDescription = "Toggle Music",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { MusicPlayer.stop() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { MusicPlayer.playPrevious() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { MusicPlayer.pause() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { MusicPlayer.next() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { MusicPlayer.shuffle() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = Color.Black, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
