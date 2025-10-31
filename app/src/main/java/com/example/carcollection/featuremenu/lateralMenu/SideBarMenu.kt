package com.example.carcollection.featuremenu.lateralMenu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.carcollection.presentation.navigation.NavRoutes
import com.example.carcollection.featureuser.UserViewModel
import com.example.carcollection.ui.theme.sidebar

private const val DefaultDrawerWidthFraction = 0.75f


@Composable
fun AppNavigationDrawer(
    userViewModel: UserViewModel,
    navController: NavController,
    onCloseDrawer: () -> Unit // caller should close drawer (may launch a coroutine)
) {
    val carCount by userViewModel.carCount.collectAsState()
    val user by userViewModel.user.collectAsState()
    val userName = user?.username ?: ""
    val photoUrl = user?.photoUrl ?: ""
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(DefaultDrawerWidthFraction),
        color = sidebar
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp, horizontal = 16.dp), // margen superior e inferior
            verticalArrangement = Arrangement.Top
        ) {
            UserProfileSection(
                userName = userName, carCount = carCount, photoUrl = photoUrl, onEditClick = {
                    onCloseDrawer()
                    navController.navigate(NavRoutes.PROFILE)
                }
            )

            // MENÚ SUPERIOR
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SidebarButton(
                    text = "Agregar Carro",
                    icon = Icons.Default.CarCrash
                ) {
                    onCloseDrawer()
                    navController.navigate(NavRoutes.ADD_EDIT_CAR)
                }

                SidebarButton(
                    text = "Catalogo Completo",
                    icon = Icons.Default.Collections
                ) {
                    onCloseDrawer()
                    navController.navigate(NavRoutes.LIBRARY)
                }
                SidebarButton(
                    text = "Configuración",
                    icon = Icons.Default.Settings
                ) {
                    onCloseDrawer()
                    navController.navigate(NavRoutes.CONFIG)
                }
                SidebarButton(
                    text = "Transferir todo a la nube",
                    icon = Icons.Default.CarCrash // Puedes cambiar el icono si lo prefieres
                ) {
                    onCloseDrawer()
                    // Lógica para transferir todos los carros locales a Firebase
                    //userViewModel.transferAllLocalCarsToFirebase()
                }

                // Mostrar solo el botón correspondiente según el estado de autenticación
                if (user != null) {
                    SidebarButton(
                        text = "Cerrar sesión",
                        icon = Icons.AutoMirrored.Filled.Logout
                    ) {
                        onCloseDrawer()
                        userViewModel.logoutUser()
                    }
                } else {
                    SidebarButton(
                        text = "Iniciar Sesion",
                        icon = Icons.AutoMirrored.Filled.Login
                    ) {
                        onCloseDrawer()
                        navController.navigate(NavRoutes.LOGIN)
                    }
                }

                // PLAYER ABAJO
                SidebarMusicPlayer()
            }
        }
    }
}

@Composable
fun SidebarButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        color = Color.Transparent // Fondo inicial transparente
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp) // Más espacio interno
        ) {
            Icon(icon, contentDescription = text, tint = Color.White)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}