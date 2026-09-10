package com.gymquest.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "martial_question_categories", indices = [Index(value = ["name"], unique = true)])
data class MartialQuestionCategoryEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val createdAt: Instant, val updatedAt: Instant)

@Entity(tableName = "martial_questions", foreignKeys = [ForeignKey(entity = MartialQuestionCategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"])], indices = [Index(value = ["categoryId"]), Index(value = ["isArchived"])])
data class MartialQuestionEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val categoryId: Long, val prompt: String, val explanation: String, val difficulty: String, val isArchived: Boolean = false, val createdAt: Instant, val updatedAt: Instant)

@Entity(tableName = "martial_answer_options", foreignKeys = [ForeignKey(entity = MartialQuestionEntity::class, parentColumns = ["id"], childColumns = ["questionId"], onDelete = ForeignKey.CASCADE)], indices = [Index(value = ["questionId"])])
data class MartialAnswerOptionEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val questionId: Long, val text: String, val isCorrect: Boolean, val sortOrder: Int, val createdAt: Instant, val updatedAt: Instant)
