package com.gymquest.app.feature.progress

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.gymquest.app.core.ui.theme.GymQuestTheme
import com.gymquest.app.feature.preview.PreviewFixtures

@Preview(name = "Progreso con datos", showBackground = true)
@Composable
private fun ProgressContentPreview() = PreviewProgress(ProgressUiState(summary = PreviewFixtures.progressSummary(), isLoading = false))

@Preview(name = "Progreso vacio", showBackground = true)
@Composable
private fun ProgressEmptyPreview() = PreviewProgress(ProgressUiState(summary = PreviewFixtures.progressSummary(0), isLoading = false))

@Preview(name = "Progreso cargando", showBackground = true)
@Composable
private fun ProgressLoadingPreview() = PreviewProgress(ProgressUiState())

@Preview(name = "Progreso con error", showBackground = true)
@Composable
private fun ProgressErrorPreview() = PreviewProgress(ProgressUiState(isLoading = false, errorMessage = "No se pudo cargar el progreso."))

@Preview(name = "Progreso compacto oscuro, fuente 200%", showBackground = true, widthDp = 320, heightDp = 760, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2f)
@Composable
private fun ProgressDarkPreview() = PreviewProgress(ProgressUiState(summary = PreviewFixtures.progressSummary(), isLoading = false), darkTheme = true)

@Preview(name = "Progreso movil grande", showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun ProgressLargePreview() = PreviewProgress(ProgressUiState(summary = PreviewFixtures.progressSummary(), isLoading = false))

@Composable
private fun PreviewProgress(state: ProgressUiState, darkTheme: Boolean = false) {
    GymQuestTheme(darkTheme = darkTheme) {
        ProgressContent(state, {})
    }
}
