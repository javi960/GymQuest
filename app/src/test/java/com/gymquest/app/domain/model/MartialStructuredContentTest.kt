package com.gymquest.app.domain.model

import com.gymquest.app.domain.model.enums.MartialDirection
import com.gymquest.app.domain.model.enums.MartialSide
import com.gymquest.app.domain.model.enums.MartialTechniqueFamily
import com.gymquest.app.domain.model.enums.MediaType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MartialStructuredContentTest {
    private val now = Instant.parse("2026-09-09T10:00:00Z")

    @Test
    fun `a kata step preserves reusable references and its structured movement details`() {
        val step = MartialContentStep(
            technicalContentId = 15,
            orderIndex = 13,
            stanceId = 3,
            techniqueId = 9,
            direction = MartialDirection.FRONT_LEFT,
            side = MartialSide.RIGHT,
            movementType = "advance",
            displacement = "forward",
            turnDegrees = 90,
            angleDegrees = 45,
            description = "Avanza y bloquea en diagonal.",
            hasKiai = true,
            hasPause = true,
            mediaFileId = 24,
            createdAt = now,
            updatedAt = now,
        )

        assertEquals(3L, step.stanceId)
        assertEquals(9L, step.techniqueId)
        assertEquals(MartialDirection.FRONT_LEFT, step.direction)
        assertEquals(MartialSide.RIGHT, step.side)
        assertEquals("forward", step.displacement)
        assertEquals(90, step.turnDegrees)
        assertEquals(45, step.angleDegrees)
        assertEquals(24L, step.mediaFileId)
        assertTrue(step.hasKiai)
        assertTrue(step.hasPause)
    }

    @Test
    fun `long kata sequences have no small practical upper bound`() {
        val steps = (0 until 104).map { index ->
            MartialContentStep(
                technicalContentId = 15,
                orderIndex = index,
                direction = MartialDirection.FRONT,
                createdAt = now,
                updatedAt = now,
            )
        }

        assertEquals(104, steps.size)
        assertEquals((0 until 104).toList(), steps.map(MartialContentStep::orderIndex))
    }

    @Test
    fun `directions cover cardinal and diagonal movement`() {
        assertEquals(
            setOf(
                MartialDirection.FRONT,
                MartialDirection.BACK,
                MartialDirection.LEFT,
                MartialDirection.RIGHT,
                MartialDirection.FRONT_LEFT,
                MartialDirection.FRONT_RIGHT,
                MartialDirection.BACK_LEFT,
                MartialDirection.BACK_RIGHT,
            ),
            MartialDirection.entries.toSet(),
        )
    }

    @Test
    fun `techniques and stances remain independent reusable library entries`() {
        val technique = MartialTechnique(
            id = 9,
            martialStyleId = 2,
            name = "Oi Zuki",
            family = MartialTechniqueFamily.PUNCH,
            createdAt = now,
            updatedAt = now,
        )
        val stance = MartialStance(
            id = 3,
            martialStyleId = 2,
            name = "Zenkutsu Dachi",
            createdAt = now,
            updatedAt = now,
        )

        assertEquals(MartialTechniqueFamily.PUNCH, technique.family)
        assertEquals("Zenkutsu Dachi", stance.name)
        assertFalse(technique.isArchived)
        assertFalse(stance.isArchived)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `a step rejects an invalid reusable technique reference`() {
        MartialContentStep(
            technicalContentId = 15,
            orderIndex = 0,
            techniqueId = 0,
            direction = MartialDirection.FRONT,
            createdAt = now,
            updatedAt = now,
        )
    }

    @Test
    fun `media types support image gif and video for local kata media`() {
        assertEquals(
            setOf(MediaType.IMAGE, MediaType.GIF, MediaType.VIDEO),
            MediaType.entries.toSet(),
        )
    }
}
