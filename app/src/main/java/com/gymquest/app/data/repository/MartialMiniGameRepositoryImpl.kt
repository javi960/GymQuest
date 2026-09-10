package com.gymquest.app.data.repository

import androidx.room.withTransaction
import com.gymquest.app.core.result.AppError
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.data.local.GymQuestDatabase
import com.gymquest.app.data.local.entity.*
import com.gymquest.app.domain.model.*
import com.gymquest.app.domain.repository.MartialMiniGameRepository
import java.time.Instant

class MartialMiniGameRepositoryImpl(private val database: GymQuestDatabase) : MartialMiniGameRepository {
    private val dao = database.martialMiniGameDao()
    override suspend fun createCategory(name: String, now: Instant) = guarded { require(name.isNotBlank()); dao.insertCategory(MartialQuestionCategoryEntity(name = name.trim(), createdAt = now, updatedAt = now)) }
    override suspend fun createQuestion(categoryId: Long, question: MartialQuestion, now: Instant) = guarded { database.withTransaction { require(dao.categories().any { it.id == categoryId }) { "La categoría no existe." }; val id = dao.insertQuestion(MartialQuestionEntity(categoryId = categoryId, prompt = question.prompt.trim(), explanation = question.explanation.trim(), difficulty = question.difficulty.name, isArchived = question.isArchived, createdAt = now, updatedAt = now)); dao.insertOptions(question.options.mapIndexed { index, option -> MartialAnswerOptionEntity(questionId = id, text = option.text.trim(), isCorrect = option.isCorrect, sortOrder = index, createdAt = now, updatedAt = now) }); id } }
    override suspend fun questions() = guarded { val categories = dao.categories().associateBy { it.id }; dao.activeQuestions().map { question -> val category = categories[question.categoryId]?.name ?: "CUSTOM"; MartialQuestion(question.id, runCatching { MartialQuestionCategory.valueOf(category.uppercase().replace(' ', '_')) }.getOrDefault(MartialQuestionCategory.CUSTOM), question.prompt, question.explanation, MartialQuestionDifficulty.valueOf(question.difficulty), dao.options(question.id).map { MartialAnswerOption(it.id, it.text, it.isCorrect) }, question.isArchived) } }
    override suspend fun answer(questionId: Long, optionId: Long) = guarded { val question = requireNotNull(dao.question(questionId)) { "La pregunta no existe." }; val option = requireNotNull(dao.options(questionId).firstOrNull { it.id == optionId }) { "La respuesta no pertenece a la pregunta." }; MartialQuestionResult(questionId, optionId, option.isCorrect, question.explanation) }
    override suspend fun archive(questionId: Long, now: Instant) = guarded { require(dao.archive(questionId, now) == 1) { "La pregunta no existe." } }
    private suspend fun <T> guarded(block: suspend () -> T): AppResult<T> = try { AppResult.Success(block()) } catch (e: IllegalArgumentException) { AppResult.Failure(AppError.Validation(e.message ?: "Pregunta no válida.")) } catch (e: Exception) { AppResult.Failure(AppError.Storage("No se pudo guardar el minijuego.", e)) }
}
