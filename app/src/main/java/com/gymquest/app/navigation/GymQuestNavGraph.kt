package com.gymquest.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gymquest.app.core.ui.component.QuestBottomNavigation
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestTopBar
import com.gymquest.app.core.ui.component.questPrimaryDestinations
import com.gymquest.app.feature.catalog.CatalogScreen
import com.gymquest.app.feature.catalog.CatalogDetailScreen
import com.gymquest.app.feature.catalog.AddExerciseScreen
import com.gymquest.app.feature.catalog.AddMuscleGroupScreen
import com.gymquest.app.feature.history.HistoryScreen
import com.gymquest.app.feature.home.HomeScreen
import com.gymquest.app.feature.martialarts.MartialArtsHomeScreen
import com.gymquest.app.feature.martialarts.MartialArtScreen
import com.gymquest.app.feature.martialarts.MartialStyleScreen
import com.gymquest.app.feature.martialarts.MartialContentDetailScreen
import com.gymquest.app.feature.martialarts.MartialContentCreateScreen
import com.gymquest.app.feature.martialarts.MartialTechniqueDetailScreen
import com.gymquest.app.feature.martialarts.MartialTechniqueCreateScreen
import com.gymquest.app.feature.martialarts.MartialStanceDetailScreen
import com.gymquest.app.feature.martialarts.MartialStanceCreateScreen
import com.gymquest.app.feature.session.SessionScreen
import com.gymquest.app.feature.routine.RoutineScreen
import com.gymquest.app.feature.routine.RoutineViewModel
import com.gymquest.app.core.time.SystemClockProvider
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gymquest.app.feature.settings.SettingsScreen
import com.gymquest.app.feature.backup.BackupScreen

@Composable
fun GymQuestNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: NavRoutes.HOME
    val primaryRoutes = questPrimaryDestinations.map { it.route }.toSet()
    val title = questPrimaryDestinations.firstOrNull { it.route == currentRoute }?.label
        ?: when (currentRoute) {
            NavRoutes.MARTIAL_ARTS, NavRoutes.MARTIAL_ART, NavRoutes.MARTIAL_STYLE, NavRoutes.MARTIAL_CONTENT,
            NavRoutes.MARTIAL_CREATE_CONTENT, NavRoutes.MARTIAL_TECHNIQUE, NavRoutes.MARTIAL_CREATE_TECHNIQUE,
            NavRoutes.MARTIAL_STANCE, NavRoutes.MARTIAL_CREATE_STANCE -> "Artes marciales"
            NavRoutes.CATALOG_DETAIL -> "Ejercicio"
            NavRoutes.ADD_MUSCLE_GROUP -> "Nuevo grupo muscular"
            NavRoutes.ADD_EXERCISE -> "Nuevo ejercicio"
            NavRoutes.SETTINGS -> "Ajustes"
            NavRoutes.BACKUP -> "Backup"
            else -> "GymQuest"
        }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            QuestTopBar(
                title = title,
                onNavigateBack = if (currentRoute !in primaryRoutes) {
                    { navController.popBackStack() }
                } else {
                    null
                },
                contextualAction = QuestAction.Settings.takeIf { currentRoute == NavRoutes.HOME },
                onContextualAction = if (currentRoute == NavRoutes.HOME) {
                    { navController.navigate(NavRoutes.SETTINGS) }
                } else {
                    null
                },
            )
        },
        bottomBar = {
            QuestBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                    }
                },
            )
        },
    ) { innerPadding -> NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        modifier = Modifier.padding(innerPadding),
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                onOpenSession = { navController.navigate(NavRoutes.SESSION) },
                onOpenCatalog = { navController.navigate(NavRoutes.CATALOG) },
            )
        }

        composable(NavRoutes.SESSION) {
            SessionScreen(onOpenRoutines = { navController.navigate(NavRoutes.ROUTINES) })
        }

        composable(NavRoutes.ROUTINES) {
            val application = LocalContext.current.applicationContext as com.gymquest.app.app.GymQuestApp
            val container = application.appContainer
            val routineViewModel: RoutineViewModel = viewModel(factory = viewModelFactory {
                initializer {
                    RoutineViewModel(
                        observeRoutines = container.observeRoutinesUseCase,
                        observeExerciseCatalog = container.observeExerciseCatalogUseCase,
                        observeMuscleGroups = container.observeMuscleGroupsUseCase,
                        saveRoutine = container.saveRoutineUseCase,
                        startSessionFromRoutineDay = container.startSessionFromRoutineDayUseCase,
                        clock = SystemClockProvider,
                    )
                }
            })
            RoutineScreen(
                stateHolder = routineViewModel,
                onUseRoutineDay = { routine, weekday ->
                    routineViewModel.useRoutineDay(routine, weekday) { started ->
                        if (started) navController.navigate(NavRoutes.SESSION) {
                            popUpTo(NavRoutes.SESSION) { inclusive = false }
                        }
                    }
                },
            )
        }

        composable(NavRoutes.CATALOG) {
            CatalogScreen(
                onOpenExerciseDetail = { navController.navigate(NavRoutes.catalogDetail(it)) },
                onAddMuscleGroup = { navController.navigate(NavRoutes.ADD_MUSCLE_GROUP) },
                onAddExercise = { navController.navigate(NavRoutes.ADD_EXERCISE) },
            )
        }

        composable(NavRoutes.CATALOG_DETAIL) { entry ->
            val id = entry.arguments?.getString("exerciseBaseId")?.toLongOrNull() ?: return@composable
            CatalogDetailScreen(id)
        }

        composable(NavRoutes.ADD_MUSCLE_GROUP) { AddMuscleGroupScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(NavRoutes.ADD_EXERCISE) { AddExerciseScreen(onNavigateBack = { navController.popBackStack() }) }

        composable(NavRoutes.HISTORY) {
            HistoryScreen()
        }

        composable(NavRoutes.MARTIAL_ARTS) {
            MartialArtsHomeScreen(onOpenArt = { navController.navigate(NavRoutes.martialArt(it)) })
        }

        composable(NavRoutes.MARTIAL_ART) { entry ->
            MartialArtScreen(
                artId = entry.arguments?.getString("artId")?.toLongOrNull() ?: return@composable,
                onOpenStyle = { navController.navigate(NavRoutes.martialStyle(it)) },
            )
        }

        composable(NavRoutes.MARTIAL_STYLE) { entry ->
            val styleId = entry.arguments?.getString("styleId")?.toLongOrNull() ?: return@composable
            MartialStyleScreen(
                styleId = styleId,
                onOpenContent = { navController.navigate(NavRoutes.martialContent(it, styleId)) },
                onOpenTechnique = { navController.navigate(NavRoutes.martialTechnique(it, styleId)) },
                onOpenStance = { navController.navigate(NavRoutes.martialStance(it, styleId)) },
                onCreateContent = { navController.navigate(NavRoutes.martialCreateContent(styleId)) },
                onCreateTechnique = { navController.navigate(NavRoutes.martialCreateTechnique(styleId)) },
                onCreateStance = { navController.navigate(NavRoutes.martialCreateStance(styleId)) },
            )
        }

        composable(NavRoutes.MARTIAL_CONTENT) { entry ->
            val contentId = entry.arguments?.getString("contentId")?.toLongOrNull() ?: return@composable
            val styleId = entry.arguments?.getString("styleId")?.toLongOrNull() ?: return@composable
            MartialContentDetailScreen(contentId, styleId)
        }

        composable(NavRoutes.MARTIAL_CREATE_CONTENT) { entry ->
            MartialContentCreateScreen(entry.arguments?.getString("styleId")?.toLongOrNull() ?: return@composable) { navController.popBackStack() }
        }

        composable(NavRoutes.MARTIAL_TECHNIQUE) { entry ->
            val techniqueId = entry.arguments?.getString("techniqueId")?.toLongOrNull() ?: return@composable
            val styleId = entry.arguments?.getString("styleId")?.toLongOrNull() ?: return@composable
            MartialTechniqueDetailScreen(techniqueId, styleId, onFinished = { navController.popBackStack() })
        }
        composable(NavRoutes.MARTIAL_CREATE_TECHNIQUE) { entry ->
            MartialTechniqueCreateScreen(entry.arguments?.getString("styleId")?.toLongOrNull() ?: return@composable) { navController.popBackStack() }
        }
        composable(NavRoutes.MARTIAL_STANCE) { entry ->
            val stanceId = entry.arguments?.getString("stanceId")?.toLongOrNull() ?: return@composable
            val styleId = entry.arguments?.getString("styleId")?.toLongOrNull() ?: return@composable
            MartialStanceDetailScreen(stanceId, styleId, onFinished = { navController.popBackStack() })
        }
        composable(NavRoutes.MARTIAL_CREATE_STANCE) { entry ->
            MartialStanceCreateScreen(entry.arguments?.getString("styleId")?.toLongOrNull() ?: return@composable) { navController.popBackStack() }
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(onOpenBackup = { navController.navigate(NavRoutes.BACKUP) })
        }

        composable(NavRoutes.BACKUP) {
            BackupScreen()
        }
    } }
}
