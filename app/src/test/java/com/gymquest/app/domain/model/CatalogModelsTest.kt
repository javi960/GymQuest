package com.gymquest.app.domain.model

import com.gymquest.app.domain.model.enums.MediaOwnerType
import com.gymquest.app.domain.model.enums.MediaType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class CatalogModelsTest {
    private val now = Instant.parse("2026-09-04T10:00:00Z")

    @Test
    fun catalogModels_defaultToActiveAndKeepOptionalFieldsNullable() {
        val muscleGroup = MuscleGroup(name = "Pecho", sortOrder = 0)
        val gym = Gym(name = "Gym Quest", createdAt = now, updatedAt = now)
        val machine = GymMachine(
            gymId = 1,
            name = "Press de pecho",
            weightComparisonType = WeightComparisonType.NOT_COMPARABLE_BETWEEN_MACHINES,
            createdAt = now,
            updatedAt = now,
        )

        assertFalse(muscleGroup.isArchived)
        assertFalse(gym.isArchived)
        assertFalse(machine.isArchived)
        assertNull(machine.exerciseVariantId)
    }

    @Test
    fun mediaFile_usesTypedOwnerAndMediaKinds() {
        val media = MediaFile(
            ownerType = MediaOwnerType.MARTIAL_TECHNIQUE,
            ownerId = 8,
            mediaType = MediaType.VIDEO,
            localUri = "content://media/external/video/media/8",
            createdAt = now,
        )

        assertEquals(MediaOwnerType.MARTIAL_TECHNIQUE, media.ownerType)
        assertEquals(MediaType.VIDEO, media.mediaType)
    }
}
