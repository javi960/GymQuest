package com.gymquest.app.feature.catalog

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.gymquest.app.core.ui.theme.GymQuestTheme
import com.gymquest.app.domain.model.MuscleGroup
import com.gymquest.app.feature.preview.PreviewFixtures

@Preview(name = "Catalogo con ejercicios", showBackground = true)
@Composable
private fun CatalogContentPreview() = PreviewCatalog(CatalogUiState(
    muscleGroups = listOf(
        MuscleGroup(id = 1, name = "Pecho", sortOrder = 0),
        MuscleGroup(id = 2, name = "Espalda", sortOrder = 1),
    ),
    catalog = PreviewFixtures.catalog(),
    selectedMuscleGroupId = 1,
    isLoading = false,
))

@Preview(name = "Catalogo vacio", showBackground = true)
@Composable
private fun CatalogEmptyPreview() = PreviewCatalog(CatalogUiState(isLoading = false))

@Preview(name = "Catalogo cargando", showBackground = true)
@Composable
private fun CatalogLoadingPreview() = PreviewCatalog(CatalogUiState())

@Preview(name = "Catalogo con error", showBackground = true)
@Composable
private fun CatalogErrorPreview() = PreviewCatalog(CatalogUiState(isLoading = false, errorMessage = "No se pudo cargar el catalogo de ejercicios."))

@Preview(name = "Catalogo compacto oscuro, fuente 200%", showBackground = true, widthDp = 320, heightDp = 760, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2f)
@Composable
private fun CatalogDarkPreview() = PreviewCatalog(CatalogUiState(catalog = PreviewFixtures.catalog(), isLoading = false), darkTheme = true)

@Preview(name = "Catalogo movil grande", showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun CatalogLargePreview() = PreviewCatalog(CatalogUiState(catalog = PreviewFixtures.catalog(), isLoading = false))

@Composable
private fun PreviewCatalog(state: CatalogUiState, darkTheme: Boolean = false) {
    GymQuestTheme(darkTheme = darkTheme) {
        CatalogContent(
            state = state,
            onOpenExerciseDetail = {},
            onAddMuscleGroup = {},
            onAddExercise = {},
            onClearFeedback = {},
            onRetry = {},
        )
    }
}
