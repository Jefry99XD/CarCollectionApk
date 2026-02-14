package com.example.carcollection.featureuser

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.carcollection.featureuser.components.BadgeSize
import com.example.carcollection.featureuser.components.LevelBadge
import com.example.carcollection.featureuser.domain.User

enum class SortOption(val display: String) {
    CARS("Cantidad de carros"),
    ACHIEVEMENTS("Cantidad de logros")
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun UserListScreen(
    viewModel: UserViewModel,
    onBackClick: () -> Unit,
    onViewProfile: (String) -> Unit
)
 {
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf<SortOption?>(null) }
    var isLoading by remember { mutableStateOf(true) }


     val users by viewModel.publicUsers.collectAsState()

     val filteredUsers = remember(searchQuery, sortOption, users) {
         users
             .filter {
                 (it.username ?: "").contains(searchQuery, ignoreCase = true)
             }
             .let { list ->
                 when (sortOption) {
                     SortOption.CARS -> list.sortedByDescending { it.totalCars }
                     SortOption.ACHIEVEMENTS -> list.sortedByDescending { it.badges.size }
                     null -> list
                 }
             }
     }


     LaunchedEffect(Unit) {
         isLoading = true
         viewModel.fetchPublicUsers()
     }

     LaunchedEffect(users) {
         if (users.isNotEmpty()) {
             isLoading = false
         }
     }


     Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de usuarios") },
                navigationIcon = {

                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }

            )
        }
    )
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // <-- ¡Aplica el padding del Scaffold aquí!
                .padding(16.dp) // Luego, aplica tu padding personalizado
        ) {

            // 🔎 Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar usuario...") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🔽 Sort Menu
            SortMenu(
                selectedOption = sortOption,
                onOptionSelected = { sortOption = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🔥 User list con loading state
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron usuarios",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredUsers, key = { it.uid }) { user ->
                        UserCard(
                            user = user,
                            onViewProfile = { onViewProfile(user.uid) }
                        )
                    }
                }
            }

        }

    }


}

// -------------------------------------------------------------------------
// SORT DROPDOWN
// -------------------------------------------------------------------------

@Composable
fun SortMenu(
    selectedOption: SortOption?,
    onOptionSelected: (SortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedOption?.display ?: "Ordenar por...")
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.display) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onViewProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // User avatar
            UserProfileImage(user.photoUrl)

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        user.username ?: "Sin nombre",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    // Badge de nivel
                    LevelBadge(
                        level = user.level,
                        size = BadgeSize.SMALL
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" ${user.totalCars}")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(" ${user.badges.size}")
                }

            }

            Button(onClick = onViewProfile) {
                Text("Ver perfil")
            }
        }
    }
}

// -------------------------------------------------------------------------
// USER IMAGE COMPONENT
// -------------------------------------------------------------------------

@Composable
fun UserProfileImage(url: String?) {
    if (url.isNullOrEmpty()) {
        Icon(
            Icons.Default.Person,
            contentDescription = "Default user",
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Gray.copy(alpha = 0.3f))
                .padding(12.dp)
        )
    } else {
        Image(
            painter = rememberAsyncImagePainter(url),
            contentDescription = "User Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(50))
        )
    }
}

