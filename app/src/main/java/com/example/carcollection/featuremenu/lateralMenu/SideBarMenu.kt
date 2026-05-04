package com.example.carcollection.featuremenu.lateralMenu

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.carcollection.featureuser.UserViewModel
import com.example.carcollection.presentation.navigation.NavRoutes
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
    // Level comes from the same user StateFlow — no extra Firestore fetch needed
    val userLevel = user?.level ?: 1
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(DefaultDrawerWidthFraction),
        color = sidebar
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserProfileSection(
                userName = userName, carCount = carCount, photoUrl = photoUrl,
                userLevel = userLevel,
                onEditClick = {
                    onCloseDrawer()
                    navController.navigate(NavRoutes.PROFILE)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sección de Navegación Principal
            SectionLabel("Navegación")

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SidebarButton(
                    text = "Agregar Carro",
                    icon = Icons.Default.CarCrash,
                    onClick = {
                        onCloseDrawer()
                        navController.navigate(NavRoutes.ADD_EDIT_CAR)
                    }
                )

                SidebarButton(
                    text = "Catálogo Completo",
                    icon = Icons.Default.Collections,
                    onClick = {
                        onCloseDrawer()
                        navController.navigate(NavRoutes.LIBRARY)
                    }
                )

                SidebarButton(
                    text = "Lista de Deseados",
                    icon = Icons.Default.Favorite,
                    onClick = {
                        onCloseDrawer()
                        navController.navigate(NavRoutes.WISHLIST)
                    }
                )

                SidebarButton(
                    text = "Logros",
                    icon = Icons.Default.CarCrash,
                    onClick = {
                        onCloseDrawer()
                        navController.navigate(NavRoutes.ACHIEVEMENTS)
                    }
                )
            }

            // Divider visual
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color.White.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            // Sección de Configuración
            SectionLabel("Ajustes")

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SidebarButton(
                    text = "Configuración",
                    icon = Icons.Default.Settings,
                    onClick = {
                        onCloseDrawer()
                        navController.navigate(NavRoutes.CONFIG)
                    }
                )

                // Mostrar solo el botón correspondiente según el estado de autenticación
                if (user != null) {
                    SidebarButton(
                        text = "Cerrar Sesión",
                        icon = Icons.AutoMirrored.Filled.Logout,
                        onClick = {
                            onCloseDrawer()
                            userViewModel.logoutUser()
                        },
                        isDestructive = true
                    )
                } else {
                    SidebarButton(
                        text = "Iniciar Sesión",
                        icon = Icons.AutoMirrored.Filled.Login,
                        onClick = {
                            onCloseDrawer()
                            navController.navigate(NavRoutes.LOGIN)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SidebarButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        color = Color.White.copy(alpha = 0.05f),
        shape = MaterialTheme.shapes.medium,
        interactionSource = interactionSource
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            // Icono con fondo circular
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDestructive)
                            Color.Red.copy(alpha = 0.15f)
                        else
                            Color.White.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(20.dp),
                    tint = if (isDestructive)
                        Color(0xFFFF6B6B)
                    else
                        Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                color = if (isDestructive)
                    Color(0xFFFF6B6B)
                else
                    Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
}