package com.gymquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymquest.app.data.local.entity.MartialArtEntity
import com.gymquest.app.data.local.entity.MartialContentTechniqueCrossRef
import com.gymquest.app.data.local.entity.MartialPracticeItemEntity
import com.gymquest.app.data.local.entity.MartialPracticeSessionEntity
import com.gymquest.app.data.local.entity.MartialRankEntity
import com.gymquest.app.data.local.entity.MartialStyleEntity
import com.gymquest.app.data.local.entity.MartialTechnicalContentEntity
import com.gymquest.app.data.local.entity.MartialTechniqueEntity
import com.gymquest.app.data.local.relation.MartialContentWithTechniques
import com.gymquest.app.data.local.relation.MartialPracticeSessionWithItems
import com.gymquest.app.data.local.relation.MartialStyleWithContent

@Dao
interface MartialArtsDao {
    @Insert
    suspend fun insertMartialArt(entity: MartialArtEntity): Long

    @Insert
    suspend fun insertMartialStyle(entity: MartialStyleEntity): Long

    @Insert
    suspend fun insertMartialRank(entity: MartialRankEntity): Long

    @Insert
    suspend fun insertTechnicalContent(entity: MartialTechnicalContentEntity): Long

    @Insert
    suspend fun insertTechnique(entity: MartialTechniqueEntity): Long

    @Insert
    suspend fun insertContentTechniqueCrossRef(entity: MartialContentTechniqueCrossRef)

    @Insert
    suspend fun insertPracticeSession(entity: MartialPracticeSessionEntity): Long

    @Insert
    suspend fun insertPracticeItem(entity: MartialPracticeItemEntity): Long

    @Update
    suspend fun updateMartialArt(entity: MartialArtEntity)

    @Update
    suspend fun updateMartialStyle(entity: MartialStyleEntity)

    @Update
    suspend fun updateMartialRank(entity: MartialRankEntity)

    @Update
    suspend fun updateTechnicalContent(entity: MartialTechnicalContentEntity)

    @Update
    suspend fun updateTechnique(entity: MartialTechniqueEntity)

    @Query("SELECT * FROM martial_arts WHERE isArchived = 0 ORDER BY name")
    suspend fun getActiveMartialArts(): List<MartialArtEntity>

    @Transaction
    @Query("SELECT * FROM martial_styles WHERE id = :styleId LIMIT 1")
    suspend fun getMartialStyleWithContent(styleId: Long): MartialStyleWithContent?

    @Transaction
    @Query("SELECT * FROM martial_technical_contents WHERE id = :contentId LIMIT 1")
    suspend fun getTechnicalContentWithTechniques(contentId: Long): MartialContentWithTechniques?

    @Transaction
    @Query("SELECT * FROM martial_practice_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getPracticeSessionWithItems(sessionId: Long): MartialPracticeSessionWithItems?

    @Query("SELECT * FROM martial_technical_contents WHERE martialStyleId = :styleId AND isArchived = 0 ORDER BY name")
    suspend fun getActiveTechnicalContents(styleId: Long): List<MartialTechnicalContentEntity>

    @Query("SELECT * FROM martial_techniques WHERE martialStyleId = :styleId AND isArchived = 0 ORDER BY name")
    suspend fun getActiveTechniques(styleId: Long): List<MartialTechniqueEntity>

    @Query(
        """
        SELECT martial_techniques.*
        FROM martial_techniques
        INNER JOIN martial_content_techniques
            ON martial_content_techniques.techniqueId = martial_techniques.id
        WHERE martial_content_techniques.contentId = :contentId
        ORDER BY martial_content_techniques.orderIndex
        """,
    )
    suspend fun getTechniquesForContent(contentId: Long): List<MartialTechniqueEntity>

    @Query("DELETE FROM martial_content_techniques WHERE contentId = :contentId AND techniqueId = :techniqueId")
    suspend fun deleteContentTechniqueCrossRef(contentId: Long, techniqueId: Long): Int
}
