// presentation/navigation/NavGraph.kt
package com.example.carcollection.presentation.navigation

import CarDetailScreen
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
import com.example.carcollection.featurecar.data.CarMethods
import com.example.carcollection.featurecar.domain.CarFormViewModel
import com.example.carcollection.featurecar.domain.CarViewModel
import com.example.carcollection.featurecar.presentation.add_edit_car.AddEditCarScreen
import com.example.carcollection.featurecar.presentation.add_edit_car.CollectionViewScreen
import com.example.carcollection.featuremenu.menu.MenuScreen
import com.example.carcollection.featuretags.data.TagsMethods
import com.example.carcollection.featuretags.domain.Tag
import com.example.carcollection.featuretags.presentation.AddTagScreen
import com.example.carcollection.featuretags.presentation.EditTagScreen
import com.example.carcollection.featuretags.presentation.TagViewModel
import com.example.carcollection.featuretags.presentation.TagsEvent
import com.example.carcollection.featuretags.presentation.ViewTagsScreen
import com.example.carcollection.presentation.consultas.QueryMenuScreen
import com.example.carcollection.presentation.consultas.STHScreen
import com.example.carcollection.presentation.consultas.STHViewModel
import com.example.carcollection.presentation.consultas.THScreen
import com.example.carcollection.presentation.consultas.THViewModel
import com.example.carcollection.presentation.data.DataScreen
import com.example.carcollection.presentation.statistics.StatisticsMenu
import com.example.carcollection.presentation.user.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNavGraph(
    navController: NavHostController,
    userViewModel: UserViewModel
) {
    val mainViewModel = remember { MainViewModel(repository) }
    NavHost(navController = navController, startDestination = NavRoutes.MENU) {

        // Pantalla principal (menú)
        composable(NavRoutes.MENU) {
            MenuScreen(
                onNavigateToCollection = { navController.navigate(NavRoutes.MAIN) },
                onNavigateToTags = { navController.navigate(NavRoutes.VIEW_TAGS) },
                onNavigateToConsultas = { navController.navigate(NavRoutes.CONSULTAS)},
                onNavigateToStatistics = { navController.navigate(NavRoutes.STATISTICS) },
                onNavigateToRegister = { navController.navigate(NavRoutes.REGISTER) }
            )
        }

        // Pantalla de colección
        composable(NavRoutes.MAIN) {
            CollectionViewScreen(
                viewModel = mainViewModel,
                navController = navController,
                onNavigateToAdd = { navController.navigate(NavRoutes.ADD_EDIT_CAR) },
                onEditCar = { carId -> navController.navigate("add_edit_car?carId=$carId") },
                onBackClick = { navController.navigate(NavRoutes.MENU) }
            )
        }
        // resto del NavGraph...

        // Pantalla para agregar nuevo auto
        composable(NavRoutes.ADD_EDIT_CAR) {
            val carMethods: CarMethods = CarMethods()
            val tagsMethods: TagsMethods = TagsMethods()
            val viewModel = CarFormViewModel(carMethods, tagsMethods,  )
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
            val viewModel = CarFormViewModel(repository, carId)

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
            val carId = backStackEntry.arguments?.getString("carId")?.toIntOrNull() ?: return@composable

            // Obtener el ViewModel compartido (mismo que usas en CollectionViewScreen)
            val carViewModel: CarViewModel = viewModel()

            // Obtener el carro por ID desde el ViewModel
            val car by remember(carId) {
                derivedStateOf {
                    carViewModel.cars.value.find { it.id == carId }
                }
            }

            // Cargar tags disponibles
            val availableTags by carViewModel.allTags.collectAsState(initial = emptyList())

            // Mostrar pantalla solo si el carro existe
            car?.let {
                CarDetailScreen(
                    car = it,
                    availableTags = availableTags,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable(NavRoutes.ADD_EDIT_TAG)
        {
            val viewModel = TagViewModel(tagsMethods = TagsMethods())
            AddTagScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onTagAdded = { navController.popBackStack() }
            )
        }
        composable("tag_edit/{tagId}") { backStackEntry ->
            val tagId = backStackEntry.arguments?.getString("tagId")!!
            val viewModel = TagViewModel(tagsMethods = TagsMethods())

            LaunchedEffect(tagId) {
                viewModel.onEvent(TagsEvent.OnEditClicked(tagId))
            }

            EditTagScreen(
                tagId = tagId,
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onTagSaved = { navController.popBackStack() },
            )
        }


        composable(NavRoutes.VIEW_TAGS) {
            val viewModel = remember { TagViewModel(tagsMethods = TagsMethods()) }

            ViewTagsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToAddTag = { navController.navigate(NavRoutes.ADD_EDIT_TAG) },
                onNavigateToEditTag = { tagId ->
                    navController.navigate(NavRoutes.editTag(tagId))
                }
            )
        }
        composable(NavRoutes.EDIT_TAG) { backStackEntry ->
            val tagId = backStackEntry.arguments?.getString("tagId") ?: return@composable

            val viewModel = remember { TagViewModel(tagsMethods = TagsMethods()) }

            EditTagScreen(
                tagId = tagId,
                viewModel = viewModel,
                onTagSaved = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.CONSULTAS) {
            QueryMenuScreen(
                onNavigateToSTH = { navController.navigate(NavRoutes.VIEW_STH) },
                onNavigateToTH = { navController.navigate(NavRoutes.VIEW_TH) },
                onBackClick = { navController.popBackStack() },
                onNavigateToLibrary = { navController.navigate(NavRoutes.LIBRARY) }
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
        composable(NavRoutes.STATISTICS) {
            StatisticsMenu(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.CONFIG) {
            com.example.carcollection.presentation.config.ConfigMenu(
                onBackClick = { navController.popBackStack() },
                onNavigateToData = { navController.navigate(NavRoutes.DATA) },
                onNavigateToStatistics = { navController.navigate(NavRoutes.STATISTICS) },
                onNavigateToAbout = { navController.navigate(NavRoutes.ABOUT) },
            )
        }
        composable(NavRoutes.ABOUT) {
            com.example.carcollection.presentation.config.About(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.LIBRARY) {
            com.example.carcollection.presentation.consultas.LibraryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.REGISTER) {
            com.example.carcollection.presentation.user.register.RegisterForm(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.LOGIN) {
            com.example.carcollection.presentation.user.login.LoginForm(
                onBackClick = { navController.popBackStack() },
                navController = navController,
                userViewModel = userViewModel
            )
        }
        composable(NavRoutes.PROFILE) {
            com.example.carcollection.presentation.user.UserMain(
                userViewModel = userViewModel,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate(NavRoutes.EDIT_PROFILE) },
            )
        }
        composable(NavRoutes.EDIT_PROFILE) {
            com.example.carcollection.presentation.user.UserEdit(
                userViewModel = userViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
