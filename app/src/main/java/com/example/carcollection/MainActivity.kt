// MainActivity.kt
package com.example.carcollection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.carcollection.data.local.CarDatabase
import com.example.carcollection.data.repository.CarRepository
import com.example.carcollection.data.repository.TagRepository
import com.example.carcollection.presentation.main.TubaCollectionApp
import com.example.carcollection.presentation.navigation.AppNavGraph
import com.example.carcollection.ui.theme.CarCollectionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CarCollectionTheme {
                val context = LocalContext.current
                val db = CarDatabase.getDatabase(context)
                val repository = CarRepository(db.carDao())
                val tagRepository = TagRepository(db.tagDao())
                val navController = rememberNavController()

                TubaCollectionApp(navController = navController) {
                    AppNavGraph(navController = navController, repository = repository, tagRepository = tagRepository)
                }
            }
        }
    }
}
