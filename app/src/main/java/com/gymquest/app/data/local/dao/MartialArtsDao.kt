package com.gymquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymquest.app.data.local.entity.MartialArtEntity
import com.gymquest.app.data.local.entity.MartialBeltSettingsEntity
import com.gymquest.app.data.local.entity.MartialContentTechniqueCrossRef
import com.gymquest.app.data.local.entity.MartialPracticeItemEntity
import com.gymquest.app.data.local.entity.MartialPracticeSessionEntity
import com.gymquest.app.data.local.entity.MartialRankEntity
import com.gymquest.app.data.local.entity.MartialStyleEntity
import com.gymquest.app.data.local.entity.MartialStanceEntity
import com.gymquest.app.data.local.entity.MartialContentStepEntity
import com.gymquest.app.data.local.entity.MartialTechnicalContentEntity
import com.gymquest.app.data.local.entity.MartialTechniqueEntity
import com.gymquest.app.data.local.relation.MartialContentWithTechniques
import com.gymquest.app.data.local.relation.MartialPracticeSessionWithItems
import com.gymquest.app.data.local.relation.MartialStyleWithContent

@Dao
interface MartialArtsDao {
    @Query("SELECT * FROM martial_belt_settings WHERE id = 1 LIMIT 1")
    suspend fun getBeltSettings(): MartialBeltSettingsEntity?

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsertBeltSettings(entity: MartialBeltSettingsEntity)

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
    suspend fun insertStance(entity: MartialStanceEntity): Long

    @Insert
    suspend fun insertContentStep(entity: MartialContentStepEntity): Long

    @Insert
    suspend fun insertContentSteps(entities: List<MartialContentStepEntity>)

    @Insert
    suspend fun insertContentTechniqueCrossRef(entity: MartialContentTechniqueCrossRef)

    @Insert
    suspend fun insertPracticeSession(entity: MartialPracticeSessionEntity): Long

    @Insert
    suspend fun insertPracticeItem(entity: MartialPracticeItemEntity): Long

    @Query("SELECT * FROM martial_arts WHERE id = :id LIMIT 1")
    suspend fun getMartialArt(id: Long): MartialArtEntity?

    @Query("SELECT * FROM martial_styles WHERE id = :id LIMIT 1")
    suspend fun getMartialStyle(id: Long): MartialStyleEntity?

    @Query("SELECT * FROM martial_technical_contents WHERE id = :id LIMIT 1")
    suspend fun getTechnicalContent(id: Long): MartialTechnicalContentEntity?

    @Query("SELECT * FROM martial_techniques WHERE id = :id LIMIT 1")
    suspend fun getTechnique(id: Long): MartialTechniqueEntity?

    @Query("SELECT * FROM martial_stances WHERE id = :id LIMIT 1")
    suspend fun getStance(id: Long): MartialStanceEntity?

    @Query("SELECT * FROM martial_content_steps WHERE id = :stepId LIMIT 1")
    suspend fun getContentStep(stepId: Long): MartialContentStepEntity?

    @Query("SELECT * FROM martial_styles WHERE martialArtId = :artId AND isArchived = 0 ORDER BY name")
    suspend fun getActiveStyles(artId: Long): List<MartialStyleEntity>

    @Query("SELECT MAX(startedAt) FROM martial_practice_sessions WHERE martialStyleId = :styleId")
    suspend fun getLastPracticeAt(styleId: Long): java.time.Instant?

    @Query("UPDATE martial_arts SET isArchived = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveMartialArt(id: Long, updatedAt: java.time.Instant): Int
    @Query("UPDATE martial_styles SET isArchived = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveMartialStyle(id: Long, updatedAt: java.time.Instant): Int

    @Query("UPDATE martial_technical_contents SET isArchived = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveTechnicalContent(id: Long, updatedAt: java.time.Instant): Int

    @Query("UPDATE martial_techniques SET isArchived = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveTechnique(id: Long, updatedAt: java.time.Instant): Int

    @Query("UPDATE martial_stances SET isArchived = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun archiveStance(id: Long, updatedAt: java.time.Instant): Int

    @Query("UPDATE martial_technical_contents SET progressStatus = :status, discoveredAt = COALESCE(discoveredAt, :discoveredAt), updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateContentProgress(id: Long, status: String, discoveredAt: java.time.Instant, updatedAt: java.time.Instant): Int

    @Query("UPDATE martial_ranks SET isCurrent = 0, updatedAt = :updatedAt WHERE martialStyleId = :styleId AND isCurrent = 1")
    suspend fun clearCurrentRanks(styleId: Long, updatedAt: java.time.Instant): Int

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

    @Update
    suspend fun updateStance(entity: MartialStanceEntity)

    @Update
    suspend fun updateContentStep(entity: MartialContentStepEntity)

    @Query("UPDATE martial_content_steps SET mediaFileId = :mediaFileId, updatedAt = :updatedAt WHERE id = :stepId")
    suspend fun updateContentStepMedia(stepId: Long, mediaFileId: Long?, updatedAt: java.time.Instant): Int

    /** Replaces a sequence atomically after the editor has calculated a contiguous new ordering. */
    @Transaction
    suspend fun replaceContentSteps(contentId: Long, steps: List<MartialContentStepEntity>) {
        deleteContentSteps(contentId)
        if (steps.isNotEmpty()) insertContentSteps(steps)
    }

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

    @Query("SELECT * FROM martial_stances WHERE martialStyleId = :styleId AND isArchived = 0 ORDER BY name")
    suspend fun getActiveStances(styleId: Long): List<MartialStanceEntity>

    @Query("SELECT * FROM martial_techniques WHERE martialStyleId = :styleId")
    suspend fun getTechniquesForStyle(styleId: Long): List<MartialTechniqueEntity>

    @Query("SELECT * FROM martial_stances WHERE martialStyleId = :styleId")
    suspend fun getStancesForStyle(styleId: Long): List<MartialStanceEntity>

    @Query("SELECT * FROM martial_content_steps WHERE technicalContentId = :contentId ORDER BY orderIndex")
    suspend fun getContentSteps(contentId: Long): List<MartialContentStepEntity>

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

    @Query("DELETE FROM martial_content_steps WHERE technicalContentId = :contentId")
    suspend fun deleteContentSteps(contentId: Long): Int
    @Query("DELETE FROM martial_content_steps WHERE id = :stepId")
    suspend fun deleteContentStepById(stepId: Long): Int
    @Query("UPDATE martial_content_steps SET orderIndex = orderIndex - 1 WHERE technicalContentId = :contentId AND orderIndex > :deletedOrderIndex")
    suspend fun closeContentStepOrderGap(contentId: Long, deletedOrderIndex: Int): Int
    /** Deletes a step and keeps the remaining sequence contiguous. */
    @Transaction
    suspend fun deleteContentStep(stepId: Long): Int {
        val step = getContentStep(stepId) ?: return 0
        val deleted = deleteContentStepById(stepId)
        if (deleted == 1) closeContentStepOrderGap(step.technicalContentId, step.orderIndex)
        return deleted
    }

    @Query("SELECT id FROM martial_styles WHERE martialArtId = :artId")
    suspend fun styleIdsForArt(artId: Long): List<Long>
    @Query("SELECT id FROM martial_technical_contents WHERE martialStyleId = :styleId")
    suspend fun contentIdsForStyle(styleId: Long): List<Long>
    @Query("SELECT id FROM martial_techniques WHERE martialStyleId = :styleId")
    suspend fun techniqueIdsForStyle(styleId: Long): List<Long>
    @Query("SELECT id FROM martial_stances WHERE martialStyleId = :styleId")
    suspend fun stanceIdsForStyle(styleId: Long): List<Long>
    @Query("DELETE FROM martial_practice_items WHERE contentId = :contentId") suspend fun deletePracticeItemsForContent(contentId: Long): Int
    @Query("DELETE FROM martial_practice_items WHERE techniqueId = :techniqueId") suspend fun deletePracticeItemsForTechnique(techniqueId: Long): Int
    @Query("DELETE FROM martial_secondary_missions WHERE technicalContentId = :contentId") suspend fun deleteMissionsForContent(contentId: Long): Int
    @Query("DELETE FROM martial_secondary_missions WHERE techniqueId = :techniqueId") suspend fun deleteMissionsForTechnique(techniqueId: Long): Int
    @Query("DELETE FROM martial_content_techniques WHERE contentId = :contentId") suspend fun deleteLinksForContent(contentId: Long): Int
    @Query("DELETE FROM martial_content_techniques WHERE techniqueId = :techniqueId") suspend fun deleteLinksForTechnique(techniqueId: Long): Int
    @Query("UPDATE martial_content_steps SET techniqueId = NULL WHERE techniqueId = :techniqueId") suspend fun unlinkTechniqueFromSteps(techniqueId: Long): Int
    @Query("UPDATE martial_content_steps SET stanceId = NULL WHERE stanceId = :stanceId") suspend fun unlinkStanceFromSteps(stanceId: Long): Int
    @Query("DELETE FROM martial_technical_contents WHERE id = :contentId") suspend fun deleteContent(contentId: Long): Int
    @Query("DELETE FROM martial_techniques WHERE id = :techniqueId") suspend fun deleteTechnique(techniqueId: Long): Int
    @Query("DELETE FROM martial_stances WHERE id = :stanceId") suspend fun deleteStance(stanceId: Long): Int
    @Query("DELETE FROM martial_ranks WHERE martialStyleId = :styleId") suspend fun deleteRanksForStyle(styleId: Long): Int
    @Query("DELETE FROM martial_practice_items WHERE practiceSessionId IN (SELECT id FROM martial_practice_sessions WHERE martialStyleId = :styleId)") suspend fun deletePracticeItemsForStyle(styleId: Long): Int
    @Query("DELETE FROM martial_practice_sessions WHERE martialStyleId = :styleId") suspend fun deletePracticeSessionsForStyle(styleId: Long): Int
    @Query("DELETE FROM martial_styles WHERE id = :styleId") suspend fun deleteStyle(styleId: Long): Int
    @Query("DELETE FROM martial_arts WHERE id = :artId") suspend fun deleteArt(artId: Long): Int

    @Transaction
    suspend fun deleteContentCascade(contentId: Long): Int { deletePracticeItemsForContent(contentId); deleteMissionsForContent(contentId); deleteLinksForContent(contentId); return deleteContent(contentId) }
    @Transaction
    suspend fun deleteTechniqueCascade(techniqueId: Long): Int { unlinkTechniqueFromSteps(techniqueId); deletePracticeItemsForTechnique(techniqueId); deleteMissionsForTechnique(techniqueId); deleteLinksForTechnique(techniqueId); return deleteTechnique(techniqueId) }
    @Transaction
    suspend fun deleteStanceCascade(stanceId: Long): Int { unlinkStanceFromSteps(stanceId); return deleteStance(stanceId) }
    @Transaction
    suspend fun deleteStyleCascade(styleId: Long): Int { for (contentId in contentIdsForStyle(styleId)) deleteContentCascade(contentId); for (techniqueId in techniqueIdsForStyle(styleId)) deleteTechniqueCascade(techniqueId); for (stanceId in stanceIdsForStyle(styleId)) deleteStanceCascade(stanceId); deleteRanksForStyle(styleId); deletePracticeItemsForStyle(styleId); deletePracticeSessionsForStyle(styleId); return deleteStyle(styleId) }
    @Transaction
    suspend fun deleteArtCascade(artId: Long): Int { for (styleId in styleIdsForArt(artId)) deleteStyleCascade(styleId); return deleteArt(artId) }
}
