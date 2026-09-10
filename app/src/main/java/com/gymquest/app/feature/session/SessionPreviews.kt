package com.gymquest.app.feature.session

import android.content.res.Configuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.gymquest.app.core.ui.theme.GymQuestTheme
import com.gymquest.app.feature.preview.PreviewFixtures

@Preview(name = "Sesion activa", showBackground = true)
@Composable
private fun SessionContentPreview() = PreviewSession(SessionUiState(
    activeSession = PreviewFixtures.sessionDetail(),
    variants = PreviewFixtures.catalog().flatMap { it.variants },
    selectedVariantId = 1, isLoading = false,
))

@Preview(name = "Sesion sin iniciar", showBackground = true)
@Composable
private fun SessionEmptyPreview() = PreviewSession(SessionUiState(isLoading = false))

@Preview(name = "Sesion cargando", showBackground = true)
@Composable
private fun SessionLoadingPreview() = PreviewSession(SessionUiState())

@Preview(name = "Sesion compacta oscura, fuente 200%", showBackground = true, widthDp = 320, heightDp = 760, uiMode = Configuration.UI_MODE_NIGHT_YES, fontScale = 2f)
@Composable
private fun SessionDarkPreview() = PreviewSession(SessionUiState(
    activeSession = PreviewFixtures.sessionDetail(),
    variants = PreviewFixtures.catalog().flatMap { it.variants },
    selectedVariantId = 1, isLoading = false,
), darkTheme = true)

@Preview(name = "Sesion movil grande", showBackground = true, widthDp = 412, heightDp = 900)
@Composable
private fun SessionLargePreview() = PreviewSession(SessionUiState(
    activeSession = PreviewFixtures.sessionDetail(),
    variants = PreviewFixtures.catalog().flatMap { it.variants },
    selectedVariantId = 1,
    isLoading = false,
))

@Preview(name = "Sesion con error", showBackground = true)
@Composable
private fun SessionErrorPreview() = PreviewSession(SessionUiState(isLoading = false, errorMessage = "No se pudo cargar la sesion activa."))

@Composable
private fun PreviewSession(state: SessionUiState, darkTheme: Boolean = false) {
    GymQuestTheme(darkTheme = darkTheme) {
        SessionContent(state, remember { SnackbarHostState() }, {}, {})
    }
}
