// presentation/navigation/NavGraph.kt
package com.example.carcollection.presentation.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.carcollection.data.repository.CarRepository
import com.example.carcollection.presentation.add_edit_car.AddEditCarScreen
import com.example.carcollection.presentation.add_edit_car.AddEditCarViewModel
import com.example.carcollection.presentation.main.MainScreen
import com.example.carcollection.presentation.main.MainViewModel

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNavGraph(
    navController: NavHostController,
    repository: CarRepository
) {
    NavHost(navController = navController, startDestination = NavRoutes.MAIN) {

        composable(NavRoutes.MAIN) {
            val viewModel = MainViewModel(repository)
            MainScreen(viewModel = viewModel, onNavigateToAdd = {
                navController.navigate(NavRoutes.ADD_EDIT_CAR)
            })
        }

        composable(NavRoutes.ADD_EDIT_CAR) {
            val viewModel = AddEditCarViewModel(repository)
            AddEditCarScreen(
                viewModel = viewModel,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.ADD_EDIT_CAR_WITH_ID) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId")?.toIntOrNull()
            val viewModel = AddEditCarViewModel(repository)

            // Si hay un carId, cargar datos
            if (carId != null) {
                viewModel.loadCar(carId)
            }

            AddEditCarScreen(
                viewModel = viewModel,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}
