package io.dreamsofcoding.dogs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.dreamsofcoding.dogs.features.SplashScreen
import io.dreamsofcoding.dogs.features.images.ImagesScreen
import io.dreamsofcoding.dogs.features.list.ListScreen
import timber.log.Timber

object Routes {
    const val SPLASH = "splash"
    const val LIST = "list"
    const val IMAGES = "images/{breedName}"

    fun images(breedName: String) = "images/$breedName"

    object Args {
        const val BREED_NAME = "breedName"
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.SPLASH) {
            Timber.d("Navigating to Splash screen")
            SplashScreen(
                navToBreedsScreen = {
                    navController.navigate(Routes.LIST) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LIST) {
            Timber.d("Navigating to List screen")
            ListScreen(
                onBreedClick = { breedName ->
                    Timber.d("Navigating to Images screen for breed: $breedName")
                    navController.navigate(Routes.images(breedName))
                }
            )
        }

        composable(
            Routes.IMAGES,
            arguments = listOf(
                navArgument(Routes.Args.BREED_NAME) {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val breedName = backStackEntry.arguments?.getString("breedName") ?: ""
            Timber.d("Navigating to Images screen with breed: $breedName")

            ImagesScreen(
                onBackClick = {
                    Timber.d("Navigating back from Images screen")
                    navController.popBackStack()
                },
                breedName = breedName
            )
        }
    }
}