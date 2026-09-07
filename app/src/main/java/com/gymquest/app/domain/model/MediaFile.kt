package com.gymquest.app.domain.model

import com.gymquest.app.domain.model.enums.MediaOwnerType
import com.gymquest.app.domain.model.enums.MediaType
import java.time.Instant

data class MediaFile(
    val id: Long = 0,
    val ownerType: MediaOwnerType,
    val ownerId: Long,
    val mediaType: MediaType,
    val localUri: String,
    val title: String? = null,
    val notes: String? = null,
    val createdAt: Instant,
    val isArchived: Boolean = false,
)
