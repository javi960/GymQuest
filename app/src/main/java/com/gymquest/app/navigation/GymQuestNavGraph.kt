package com.gymquest.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gymquest.app.feature.catalog.CatalogScreen
import com.gymquest.app.feature.history.HistoryScreen
import com.gymquest.app.feature.home.HomeScreen
import com.gymquest.app.feature.martialarts.MartialArtsHomeScreen
import com.gymquest.app.feature.progress.ProgressScreen
import com.gymquest.app.feature.session.SessionScreen
import com.gymquest.app.feature.settings.SettingsScreen

@Composable
fun GymQuestNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                onOpenSession = { navController.navigate(NavRoutes.SESSION) },
                onOpenCatalog = { navController.navigate(NavRoutes.CATALOG) },
                onOpenHistory = { navController.navigate(NavRoutes.HISTORY) },
                onOpenProgress = { navController.navigate(NavRoutes.PROGRESS) },
                onOpenMartialArts = { navController.navigate(NavRoutes.MARTIAL_ARTS) },
                onOpenSettings = { navController.navigate(NavRoutes.SETTINGS) }
            )
        }

        composable(NavRoutes.SESSION) {
            SessionScreen()
        }

        composable(NavRoutes.CATALOG) {
            CatalogScreen()
        }

        composable(NavRoutes.HISTORY) {
            HistoryScreen()
        }

        composable(NavRoutes.PROGRESS) {
            ProgressScreen()
        }

        composable(NavRoutes.MARTIAL_ARTS) {
            MartialArtsHomeScreen()
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen()
        }
    }
}