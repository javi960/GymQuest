package com.gymquest.app.core.ui.component

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.SemanticsMatcher
import com.gymquest.app.core.ui.theme.GymQuestTheme
import org.junit.Rule
import org.junit.Test

class QuestButtonStateContractTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun actionButtonExposesNormalSelectedLoadingAndDisabledStatesSemantically() {
        composeRule.setContent {
            GymQuestTheme {
                QuestActionButton(QuestAction.Add, onClick = {})
                QuestActionButton(QuestAction.Edit, onClick = {}, state = QuestButtonState.Selected)
                QuestActionButton(QuestAction.Save, onClick = {}, state = QuestButtonState.Loading)
                QuestActionButton(QuestAction.Cancel, onClick = {}, state = QuestButtonState.Disabled)
            }
        }

        composeRule.onNodeWithContentDescription("Añadir elemento")
            .assertIsDisplayed()
            .assertHasClickAction()
            .assert(hasState(QuestButtonVisualState.Normal))
        composeRule.onNodeWithContentDescription("Editar elemento")
            .assert(hasState(QuestButtonVisualState.Selected))
        composeRule.onNodeWithContentDescription("Guardar cambios")
            .assert(hasState(QuestButtonVisualState.Loading))
        composeRule.onNodeWithText("Cargando").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Cancelar acción")
            .assertIsNotEnabled()
            .assert(hasState(QuestButtonVisualState.Disabled))
    }

    @Test
    fun destructiveActionKeepsItsSemanticRoleAndConfirmationRequirement() {
        composeRule.setContent {
            GymQuestTheme {
                QuestActionButton(QuestAction.Delete, onClick = {})
            }
        }

        composeRule.onNodeWithContentDescription("Eliminar elemento")
            .assert(hasRole(QuestActionRole.Destructive))
            .assert(requiresConfirmation())
    }

    @Test
    fun actionButtonExposesPressedAndFocusFeedbackStates() {
        composeRule.setContent {
            GymQuestTheme {
                QuestActionButton(QuestAction.Start, onClick = {}, state = QuestButtonState.Pressed)
                QuestActionButton(QuestAction.Resume, onClick = {}, state = QuestButtonState.Focused)
            }
        }

        composeRule.onNodeWithContentDescription("Iniciar sesión")
            .assert(hasState(QuestButtonVisualState.Pressed))
        composeRule.onNodeWithContentDescription("Reanudar")
            .assert(hasState(QuestButtonVisualState.Focused))
    }

    @Test
    fun iconAndOutlinedButtonsKeepTheSharedActionSemantics() {
        composeRule.setContent {
            GymQuestTheme {
                QuestIconButton(QuestAction.Back, onClick = {}, enabled = false)
                QuestButton(
                    text = "Editar ejercicio",
                    action = QuestAction.Edit,
                    onClick = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Volver atrás")
            .assertIsNotEnabled()
            .assert(hasState(QuestButtonVisualState.Disabled))
        composeRule.onNodeWithContentDescription("Editar elemento")
            .assertHasClickAction()
            .assert(hasRole(QuestActionRole.Secondary))
    }

    private fun hasState(state: QuestButtonVisualState) =
        SemanticsMatcher.expectValue(QuestButtonSemantics.visualStateKey, state)

    private fun hasRole(role: QuestActionRole) =
        SemanticsMatcher.expectValue(QuestButtonSemantics.actionRoleKey, role)

    private fun requiresConfirmation() =
        SemanticsMatcher.expectValue(QuestButtonSemantics.requiresConfirmationKey, true)
}
