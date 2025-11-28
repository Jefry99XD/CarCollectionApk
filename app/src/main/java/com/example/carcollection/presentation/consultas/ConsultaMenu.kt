package com.example.carcollection.presentation.consultas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalCarWash
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueryMenuScreen(
    onNavigateToSTH: () -> Unit,
    onNavigateToTH: () -> Unit,
    onBackClick: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToUserList: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consultas") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            item {
                Button(
                    onClick = onNavigateToSTH,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star, // Usa el ícono que quieras
                        tint =  androidx.compose.ui.graphics.Color.Yellow,
                        contentDescription = null
                    )
                    Text("Super Treasure Hunt")
                }
            }


            item {
                Button(
                    onClick = onNavigateToTH,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Star, // Usa el ícono que quieras
                        tint =  Color.Gray,
                        contentDescription = null
                    )
                    Text("Treasure Hunt")
                }
            }
            item {
                Button(
                    onClick = onNavigateToLibrary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(

                        imageVector = Icons.Default.LocalCarWash, // Usa el ícono que quieras
                        tint =  Color.Gray,
                        contentDescription = null
                    )
                    Text("Catalogo completo")
                }
            }

            item {
                Button(
                    onClick = onNavigateToUserList,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = null
                    )
                    Text("Lista de usuarios")
                }
            }

            item {
                Button(
                    onClick = onNavigateToStats,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.QueryStats,
                        contentDescription = null
                    )
                    Text("Estadisticas")
                }
            }
        }
    }
}
