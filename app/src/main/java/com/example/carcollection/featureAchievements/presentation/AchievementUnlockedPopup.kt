package com.example.carcollection.featureAchievements.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.carcollection.featureAchievements.domain.AchievementGlobal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AchievementUnlockedPopup(
    achievement: AchievementGlobal,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope() // 👈 para lanzar corrutinas fuera de Composable

    // 🎵 Reproducir sonido al aparecer
    LaunchedEffect(Unit) {
        visible = true
        try {
            val soundPool = android.media.SoundPool.Builder().setMaxStreams(1).build()
            val soundId = soundPool.load(
                context,
                com.example.carcollection.R.raw.ac1,
                1
            )
            soundPool.setOnLoadCompleteListener { _, _, _ ->
                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                scaleIn(
                    initialScale = 0.4f,
                    animationSpec = tween(700, easing = EaseOutBack)
                ),
        exit = fadeOut(animationSpec = tween(300)) +
                scaleOut(targetScale = 0.8f, animationSpec = tween(300))
    ) {
        // Fondo oscuro detrás del popup
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = achievement.iconUrl,
                        contentDescription = achievement.title,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = "🏆 ¡Logro desbloqueado!",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold
                        )
                    )

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(22.dp))
                    Button(onClick = {
                        visible = false
                        scope.launch { // ✅ usamos corrutina aquí, no LaunchedEffect
                            delay(250)
                            onDismiss()
                        }
                    }) {
                        Text("Entendido")
                    }
                }
            }
        }
    }
}
