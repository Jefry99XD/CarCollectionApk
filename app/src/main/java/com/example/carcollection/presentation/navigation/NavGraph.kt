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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.carcollection.featureAchievements.presentation.AchievementScreen
import com.example.carcollection.featureAchievements.presentation.AchievementViewModel
import com.example.carcollection.featureAchievements.presentation.AddAchievementForm
import com.example.carcollection.featurecar.data.CarMethods
import com.example.carcollection.featurecar.domain.CarFormViewModel
import com.example.carcollection.featurecar.domain.CarFormViewModelFactory
import com.example.carcollection.featurecar.domain.CarViewModel
import com.example.carcollection.featurecar.presentation.add_edit_car.AddEditCarScreen
import com.example.carcollection.featurecar.presentation.add_edit_car.CollectionViewScreen
import com.example.carcollection.featuremenu.main.EasterEggScreen
import com.example.carcollection.featuremenu.menu.MenuScreen
import com.example.carcollection.featurestats.StatsCategory
import com.example.carcollection.featurestats.StatsCategoryScreen
import com.example.carcollection.featurestats.StatsMainScreen
import com.example.carcollection.featurestats.StatsViewModel
import com.example.carcollection.featurestats.StatsViewModelFactory
import com.example.carcollection.featuretags.data.TagsMethods
import com.example.carcollection.featuretags.presentation.AddTagScreen
import com.example.carcollection.featuretags.presentation.EditTagScreen
import com.example.carcollection.featuretags.presentation.TagViewModel
import com.example.carcollection.featuretags.presentation.TagsEvent
import com.example.carcollection.featuretags.presentation.ViewTagsScreen
import com.example.carcollection.featureuser.UserEdit
import com.example.carcollection.featureuser.UserListScreen
import com.example.carcollection.featureuser.UserMain
import com.example.carcollection.featureuser.publicUser.UserPublicProfile
import com.example.carcollection.featureuser.UserViewModel
import com.example.carcollection.featureuser.login.LoginForm
import com.example.carcollection.featureuser.publicUser.PublicUserAchievements
import com.example.carcollection.featureuser.publicUser.PublicUserCarList
import com.example.carcollection.featureuser.register.RegisterForm
import com.example.carcollection.featureconfig.config.About
import com.example.carcollection.featureconfig.config.ConfigMenu
import com.example.carcollection.presentation.consultas.CarLibraryViewModel
import com.example.carcollection.presentation.consultas.CarModelLibraryScreen
import com.example.carcollection.presentation.consultas.LibraryScreen
import com.example.carcollection.featureWishlist.presentation.WishListScreen
import com.example.carcollection.presentation.consultas.QueryMenuScreen
import com.example.carcollection.presentation.consultas.STHScreen
import com.example.carcollection.presentation.consultas.STHViewModel
import com.example.carcollection.presentation.consultas.THScreen
import com.example.carcollection.presentation.consultas.THViewModel
import com.example.carcollection.presentation.data.DataScreen


@SuppressLint("ViewModelConstructorInComposable")
@Composable
fun AppNavGraph(
    navController: NavHostController,
    userViewModel: UserViewModel
) {
    val carMethods = CarMethods()
    val tagsMethods = TagsMethods()
    val achievementViewModel: AchievementViewModel = viewModel()
    val collectionViewModel = remember { CarViewModel( carMethods, tagsMethods) }
    NavHost(navController = navController, startDestination = NavRoutes.MENU) {

        // Pantalla principal (menú)
        composable(NavRoutes.MENU) {
            MenuScreen(
                userViewModel = userViewModel,
                onNavigateToCollection = { navController.navigate(NavRoutes.COLLECTION) },
                onNavigateToTags = { navController.navigate(NavRoutes.VIEW_TAGS) },
                onNavigateToConsultas = { navController.navigate(NavRoutes.CONSULTAS)},
                onNavigateToAddAchievement = { navController.navigate(NavRoutes.ADD_ACHIEVEMENT) }
            )
        }

        // Pantalla de colección
        composable(NavRoutes.COLLECTION) {
            CollectionViewScreen(
                viewModel = collectionViewModel,
                navController = navController,
                onNavigateToAdd = { navController.navigate(NavRoutes.ADD_EDIT_CAR) },
                onEditCar = { carId -> navController.navigate("add_edit_car?carId=$carId") },
                onBackClick = { navController.navigate(NavRoutes.MENU) }
            )
        }
        // resto del NavGraph...

        // Pantalla para agregar nuevo auto
        composable(NavRoutes.ADD_EDIT_CAR) {
            val viewModel = viewModel<CarFormViewModel>(
                factory = CarFormViewModelFactory(carMethods, tagsMethods)
            )
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
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId")
            val viewModel = viewModel<CarFormViewModel>(
                factory = CarFormViewModelFactory(carMethods, tagsMethods)
            )

            LaunchedEffect(carId) {
                if (carId?.isNotEmpty() == true) {
                    viewModel.loadCar(carId)
                }
            }

            AddEditCarScreen(
                viewModel = viewModel,
                onSaveSuccess = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "${NavRoutes.DETAIL}/{carId}"
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId")

            val cars = collectionViewModel.cars.collectAsState(initial = emptyList()).value
            val allTags = collectionViewModel.allTags.collectAsState(initial = emptyList()).value

            val car = cars.find { it.id == carId }

            CarDetailScreen(
                car = car,
                allTags = allTags,
                onBackClick = { navController.popBackStack() }
            )
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
                onNavigateToLibrary = { navController.navigate(NavRoutes.LIBRARY) },
                onNavigateToUserList = { navController.navigate(NavRoutes.USER_LIST) },
                onNavigateToStats = { navController.navigate(NavRoutes.STATS_MAIN) }
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
        composable(NavRoutes.STATS_MAIN) {
            StatsMainScreen(
                onBackClick = { navController.popBackStack() },
                onCategoryClick = { category ->
                    navController.navigate("statsCategory/${category.name}")
                }
            )
        }
        composable(
            route = "statsCategory/{category}",
            arguments = listOf(navArgument("category") { type = NavType.StringType })
        ) { backStackEntry ->

            val categoryName = backStackEntry.arguments?.getString("category")
            val category = StatsCategory.valueOf(categoryName!!)

            val statsViewModel = viewModel<StatsViewModel>(
                factory = StatsViewModelFactory(carMethods)
            )

            StatsCategoryScreen(
                selectedCategory = category,
                viewModel = statsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.CONFIG) {
            ConfigMenu(
                onBackClick = { navController.popBackStack() },
                onNavigateToData = { navController.navigate(NavRoutes.DATA) },
                onNavigateToAbout = { navController.navigate(NavRoutes.ABOUT) },
            )
        }

        composable(NavRoutes.ABOUT) {
            About(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.DATA) {
            DataScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.LIBRARY) {
            val carLibraryViewModel: CarLibraryViewModel = viewModel()
            val selectedCar = carLibraryViewModel.selectedCar.collectAsState()

            // Si hay un carro seleccionado, navegar a la pantalla de modelo
            LaunchedEffect(selectedCar.value) {
                if (selectedCar.value != null) {
                    println("🔀 NavGraph: Navigating to car model library")
                    navController.navigate(NavRoutes.CAR_MODEL_LIBRARY)
                }
            }

            LibraryScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = carLibraryViewModel
            )
        }

        composable(NavRoutes.WISHLIST) {
            WishListScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.CAR_MODEL_LIBRARY) {
            val parentEntry = remember(it) {
                navController.getBackStackEntry(NavRoutes.LIBRARY)
            }
            val carLibraryViewModel: CarLibraryViewModel = viewModel(parentEntry)
            val selectedCar = carLibraryViewModel.selectedCar.collectAsState()

            selectedCar.value?.let { car ->
                CarModelLibraryScreen(
                    carEntry = car,
                    onBackClick = {
                        carLibraryViewModel.clearSelection()
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(NavRoutes.REGISTER) {
            RegisterForm(
                userViewModel = userViewModel,
                navController = navController,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.LOGIN) {
            LoginForm(
                onBackClick = { navController.popBackStack() },
                navController = navController,
                userViewModel = userViewModel
            )
        }
        composable(NavRoutes.PROFILE) {
            UserMain(
                userViewModel = userViewModel,
                onBackClick = { navController.popBackStack() },
                onEditClick = { navController.navigate(NavRoutes.EDIT_PROFILE) },
                navController = navController
            )
        }
        composable(
            route = NavRoutes.PUBLIC_PROFILE,
            arguments = listOf(
                navArgument("uid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""

            UserPublicProfile(
                uid = uid,
                viewModel = userViewModel,
                onBackClick = { navController.popBackStack() },
                onViewCollection = {
                    navController.navigate("public_car_list/$uid")
                },
                onViewAchievements = {
                    navController.navigate("public_achievements/$uid")
                }
            )

        }


        composable(NavRoutes.EDIT_PROFILE) {
            UserEdit(
                userViewModel = userViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.ACHIEVEMENTS) {
            AchievementScreen(
                achievementViewModel = achievementViewModel,
                carViewModel = collectionViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ADD_ACHIEVEMENT) {
            AddAchievementForm(
                achievementViewModel,
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(NavRoutes.USER_LIST){
            UserListScreen(
                userViewModel,
                onBackClick = { navController.popBackStack() },
                onViewProfile = { uid ->
                    navController.navigate(NavRoutes.publicProfile(uid))
                }
                ,
            )
        }
        composable(
            "public_car_list/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->

            val uid = backStackEntry.arguments?.getString("uid")!!

            PublicUserCarList(
                uid = uid,
                viewModel = userViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            "public_achievements/{uid}",
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->

            val uid = backStackEntry.arguments?.getString("uid")!!

            PublicUserAchievements(
                uid = uid,
                achievementViewModel = achievementViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 🥚 Easter Egg secreto
        composable(NavRoutes.EASTER_EGG) {
            EasterEggScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Pantalla de notificaciones
        composable(NavRoutes.NOTIFICATIONS) {
            com.example.carcollection.featureNotification.presentation.NotificationScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

    }
}
