package com.gymquest.app.core.ui.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gymquest.app.core.ui.theme.GymQuestTheme
import org.junit.Rule
import org.junit.Test

class QuestNavigationComponentsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun topBarExposesTitleAndContextAction() {
        composeRule.setContent {
            GymQuestTheme {
                QuestTopBar(
                    title = "Sesión",
                    contextualAction = QuestAction.More,
                    onContextualAction = {},
                )
            }
        }

        composeRule.onNodeWithText("Sesión").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Mostrar más opciones").assertIsDisplayed()
    }

    @Test
    fun bottomNavigationShowsAllStableDestinationsAtNormalFontScale() {
        composeRule.setContent {
            GymQuestTheme {
                QuestBottomNavigation(
                    currentRoute = "home",
                    onNavigate = {},
                )
            }
        }

        listOf("Inicio", "Sesión", "Historial", "Dojo", "Catálogo").forEach { label ->
            composeRule.onNodeWithText(label).assertIsDisplayed()
        }
        composeRule.onAllNodesWithText("Progreso").assertCountEquals(0)
    }

    @Test
    fun bottomNavigationKeepsAccessibleIconsWhenLabelsAreHiddenForLargeText() {
        composeRule.setContent {
            GymQuestTheme {
                QuestBottomNavigation(
                    currentRoute = "home",
                    onNavigate = {},
                    showLabels = false,
                )
            }
        }

        composeRule.onAllNodesWithText("Inicio").assertCountEquals(0)
        composeRule.onNodeWithContentDescription("Ir a inicio").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Ir a sesion").assertIsDisplayed()
    }
}
