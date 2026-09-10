package com.gymquest.app.domain.model

import java.time.Instant

data class UserAchievement(
    val id: Long = 0,
    val achievementId: Long,
    val unlockedAt: Instant,
    val relatedEntityType: String? = null,
    val relatedEntityId: Long? = null,
) {
    init {
        require(achievementId > 0) { "El logro desbloqueado debe existir." }
        require(relatedEntityId == null || relatedEntityId > 0) { "La entidad relacionada no es valida." }
    }
}
