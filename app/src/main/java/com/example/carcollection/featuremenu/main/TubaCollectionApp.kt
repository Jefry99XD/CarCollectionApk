package com.example.carcollection.featuremenu.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.carcollection.R
import com.example.carcollection.featuremenu.lateralMenu.AppNavigationDrawer
import com.example.carcollection.presentation.navigation.AppNavGraph
import com.example.carcollection.featureuser.UserViewModel
import com.example.carcollection.featureNotification.presentation.NotificationViewModel
import com.example.carcollection.ui.theme.sidebar
import kotlinx.coroutines.launch


@Composable
fun TubaCollectionApp(
    userViewModel: UserViewModel,
    navController: NavHostController,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // ViewModel de notificaciones para mostrar badge
    val notificationViewModel: NotificationViewModel = viewModel()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    // Estado para el easter egg - requiere 5 clicks rápidos
    var clickCount by remember { mutableStateOf(0) }
    var lastClickTime by remember { mutableStateOf(0L) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppNavigationDrawer(
                userViewModel = userViewModel,
                navController = navController,
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // HEADER TOP
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(sidebar),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Abrir Sidebar",
                            tint = Color.White
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Logo de Tuba Collection",
                        modifier = Modifier
                            .height(120.dp)
                            .clickable {
                                // 🥚 Easter egg secreto - requiere 5 clicks rápidos
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastClickTime < 2000) { // Clicks dentro de 2 segundos
                                    clickCount++
                                    if (clickCount >= 5) {
                                        navController.navigate("easter_egg_secret")
                                        clickCount = 0 // Reset después de activar
                                    }
                                } else {
                                    clickCount = 1 // Reiniciar contador si pasó mucho tiempo
                                }
                                lastClickTime = currentTime
                            }
                    )

                    // Botón de notificaciones con indicador de no leídas
                    Box {
                        IconButton(
                            onClick = { navController.navigate("notifications") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Red dot indicator para notificaciones no leídas
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .offset(x = 20.dp, y = 8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                        }
                    }
                }
            }

            // MAIN CONTENT con NavHost
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.LightGray)
            ) {
                AppNavGraph(
                    navController = navController,
                    userViewModel = userViewModel
                )
            }
        }
    }
}
