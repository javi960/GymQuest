package com.gymquest.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MartialMiniGameTest {
    @Test fun `a valid martial question has exactly one correct response`() {
        val question = MartialQuestion(
            category = MartialQuestionCategory.STRIKES,
            prompt = "¿Qué golpe se ejecuta con el puño?",
            explanation = "Tsuki es un golpe de puño.",
            difficulty = MartialQuestionDifficulty.EASY,
            options = listOf(MartialAnswerOption(text = "Tsuki", isCorrect = true), MartialAnswerOption(text = "Geri", isCorrect = false)),
        )
        assertEquals(2, question.options.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a question rejects more than one correct response`() {
        MartialQuestion(
            category = MartialQuestionCategory.BODY_PARTS,
            prompt = "Pregunta",
            explanation = "Explicación",
            difficulty = MartialQuestionDifficulty.EASY,
            options = listOf(MartialAnswerOption(text = "A", isCorrect = true), MartialAnswerOption(text = "B", isCorrect = true)),
        )
    }
}
