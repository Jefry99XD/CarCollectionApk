package com.example.carcollection.presentation.lateralMenu

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
import androidx.compose.material.icons.filled.CarCrash
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
import com.example.carcollection.presentation.user.UserViewModel
import com.example.carcollection.ui.theme.sidebar

private const val DefaultDrawerWidthFraction = 0.75f


@Composable
fun AppNavigationDrawer(userViewModel: UserViewModel,
                        navController: NavController) {
    val carCount by userViewModel.carCount.collectAsState()
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
            UserProfileSection(userName = "KiraSlayer", carCount)


            // MENÚ SUPERIOR
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp), // separación entre items
                horizontalAlignment = Alignment.Start, // centrado a la izquierda
                modifier = Modifier.align(Alignment.Start)
            ) {
                SidebarButton(
                    text = "Agregar Carro",
                    icon = Icons.Default.CarCrash
                ) {
                    navController.navigate(NavRoutes.ADD_EDIT_CAR)
                }
                Text("Configuración", style = MaterialTheme.typography.bodyLarge)
            }

            // PLAYER ABAJO
            SidebarMusicPlayer()
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
