// MainActivity.kt
package com.example.carcollection

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.ComposeView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.compose.rememberNavController
import com.example.carcollection.data.local.CarDatabase
import com.example.carcollection.data.repository.CarRepository
import com.example.carcollection.featuremenu.lateralMenu.SidebarMusicPlayer
import com.example.carcollection.featuremenu.main.TubaCollectionApp
import com.example.carcollection.presentation.user.UserViewModel
import com.example.carcollection.ui.theme.CarCollectionTheme

class MainActivity : ComponentActivity() {

    private lateinit var drawerLayout: DrawerLayout

    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        // Inicializa el sidebar en Compose
        val composeView = findViewById<ComposeView>(R.id.sidebar_compose_view)
        composeView.setContent {
            CarCollectionTheme {
                SidebarMusicPlayer()
            }
        }

        setContent {
            CarCollectionTheme {
                val db = CarDatabase.getDatabase(this)
                val repository = CarRepository(db.carDao())
                val navController = rememberNavController()

                TubaCollectionApp(
                    userViewModel = UserViewModel(repository),
                    navController = navController,
                    repository = repository
                )
            }
            }
        }

    }


