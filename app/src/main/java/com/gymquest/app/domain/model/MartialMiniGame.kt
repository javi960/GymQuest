package com.gymquest.app.domain.model

enum class MartialQuestionCategory { STRIKES, BODY_PARTS, CUSTOM }
enum class MartialQuestionDifficulty { EASY, MEDIUM, HARD }

data class MartialQuestion(
    val id: Long = 0,
    val category: MartialQuestionCategory,
    val prompt: String,
    val explanation: String,
    val difficulty: MartialQuestionDifficulty,
    val options: List<MartialAnswerOption>,
    val isArchived: Boolean = false,
) {
    init { require(prompt.isNotBlank() && explanation.isNotBlank()); require(options.size in 2..6); require(options.count { it.isCorrect } == 1) }
}
data class MartialAnswerOption(val id: Long = 0, val text: String, val isCorrect: Boolean) { init { require(text.isNotBlank()) } }
data class MartialQuestionResult(val questionId: Long, val selectedOptionId: Long, val isCorrect: Boolean, val explanation: String)
