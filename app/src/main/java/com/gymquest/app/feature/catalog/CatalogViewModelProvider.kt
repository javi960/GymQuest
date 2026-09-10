package com.gymquest.app.feature.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gymquest.app.app.GymQuestApp
import com.gymquest.app.core.time.SystemClockProvider

@Composable
internal fun rememberCatalogViewModel(): CatalogViewModel {
    val application = LocalContext.current.applicationContext as GymQuestApp
    val container = application.appContainer
    return viewModel(
        factory = viewModelFactory {
            initializer {
                CatalogViewModel(
                    createMuscleGroup = container.createMuscleGroupUseCase,
                    observeMuscleGroups = container.observeMuscleGroupsUseCase,
                    createExerciseBase = container.createExerciseBaseUseCase,
                    createExerciseVariant = container.createExerciseVariantUseCase,
                    observeExerciseCatalog = container.observeExerciseCatalogUseCase,
                    updateExerciseBaseUseCase = container.updateExerciseBaseUseCase,
                    updateExerciseVariantUseCase = container.updateExerciseVariantUseCase,
                    archiveExerciseBaseUseCase = container.archiveExerciseBaseUseCase,
                    archiveExerciseVariantUseCase = container.archiveExerciseVariantUseCase,
                    replaceExerciseBaseMedia = container.replaceExerciseBaseMediaUseCase,
                    removeExerciseBaseMedia = container.removeExerciseBaseMediaUseCase,
                    observeExerciseBaseMedia = container.observeExerciseBaseMediaUseCase,
                    clock = SystemClockProvider,
                )
            }
        },
    )
}
