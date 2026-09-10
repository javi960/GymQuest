package com.gymquest.app.domain.repository

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.MartialQuestion
import com.gymquest.app.domain.model.MartialQuestionResult
import java.time.Instant

interface MartialMiniGameRepository {
    suspend fun createCategory(name: String, now: Instant): AppResult<Long>
    suspend fun createQuestion(categoryId: Long, question: MartialQuestion, now: Instant): AppResult<Long>
    suspend fun questions(): AppResult<List<MartialQuestion>>
    suspend fun answer(questionId: Long, optionId: Long): AppResult<MartialQuestionResult>
    suspend fun archive(questionId: Long, now: Instant): AppResult<Unit>
}
