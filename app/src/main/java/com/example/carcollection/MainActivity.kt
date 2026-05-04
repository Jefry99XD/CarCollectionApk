// MainActivity.kt
package com.example.carcollection

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.platform.ComposeView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.carcollection.featureconfig.data.MusicManager
import com.example.carcollection.featuremenu.lateralMenu.SidebarMusicPlayer
import com.example.carcollection.featuremenu.main.TubaCollectionApp
import com.example.carcollection.featureuser.UserViewModel
import com.example.carcollection.ui.theme.CarCollectionTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var musicManager: MusicManager

    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        // Inicializar MusicManager — singleton, shared with ConfigMenu
        musicManager = MusicManager.getInstance(this)

        // Inicializa el sidebar en Compose
        val composeView = findViewById<ComposeView>(R.id.sidebar_compose_view)
        composeView.setContent {
            CarCollectionTheme {
                SidebarMusicPlayer()
            }
        }

        setContent {
            CarCollectionTheme {
                val navController = rememberNavController()
                val userViewModel = UserViewModel()

                // ✅ Sincronizar back button del sistema con NavController
                val backPressedCallback = object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (!navController.popBackStack()) {
                            // Si no hay stack anterior, permite que el sistema maneje el back
                            isEnabled = false
                            onBackPressedDispatcher.onBackPressed()
                        }
                    }
                }

                // Registrar el callback
                this.onBackPressedDispatcher.addCallback(this, backPressedCallback)

                TubaCollectionApp(
                    userViewModel, navController
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Verificar preferencia y reproducir si está habilitada (restoring saved volume first)
        lifecycleScope.launch {
            musicManager.restoreVolume()      // Point 4: apply saved volume before play
            val isMusicEnabled = musicManager.isMusicEnabled.first()
            if (isMusicEnabled) {
                musicManager.startMusic()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // Pausar música cuando la app va a segundo plano
        musicManager.pauseMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Liberar recursos
        musicManager.release()
    }
}


