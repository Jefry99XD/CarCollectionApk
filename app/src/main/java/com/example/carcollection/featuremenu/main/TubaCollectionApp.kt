package com.example.carcollection.featuremenu.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.carcollection.R
import com.example.carcollection.data.repository.CarRepository
import com.example.carcollection.featuremenu.lateralMenu.AppNavigationDrawer
import com.example.carcollection.presentation.navigation.AppNavGraph
import com.example.carcollection.presentation.user.UserViewModel
import com.example.carcollection.ui.theme.sidebar
import kotlinx.coroutines.launch


@Composable
fun TubaCollectionApp(
    userViewModel: UserViewModel,
    navController: NavHostController,
    repository: CarRepository,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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
                        modifier = Modifier.height(120.dp)
                    )

                    Spacer(modifier = Modifier.width(48.dp))
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
                    repository = repository,
                    userViewModel = userViewModel
                )
            }
        }
    }
}
