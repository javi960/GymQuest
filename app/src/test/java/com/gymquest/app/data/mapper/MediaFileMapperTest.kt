package com.gymquest.app.data.mapper

import com.gymquest.app.data.local.entity.MediaFileEntity
import com.gymquest.app.domain.model.MediaFile
import com.gymquest.app.domain.model.enums.MediaOwnerType
import com.gymquest.app.domain.model.enums.MediaType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class MediaFileMapperTest {
    @Test
    fun mediaMapper_usesStableStringsAndRoundTripsAllMartialOwners() {
        val media = MediaFile(
            id = 4,
            ownerType = MediaOwnerType.MARTIAL_CONTENT_STEP,
            ownerId = 8,
            mediaType = MediaType.VIDEO,
            localUri = "content://local/video/8",
            thumbnailUri = "content://local/image/8",
            createdAt = Instant.parse("2026-09-09T09:00:00Z"),
        )

        val entity = MediaFileMapper.toEntity(media)

        assertEquals("martial_content_step", entity.ownerType)
        assertEquals("video", entity.mediaType)
        assertEquals(media, MediaFileMapper.toDomain(entity))
    }

    @Test
    fun mediaMapper_rejectsUnknownRoomStrings() {
        val entity = MediaFileEntity(
            ownerType = "unknown_owner",
            ownerId = 1,
            mediaType = "image",
            localUri = "content://local/image/1",
            createdAt = Instant.now(),
        )

        try {
            MediaFileMapper.toDomain(entity)
            fail("An unknown owner type must not be accepted")
        } catch (_: IllegalArgumentException) {
            // Expected: corrupt or unsupported persisted values are never silently accepted.
        }
    }
}
