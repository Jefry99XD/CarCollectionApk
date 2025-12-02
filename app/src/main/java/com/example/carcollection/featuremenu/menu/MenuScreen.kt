package com.example.carcollection.featuremenu.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.carcollection.featuremenu.HighlightedCar.CarOfTheDayScreen
import com.example.carcollection.featureuser.UserViewModel

@Composable
fun MenuScreen(
    userViewModel: UserViewModel,
    onNavigateToCollection: () -> Unit,
    onNavigateToTags: () -> Unit,
    onNavigateToConsultas: () -> Unit,
    onNavigateToAddAchievement: () -> Unit
) {
    val currentUser by userViewModel.user.collectAsState()
    val isAdmin = currentUser?.isAdmin ?: false
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 64.dp) // espacio extra al final
    ) {
        item {
            Text(
                text = "Hola Tubas",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        // Cada botón como item independiente para scroll más suave
        item { MenuButton("Colección", Icons.Filled.CarCrash, onNavigateToCollection) }
        item { MenuButton("Tags", Icons.Filled.Tag, onNavigateToTags) }
        item { MenuButton("Consultas", Icons.Default.QueryStats, onNavigateToConsultas) }

        // Solo mostrar el botón de Agregar Logro para administradores
        if (isAdmin) {
            item { MenuButton("Agregar Logro", Icons.Filled.AddCircle, onNavigateToAddAchievement) }
        }

        item { CarOfTheDayScreen() }

        item {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Powered by Jefry Cuendiz. V3",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MenuButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Icon(icon, contentDescription = text)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}