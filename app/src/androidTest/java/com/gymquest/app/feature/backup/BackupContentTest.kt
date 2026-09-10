package com.gymquest.app.feature.backup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gymquest.app.core.ui.theme.GymQuestTheme
import org.junit.Rule
import org.junit.Test

class BackupContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun exportRequiresAnExplicitConfirmationBeforeOpeningTheDocumentPicker() {
        var pickerRequests = 0
        composeRule.setContent {
            GymQuestTheme {
                BackupContent(
                    isExporting = false,
                    statusMessage = null,
                    onRequestDocument = { pickerRequests++ },
                )
            }
        }

        composeRule.onNodeWithText("Exportar JSON").performClick()
        composeRule.onNodeWithText("Exportar copia de seguridad").assertIsDisplayed()
        composeRule.onNodeWithText("Este archivo contiene datos personales de entrenamiento.").assertIsDisplayed()
        composeRule.runOnIdle { org.junit.Assert.assertEquals(0, pickerRequests) }

        composeRule.onNodeWithText("Elegir ubicación y exportar").performClick()
        composeRule.runOnIdle { org.junit.Assert.assertEquals(1, pickerRequests) }
    }
}
