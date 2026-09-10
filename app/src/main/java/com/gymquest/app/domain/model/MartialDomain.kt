package com.gymquest.app.domain.model

import com.gymquest.app.domain.model.enums.MartialDirection
import com.gymquest.app.domain.model.enums.MartialSide
import com.gymquest.app.domain.model.enums.MartialTechniqueFamily
import java.time.Instant
import java.time.LocalTime

data class MartialArt(val id: Long = 0, val name: String, val description: String? = null, val isArchived: Boolean = false, val createdAt: Instant, val updatedAt: Instant) { init { require(name.isNotBlank()); require(!updatedAt.isBefore(createdAt)) } }
data class MartialStyle(val id: Long = 0, val martialArtId: Long, val name: String, val description: String? = null, val isArchived: Boolean = false, val createdAt: Instant, val updatedAt: Instant) { init { require(martialArtId > 0); require(name.isNotBlank()); require(!updatedAt.isBefore(createdAt)) } }
data class MartialRank(val id: Long = 0, val martialStyleId: Long, val name: String, val rankOrder: Int = 0, val achievedAt: Instant? = null, val notes: String? = null, val isCurrent: Boolean = false, val createdAt: Instant, val updatedAt: Instant) { init { require(martialStyleId > 0); require(name.isNotBlank()); require(rankOrder >= 0); require(!updatedAt.isBefore(createdAt)) } }
data class MartialContent(val id: Long = 0, val martialStyleId: Long, val name: String, val contentType: String, val description: String? = null, val progressStatus: String = NOT_STARTED, val discoveredAt: Instant? = null, val notes: String? = null, val isArchived: Boolean = false, val createdAt: Instant, val updatedAt: Instant) {
    companion object { const val NOT_STARTED = "not_started" }
}
data class MartialTechnique(val id: Long = 0, val martialStyleId: Long, val name: String, val translation: String? = null, val family: MartialTechniqueFamily? = null, val description: String? = null, val notes: String? = null, val isArchived: Boolean = false, val createdAt: Instant, val updatedAt: Instant) { init { require(martialStyleId > 0); require(name.isNotBlank()); require(!updatedAt.isBefore(createdAt)) } }
data class MartialStance(val id: Long = 0, val martialStyleId: Long, val name: String, val translation: String? = null, val description: String? = null, val notes: String? = null, val isArchived: Boolean = false, val createdAt: Instant, val updatedAt: Instant) { init { require(martialStyleId > 0); require(name.isNotBlank()); require(!updatedAt.isBefore(createdAt)) } }
data class MartialContentStep(val id: Long = 0, val technicalContentId: Long, val orderIndex: Int, val stanceId: Long? = null, val techniqueId: Long? = null, val direction: MartialDirection, val side: MartialSide? = null, val movementType: String? = null, val displacement: String? = null, val turnDegrees: Int? = null, val angleDegrees: Int? = null, val description: String? = null, val hasKiai: Boolean = false, val hasPause: Boolean = false, val mediaFileId: Long? = null, val createdAt: Instant, val updatedAt: Instant) { init { require(technicalContentId > 0); require(orderIndex >= 0); require(stanceId == null || stanceId > 0); require(techniqueId == null || techniqueId > 0); require(mediaFileId == null || mediaFileId > 0); require(!updatedAt.isBefore(createdAt)) } }
data class MartialPracticeItem(val contentId: Long? = null, val techniqueId: Long? = null, val repetitions: Int? = null, val minutes: Int? = null, val confidence: Int? = null, val difficulty: Int? = null, val progressStatusAfter: String? = null, val notes: String? = null) {
    init { require((contentId == null) != (techniqueId == null)) { "La práctica debe tener un único destino técnico." } }
}
data class MartialPractice(val martialStyleId: Long, val startedAt: Instant, val durationSeconds: Long, val items: List<MartialPracticeItem>, val instructor: String? = null, val location: String? = null, val notes: String? = null) { init { require(martialStyleId > 0); require(durationSeconds > 0); require(items.isNotEmpty()) } }
data class MartialProgress(val style: MartialStyle, val content: List<MartialContent>, val lastPracticeAt: Instant? = null) {
    val discoveredCount: Int get() = content.count { it.discoveredAt != null }
}
data class MartialContentDetail(
    val content: MartialContent,
    val techniques: List<MartialTechnique>,
    val steps: List<MartialContentStep>,
    val stances: List<MartialStance> = emptyList(),
)

enum class MartialMissionStatus { SCHEDULED, COMPLETED, POSTPONED, DISMISSED, EXPIRED }
data class MartialReminderSettings(val enabled: Boolean = false, val missionsPerDay: Int = 1, val privacyMode: PrivacyMode = PrivacyMode.GENERIC, val maxDailyXp: Long = 20) { init { require(missionsPerDay in 1..5); require(maxDailyXp >= 0) } }
enum class PrivacyMode { GENERIC, DETAILED }
data class MartialReminderWindow(val startLocalTime: LocalTime, val endLocalTime: LocalTime, val enabled: Boolean = true, val sortOrder: Int) { init { require(startLocalTime < endLocalTime); require(sortOrder >= 0) } }
data class MartialSecondaryMission(val id: Long = 0, val technicalContentId: Long? = null, val techniqueId: Long? = null, val scheduledFor: Instant, val status: MartialMissionStatus = MartialMissionStatus.SCHEDULED, val completedAt: Instant? = null, val xpAwarded: Long = 0, val createdPracticeSessionId: Long? = null, val createdAt: Instant, val updatedAt: Instant) { init { require((technicalContentId == null) != (techniqueId == null)); require(xpAwarded >= 0); require(!updatedAt.isBefore(createdAt)); require(completedAt == null || !completedAt.isBefore(scheduledFor)) } }
