package com.gymquest.app.core.ui.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.gymquest.app.core.ui.theme.GymQuestTheme
import org.junit.Rule
import org.junit.Test

class QuestConfirmationDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun confirmationDialogExplainsTheConsequenceAndOffersBothChoices() {
        composeRule.setContent {
            GymQuestTheme {
                QuestConfirmationDialog(
                    title = "¿Completar sesión?",
                    message = "Se calculará el progreso con las series guardadas.",
                    confirmLabel = "Completar sesión",
                    onConfirm = {},
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText("¿Completar sesión?").assertIsDisplayed()
        composeRule.onNodeWithText("Se calculará el progreso con las series guardadas.").assertIsDisplayed()
        composeRule.onNodeWithText("Completar sesión").assertIsDisplayed()
        composeRule.onNodeWithText("Volver").assertIsDisplayed()
    }
}
