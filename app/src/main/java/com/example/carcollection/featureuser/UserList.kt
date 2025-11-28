package com.example.carcollection.featureuser

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GridView
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
    var isGrid by remember { mutableStateOf(false) }


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
         viewModel.fetchPublicUsers()
     }


     Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de usuarios") },
                navigationIcon = {

                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { isGrid = !isGrid }) {
                        Icon(
                            imageVector = if (isGrid) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                            contentDescription = null
                        )
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

            // 🔥 User list
            if (isGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredUsers, key = { it.uid }) { user ->
                        UserCardVertical(
                            user = user,
                            onViewProfile = { onViewProfile(user.uid) }
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredUsers, key = { it.uid }) { user ->
                        UserCard(
                            user = user,
                            onViewProfile = { onViewProfile(user.uid) },
                            modifier = Modifier.animateItemPlacement()
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
            SortOption.values().forEach { option ->
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

// -------------------------------------------------------------------------
// USER CARD
// -------------------------------------------------------------------------

@Composable
fun UserCard(
    user: User,
    onViewProfile: () -> Unit,
    modifier: Modifier
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
            var textScale by remember { mutableStateOf(1f) }


            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    user.username ?: "Sin nombre",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

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

@Composable
fun UserCardVertical(
    user: User,
    onViewProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserProfileImage(user.photoUrl)

            Spacer(Modifier.height(8.dp))

            Text(
                user.username ?: "Sin nombre",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" ${user.totalCars}")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" ${user.badges.size}")
            }

            Spacer(Modifier.height(10.dp))

            Button(onClick = onViewProfile, modifier = Modifier.fillMaxWidth()) {
                Text("Ver perfil")
            }
        }
    }
}
