package com.gymquest.app.feature.home

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.gymquest.app.core.ui.theme.GymQuestTheme
import com.gymquest.app.feature.preview.PreviewFixtures

@Preview(name = "Inicio con progreso", showBackground = true)
@Composable
private fun HomeContentPreview() = PreviewHome(HomeUiState(summary = PreviewFixtures.progressSummary()))

@Preview(name = "Inicio vacio", showBackground = true)
@Composable
private fun HomeEmptyPreview() = PreviewHome(HomeUiState(summary = PreviewFixtures.progressSummary(0), isLoading = false))

@Preview(name = "Inicio compacto oscuro, fuente 200%", showBackground = true, widthDp = 320, heightDp = 760, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2f)
@Composable
private fun HomeDarkPreview() = PreviewHome(HomeUiState(summary = PreviewFixtures.progressSummary(), activeSession = PreviewFixtures.sessionDetail(), isLoading = false), darkTheme = true)

@Preview(name = "Inicio movil grande", showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun HomeLargePreview() = PreviewHome(HomeUiState(summary = PreviewFixtures.progressSummary(), isLoading = false))

@Preview(name = "Inicio con error", showBackground = true)
@Composable
private fun HomeErrorPreview() = PreviewHome(HomeUiState(isLoading = false, errorMessage = "No se pudo actualizar el inicio."))

@Composable
private fun PreviewHome(state: HomeUiState, darkTheme: Boolean = false) {
    GymQuestTheme(darkTheme = darkTheme) {
        HomeContent(state, {}, {}, {})
    }
}
