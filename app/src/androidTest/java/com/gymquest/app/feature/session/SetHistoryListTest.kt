package com.gymquest.app.feature.session

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.gymquest.app.core.ui.theme.GymQuestTheme
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.SetType
import java.time.Instant
import org.junit.Rule
import org.junit.Test

class SetHistoryListTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun savedSetHasAnAccessibleResultIcon() {
        composeRule.setContent {
            GymQuestTheme {
                SetHistoryList(
                    sets = listOf(
                        WorkoutSet(
                            id = 1,
                            workoutExerciseId = 1,
                            setNumber = 1,
                            weightValue = 80.0,
                            reps = 8,
                            setType = SetType.WORK,
                            createdAt = Instant.EPOCH,
                            updatedAt = Instant.EPOCH,
                        ),
                    ),
                    onUpdateSet = { _, _, _, _ -> },
                    onDeleteSet = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Serie guardada").assertIsDisplayed()
    }
}
