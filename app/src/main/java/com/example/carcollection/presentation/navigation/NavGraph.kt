// presentation/navigation/NavGraph.kt
package com.example.carcollection.presentation.navigation

import CarDetailScreen
import DataScreen
import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.carcollection.data.repository.CarRepository
import com.example.carcollection.data.repository.TagRepository
import com.example.carcollection.presentation.add_edit_car.AddEditCarScreen
import com.example.carcollection.presentation.add_edit_car.AddEditCarViewModel
import com.example.carcollection.presentation.consultas.QueryMenuScreen
import com.example.carcollection.presentation.consultas.STHScreen
import com.example.carcollection.presentation.consultas.STHViewModel
import com.example.carcollection.presentation.consultas.THScreen
import com.example.carcollection.presentation.consultas.THViewModel
import com.example.carcollection.presentation.data.AddEditTagViewModel
import com.example.carcollection.presentation.data.AddTagScreen
import com.example.carcollection.presentation.data.ViewTagsScreen
import com.example.carcollection.presentation.data.ViewTagsViewModel
import com.example.carcollection.presentation.main.MainScreen
import com.example.carcollection.presentation.main.MainViewModel
import com.example.carcollection.presentation.menu.MenuScreen

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNavGraph(
    navController: NavHostController,
    repository: CarRepository,
    tagRepository: TagRepository
) {
    NavHost(navController = navController, startDestination = NavRoutes.MENU) {

        // Pantalla principal (menú)
        composable(NavRoutes.MENU) {
            MenuScreen(
                onNavigateToCollection = { navController.navigate(NavRoutes.MAIN) },
                onNavigateToData = { navController.navigate(NavRoutes.DATA) },
                onNavigateToTags = { navController.navigate(NavRoutes.VIEW_TAGS) },
                onNavigateToConsultas = { navController.navigate(NavRoutes.CONSULTAS) }
            )
        }

        // Pantalla de colección
        composable(NavRoutes.MAIN) {
            val viewModel = MainViewModel(repository, tagRepository)
            MainScreen(
                viewModel = viewModel,
                navController = navController,
                onNavigateToAdd = {
                    navController.navigate(NavRoutes.ADD_EDIT_CAR)
                },
                onEditCar = { carId ->
                    navController.navigate("add_edit_car?carId=$carId")
                },
                onBackClick = {
                    navController.navigate(NavRoutes.MENU) // o navController.navigate(NavRoutes.MENU) para ir al menú explícitamente
                }
            )
        }


        // Pantalla de datos (import/export)
        composable(NavRoutes.DATA) {
            DataScreen(
                repository = repository,
                onBackClick = {
                    navController.popBackStack()
                }
                ,
                onNavigateToTags = {
                    navController.navigate(NavRoutes.VIEW_TAGS)
                }
            )
        }
        // Pantalla para agregar nuevo auto
        composable(NavRoutes.ADD_EDIT_CAR) {
            val viewModel = AddEditCarViewModel(repository, tagRepository)
            AddEditCarScreen(
                viewModel = viewModel,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }


        // Pantalla para editar un auto existente
        composable(
            route = NavRoutes.ADD_EDIT_CAR_WITH_ID,
            arguments = listOf(
                navArgument("carId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getInt("carId") ?: -1
            val viewModel = AddEditCarViewModel(repository, tagRepository, carId)

            if (carId != -1) {
                viewModel.loadCar(carId)
            }

            AddEditCarScreen(
                viewModel = viewModel,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
        // Pantalla de detalle del auto
        composable("car_detail/{carId}") { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId")?.toIntOrNull()
            val car = carId?.let { MainViewModel(repository, tagRepository).getCarByIdSync(it) } // ← necesitarás esta función
            car?.let {
                CarDetailScreen(car = it, onBackClick = { navController.popBackStack() })

            }
        }
        composable(NavRoutes.ADD_EDIT_TAG)
        {
            val viewModel = AddEditTagViewModel(tagRepository)
            AddTagScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onTagAdded = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.VIEW_TAGS)
        {
            val viewModel = ViewTagsViewModel(tagRepository, repository)
            ViewTagsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToAddTag = { navController.navigate(NavRoutes.ADD_EDIT_TAG) }
            )
        }
        // Pantalla de menú de consultas
        composable(NavRoutes.CONSULTAS) {
            QueryMenuScreen(
                onNavigateToSTH = { navController.navigate(NavRoutes.VIEW_STH) },
                onNavigateToTH = { navController.navigate(NavRoutes.VIEW_TH) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.VIEW_STH) {
            val context = LocalContext.current
            val viewModel = remember { STHViewModel(context.applicationContext as Application) }
            val sthList = viewModel.sthEntries.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.loadSTHFromWeb("https://raw.githubusercontent.com/Jefry99XD/CarCollectionApk/main/app/src/main/assets/sth.json")
            }
            STHScreen(
                sthEntries = sthList.value,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.VIEW_TH) {
            val context = LocalContext.current
            val viewModel = remember { THViewModel(context.applicationContext as Application) }
            val thList = viewModel.thEntries.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.loadTHFromWeb("https://raw.githubusercontent.com/Jefry99XD/CarCollectionApk/main/app/src/main/assets/th.json")
            }
            THScreen(
                thEntries = thList.value,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
