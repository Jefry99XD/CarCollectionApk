package com.example.carcollection.featureconfig.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.carcollection.featuremenu.menu.MenuButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigMenu(
    onBackClick: () -> Unit,
    onNavigateToData: () -> Unit,
    onNavigateToAbout: () -> Unit,

) {

    val context = LocalContext.current
    val versionName = context.packageManager.getPackageInfo(context.packageName, 0).versionName
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ){
            item {
                MenuButton(
                    text = "Respaldos",
                    icon = Icons.Default.Cloud,
                    description = "Gestionar copias de seguridad",
                    onClick = onNavigateToData
                )
            }
            item {
                MenuButton(
                    text = "Actualizar",
                    icon = Icons.Default.SystemUpdate,
                    description = "Buscar actualizaciones",
                    onClick = {
                        checkForUpdateAndDownload(context, versionName.toString())
                    }
                )
            }
            item {
                MenuButton(
                    text = "Acerca de...",
                    icon = Icons.Default.Android,
                    description = "Información de la app",
                    onClick = onNavigateToAbout
                )
            }
        }
    }

}