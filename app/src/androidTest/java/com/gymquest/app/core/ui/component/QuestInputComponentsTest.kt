package com.gymquest.app.core.ui.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.gymquest.app.core.ui.theme.GymQuestTheme
import org.junit.Rule
import org.junit.Test

class QuestInputComponentsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun numericFieldShowsUnitAndLocalizedError() {
        composeRule.setContent {
            GymQuestTheme {
                QuestNumericField(
                    value = "",
                    onValueChange = {},
                    label = "Peso",
                    unit = "kg",
                    errorMessage = "Introduce un peso valido.",
                )
            }
        }

        composeRule.onNodeWithText("Peso").assertIsDisplayed()
        composeRule.onNodeWithText("Introduce un peso valido.").assertIsDisplayed()
    }

    @Test
    fun numericFieldExposesItsLabelAndUnit() {
        composeRule.setContent {
            GymQuestTheme {
                QuestNumericField(
                    value = "8",
                    onValueChange = {},
                    label = "Repeticiones",
                    unit = "reps",
                    integerOnly = true,
                )
            }
        }

        composeRule.onNodeWithText("Repeticiones").assertIsDisplayed()
        composeRule.onNodeWithText("reps").assertIsDisplayed()
    }

    @Test
    fun denseDataRowKeepsEveryMetricVisible() {
        composeRule.setContent {
            GymQuestTheme {
                QuestDenseDataRow(
                    metrics = listOf(
                        QuestDenseMetric("Peso", "82,5 kg"),
                        QuestDenseMetric("Reps", "8 reps"),
                        QuestDenseMetric("Descanso", "60 s"),
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Peso: 82,5 kg").assertIsDisplayed()
        composeRule.onNodeWithText("Reps: 8 reps").assertIsDisplayed()
        composeRule.onNodeWithText("Descanso: 60 s").assertIsDisplayed()
    }

    @Test
    fun numericFieldAcceptsOnlyTheConfiguredDecimalOrIntegerFormat() {
        var decimalValue by mutableStateOf("")
        var integerValue by mutableStateOf("")
        composeRule.setContent {
            GymQuestTheme {
                QuestNumericField(
                    value = decimalValue,
                    onValueChange = { decimalValue = it },
                    label = "Peso",
                    unit = "kg",
                )
                QuestNumericField(
                    value = integerValue,
                    onValueChange = { integerValue = it },
                    label = "Descanso",
                    unit = "s",
                    integerOnly = true,
                )
            }
        }

        composeRule.onNodeWithContentDescription("Peso, unidad kg").performTextInput("82a,5")
        composeRule.onNodeWithContentDescription("Descanso, unidad s").performTextInput("6x0")

        composeRule.runOnIdle {
            org.junit.Assert.assertEquals("82,5", decimalValue)
            org.junit.Assert.assertEquals("60", integerValue)
        }
    }

    @Test
    fun disabledNumericFieldRemainsUnavailableAndKeepsItsAccessibleName() {
        composeRule.setContent {
            GymQuestTheme {
                QuestNumericField(
                    value = "",
                    onValueChange = {},
                    label = "Repeticiones",
                    unit = "reps",
                    integerOnly = true,
                    enabled = false,
                )
            }
        }

        composeRule.onNodeWithContentDescription("Repeticiones, unidad reps")
            .assertIsNotEnabled()
            .assertIsDisplayed()
    }
}
