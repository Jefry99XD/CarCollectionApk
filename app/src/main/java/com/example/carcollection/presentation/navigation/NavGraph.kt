// presentation/navigation/NavGraph.kt
package com.example.carcollection.presentation.navigation

import CarDetailScreen
import DataScreen
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.carcollection.data.repository.CarRepository
import com.example.carcollection.data.repository.TagRepository
import com.example.carcollection.presentation.add_edit_car.AddEditCarScreen
import com.example.carcollection.presentation.add_edit_car.AddEditCarViewModel
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
                onNavigateToData = { navController.navigate(NavRoutes.DATA) }
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
            val viewModel = ViewTagsViewModel(tagRepository)
            ViewTagsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToAddTag = { navController.navigate(NavRoutes.ADD_EDIT_TAG) }
            )
        }
    }
}
