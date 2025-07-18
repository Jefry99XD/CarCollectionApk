package com.example.carcollection.presentation.statistics

import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.decode.GifDecoder
import coil.ImageLoader
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import com.example.carcollection.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsMenu(
    onBackClick: () -> Unit
) {
    val gifUrl = "https://media.tenor.com/8QavbrTvkpUAAAAM/god-of-war-kratos-falling.gif"
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(GifDecoder.Factory())
        }
        .build()

    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.kratos) // tu_audio debe estar en res/raw
    }

    DisposableEffect(Unit) {
        mediaPlayer.start() // Inicia al entrar

        onDispose {
            mediaPlayer.stop()   // Para cuando la pantalla se va
            mediaPlayer.release() // Libera recursos
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todavia no, pero pronto... No habra esperanza") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(gifUrl)
                    .crossfade(true)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = "GIF de próximamente",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Text(
                text = "¡Próximamente!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}