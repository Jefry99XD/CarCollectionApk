package com.example.carcollection.featuremenu.menu

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.example.carcollection.R
import com.example.carcollection.featuremenu.HighlightedCar.CarOfTheDayScreen
import com.example.carcollection.featuremenu.HighlightedCar.TubaDelMesScreen
import com.example.carcollection.featureuser.UserViewModel
import com.example.carcollection.featurecar.domain.CarViewModel

@Composable
fun MenuScreen(
    userViewModel: UserViewModel,
    carViewModel: CarViewModel? = null,
    onNavigateToCollection: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToConsultas: () -> Unit,
    onNavigateToAddAchievement: () -> Unit
) {
    val currentUser by userViewModel.user.collectAsState()
    val userName = currentUser?.username ?: "Usuario"
    val isAdmin = currentUser?.isAdmin ?: false

    // ✅ NUEVO: Estado de carga
    val isLoading by userViewModel.isLoading.collectAsState()
    
    // ✅ Obtener carros del usuario del ViewModel
    val userCars by carViewModel?.cars?.collectAsState(initial = emptyList()) ?: mutableStateOf(emptyList())

    // ...existing code...
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp
    val isTablet = screenWidthDp >= 600

    // ✅ Determinar número de columnas para grid de botones
    val columns = when {
        isTablet && isLandscape -> 3
        isTablet -> 2
        isLandscape -> 2
        else -> 1
    }

    // ✅ Crear lista de botones del menú
    data class MenuItemData(
        val text: String,
        val icon: ImageVector,
        val description: String,
        val onClick: () -> Unit,
        val isAdmin: Boolean = false,
        val showForAdmin: Boolean = false
    )

    val menuItems = listOf(
        MenuItemData("Colección", Icons.Filled.CarCrash, "Explora tus autos", onNavigateToCollection),
        MenuItemData("Tags", Icons.Filled.Tag, "Organiza por etiquetas", onNavigateToTags),
        MenuItemData("Consultas", Icons.Default.QueryStats, "Estadísticas y análisis", onNavigateToConsultas),
        MenuItemData("Administrar Logros", Icons.Filled.AddCircle, "Crear, editar y eliminar logros", onNavigateToAddAchievement, isAdmin = true, showForAdmin = true)
    )

    // ✅ Filtrar items visibles (excluir admin si no es admin)
    val visibleItems = menuItems.filter { !it.showForAdmin || (it.showForAdmin && isAdmin) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            // Header Card con saludo
            item {
                WelcomeHeader(userName = userName)
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Sección de menú
            item {
                Text(
                    text = "Menú Principal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ✅ Botones del menú en grid responsivo
            if (columns == 1) {
                // 📱 Móvil vertical: 1 botón por fila
                items(visibleItems.size) { index ->
                    val item = visibleItems[index]
                    MenuButton(
                        text = item.text,
                        icon = item.icon,
                        description = item.description,
                        onClick = item.onClick,
                        isAdmin = item.isAdmin
                    )
                }
            } else {
                // 📊 Tablet/Horizontal: múltiples botones por fila
                val groupedItems = visibleItems.chunked(columns)
                items(groupedItems.size) { rowIndex ->
                    val rowItems = groupedItems[rowIndex]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                MenuButton(
                                    text = item.text,
                                    icon = item.icon,
                                    description = item.description,
                                    onClick = item.onClick,
                                    isAdmin = item.isAdmin,
                                    isCompact = true
                                )
                            }
                        }
                        // Espacios en blanco para completar la fila
                        repeat(columns - rowItems.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Carro del día
            item { CarOfTheDayScreen(userCars = userCars) }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Tuba del mes con leaderboard
            item { TubaDelMesScreen() }

            // Footer
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Powered by Jefry Cuendiz\nV3",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = MaterialTheme.typography.labelMedium.lineHeight.times(1.3f)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // ✅ NUEVO: Indicador de carga overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Cargando...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeHeader(userName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Columna con el texto
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "👋",
                    style = MaterialTheme.typography.displaySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hola $userName",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bienvenido a tu colección",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Imagen del app icon
            Image(
                painter = painterResource(id = R.drawable.appicon),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: ImageVector,
    description: String = "",
    onClick: () -> Unit,
    isAdmin: Boolean = false,
    isCompact: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isAdmin)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        if (isCompact) {
            // 📊 Modo compacto para tablets/horizontal - layout vertical
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = if (isAdmin)
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = text,
                            modifier = Modifier.size(24.dp),
                            tint = if (isAdmin)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAdmin)
                        MaterialTheme.colorScheme.onTertiaryContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // 📱 Modo normal para móvil - layout horizontal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = if (isAdmin)
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                    else
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = text,
                            modifier = Modifier.size(28.dp),
                            tint = if (isAdmin)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAdmin)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    if (description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isAdmin)
                                MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}