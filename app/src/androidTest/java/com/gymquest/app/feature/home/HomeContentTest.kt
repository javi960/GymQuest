package com.gymquest.app.feature.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.gymquest.app.core.ui.theme.GymQuestTheme
import com.gymquest.app.feature.preview.PreviewFixtures
import org.junit.Rule
import org.junit.Test

class HomeContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyHomeDirectsTheUserToCreateTheirFirstExercise() {
        composeRule.setContent {
            GymQuestTheme {
                HomeContent(
                    state = HomeUiState(summary = PreviewFixtures.progressSummary(0), isLoading = false),
                    onAction = {},
                    onOpenCatalog = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText("Crear primer ejercicio").assertIsDisplayed()
        composeRule.onNodeWithText("Resumen de progreso").assertIsDisplayed()
    }
}
