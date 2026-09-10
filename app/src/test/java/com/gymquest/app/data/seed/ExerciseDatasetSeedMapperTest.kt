package com.gymquest.app.data.seed

import com.gymquest.app.domain.model.enums.EquipmentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ExerciseDatasetSeedMapperTest {
    @Test
    fun mapper_keepsSpanishTrainingInformationAndDropsThirdPartyMedia() {
        val entry = ExerciseDatasetSeedMapper.map(
            ExerciseDatasetRecord(
                id = "0001",
                name = "3/4 sit-up",
                name_es = "Abdominal de tres cuartos",
                category = "waist",
                body_part = "waist",
                equipment = "body weight",
                target = "abs",
                muscle_group = "hip flexors",
                secondary_muscles = listOf("hip flexors", "lower back"),
                instruction_steps = mapOf("es" to listOf("Túmbate.", "Eleva el tronco.")),
            ),
        )

        assertEquals("Core", entry.muscleGroupName)
        assertEquals(EquipmentType.BODYWEIGHT, entry.equipmentType)
        assertEquals("Abdominal de tres cuartos", entry.exerciseName)
        assertEquals("3/4 sit-up", entry.sourceExerciseName)
        assert(entry.description.contains("Objetivo: abs."))
        assert(entry.description.contains("Túmbate."))
        assertFalse(entry.description.contains("gif"))
        assertFalse(entry.description.contains("image"))
    }
}
