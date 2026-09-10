package com.gymquest.app.core.ui.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.gymquest.app.core.ui.theme.GymQuestTheme
import org.junit.Rule
import org.junit.Test

class QuestCatalogControlsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun sharedSearchAndFilterControlsExposeTheirLabels() {
        composeRule.setContent {
            GymQuestTheme {
                QuestSearchField(value = "", onValueChange = {})
                QuestFilterChip(label = "Grupo actual", selected = false, onClick = {})
                QuestTextField(
                    value = "",
                    onValueChange = {},
                    label = "Ejercicio base",
                    singleLine = true,
                )
                QuestSingleChoiceMenu(
                    selected = "Trabajo",
                    options = listOf("Trabajo", "Calentamiento"),
                    label = "Tipo",
                    optionLabel = { it },
                    onSelectedChange = {},
                )
            }
        }

        composeRule.onNodeWithText("Buscar ejercicios").assertIsDisplayed()
        composeRule.onNodeWithText("Grupo actual").assertIsDisplayed()
        composeRule.onNodeWithText("Ejercicio base").assertIsDisplayed()
        composeRule.onNodeWithText("Tipo").assertIsDisplayed()
    }
}
