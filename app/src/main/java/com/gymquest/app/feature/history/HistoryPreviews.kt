package com.gymquest.app.feature.history

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.gymquest.app.core.ui.theme.GymQuestTheme
import com.gymquest.app.feature.preview.PreviewFixtures

@Preview(name = "Historial con sesiones", showBackground = true)
@Composable
private fun HistoryContentPreview() = PreviewHistory(HistoryUiState(
    sessions = listOf(PreviewFixtures.sessionDetail().session),
    selectedSessionId = 1,
    selectedSessionDetail = PreviewFixtures.sessionDetail(),
    isLoading = false,
))

@Preview(name = "Historial vacio", showBackground = true)
@Composable
private fun HistoryEmptyPreview() = PreviewHistory(HistoryUiState(isLoading = false))

@Preview(name = "Historial cargando", showBackground = true)
@Composable
private fun HistoryLoadingPreview() = PreviewHistory(HistoryUiState())

@Preview(name = "Historial compacto oscuro, fuente 200%", showBackground = true, widthDp = 320, heightDp = 760, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2f)
@Composable
private fun HistoryDarkPreview() = PreviewHistory(HistoryUiState(sessions = listOf(PreviewFixtures.sessionDetail().session), isLoading = false), darkTheme = true)

@Preview(name = "Historial movil grande", showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun HistoryLargePreview() = PreviewHistory(HistoryUiState(sessions = listOf(PreviewFixtures.sessionDetail().session), isLoading = false))

@Preview(name = "Historial con error", showBackground = true)
@Composable
private fun HistoryErrorPreview() = PreviewHistory(HistoryUiState(isLoading = false, errorMessage = "No se pudo cargar el historial."))

@Composable
private fun PreviewHistory(state: HistoryUiState, darkTheme: Boolean = false) {
    GymQuestTheme(darkTheme = darkTheme) {
        HistoryContent(state, {}, {})
    }
}
