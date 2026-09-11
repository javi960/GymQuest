package com.gymquest.app.domain.repository

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.*
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface MartialArtsRepository {
    fun observePracticeCount(): Flow<Long>
    suspend fun activeArts(): AppResult<List<MartialArt>>
    suspend fun activeStyles(artId: Long): AppResult<List<MartialStyle>>
    suspend fun activeTechniques(styleId: Long): AppResult<List<MartialTechnique>>
    suspend fun activeStances(styleId: Long): AppResult<List<MartialStance>>
    suspend fun contentDetail(contentId: Long): AppResult<MartialContentDetail>
    suspend fun updateArt(art: MartialArt): AppResult<Unit>
    suspend fun updateStyle(style: MartialStyle): AppResult<Unit>
    suspend fun archiveArt(artId: Long, now: Instant): AppResult<Unit>
    suspend fun archiveStyle(styleId: Long, now: Instant): AppResult<Unit>
    suspend fun addContentStep(step: MartialContentStep): AppResult<Long>
    suspend fun updateContentStep(step: MartialContentStep): AppResult<Unit>
    suspend fun deleteContentStep(stepId: Long): AppResult<Unit>
    suspend fun replaceContentSteps(contentId: Long, steps: List<MartialContentStep>): AppResult<Unit>
    suspend fun replaceContentStepMedia(stepId: Long, mediaFile: MediaFile): AppResult<Long>
    suspend fun removeContentStepMedia(stepId: Long): AppResult<Unit>
    suspend fun createArt(art: MartialArt): AppResult<Long>
    suspend fun createStyle(style: MartialStyle): AppResult<Long>
    /** Adds any missing built-in Shito-Ryu entries for this style, without changing user entries. */
    suspend fun ensureShitoRyuCatalog(styleId: Long): AppResult<Unit>
    suspend fun setCurrentRank(rank: MartialRank): AppResult<Long>
    suspend fun createContent(content: MartialContent): AppResult<Long>
    suspend fun createTechnique(technique: MartialTechnique): AppResult<Long>
    suspend fun createStance(stance: MartialStance): AppResult<Long>
    suspend fun updateContent(content: MartialContent): AppResult<Unit>
    suspend fun updateTechnique(technique: MartialTechnique): AppResult<Unit>
    suspend fun updateStance(stance: MartialStance): AppResult<Unit>
    suspend fun linkTechnique(contentId: Long, techniqueId: Long): AppResult<Unit>
    suspend fun registerPractice(practice: MartialPractice): AppResult<Long>
    suspend fun observeProgress(styleId: Long): AppResult<MartialProgress>
    suspend fun updateContentProgress(contentId: Long, status: String, now: Instant): AppResult<Unit>
    suspend fun archiveContent(contentId: Long, now: Instant): AppResult<Unit>
    suspend fun archiveTechnique(techniqueId: Long, now: Instant): AppResult<Unit>
    suspend fun archiveStance(stanceId: Long, now: Instant): AppResult<Unit>
    suspend fun deleteArt(artId: Long): AppResult<Unit>
    suspend fun deleteStyle(styleId: Long): AppResult<Unit>
    suspend fun deleteContent(contentId: Long): AppResult<Unit>
    suspend fun deleteTechnique(techniqueId: Long): AppResult<Unit>
    suspend fun deleteStance(stanceId: Long): AppResult<Unit>
    suspend fun currentBelt(): AppResult<MartialBelt>
    suspend fun setCurrentBelt(belt: MartialBelt, now: Instant): AppResult<Unit>
}
