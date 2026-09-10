package com.gymquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymquest.app.data.local.entity.MartialAnswerOptionEntity
import com.gymquest.app.data.local.entity.MartialQuestionCategoryEntity
import com.gymquest.app.data.local.entity.MartialQuestionEntity

@Dao
interface MartialMiniGameDao {
    @Insert suspend fun insertCategory(entity: MartialQuestionCategoryEntity): Long
    @Insert suspend fun insertQuestion(entity: MartialQuestionEntity): Long
    @Insert suspend fun insertOptions(entities: List<MartialAnswerOptionEntity>)
    @Update suspend fun updateQuestion(entity: MartialQuestionEntity): Int
    @Query("SELECT * FROM martial_question_categories ORDER BY name") suspend fun categories(): List<MartialQuestionCategoryEntity>
    @Query("SELECT * FROM martial_questions WHERE id = :id LIMIT 1") suspend fun question(id: Long): MartialQuestionEntity?
    @Query("SELECT * FROM martial_questions WHERE isArchived = 0 ORDER BY createdAt") suspend fun activeQuestions(): List<MartialQuestionEntity>
    @Query("SELECT * FROM martial_answer_options WHERE questionId = :questionId ORDER BY sortOrder") suspend fun options(questionId: Long): List<MartialAnswerOptionEntity>
    @Query("UPDATE martial_questions SET isArchived = 1, updatedAt = :updatedAt WHERE id = :id") suspend fun archive(id: Long, updatedAt: java.time.Instant): Int
    @Transaction suspend fun replaceOptions(questionId: Long, options: List<MartialAnswerOptionEntity>) { deleteOptions(questionId); insertOptions(options) }
    @Query("DELETE FROM martial_answer_options WHERE questionId = :questionId") suspend fun deleteOptions(questionId: Long): Int
}
