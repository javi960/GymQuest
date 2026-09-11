package com.gymquest.app.data.seed

import com.gymquest.app.domain.model.enums.EquipmentType
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseDatasetSeedMapperTest {
    @Test
    fun mapper_keepsSpanishTrainingInformationAndItsVerifiedGifReference() {
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
                gif_url = "file:///android_asset/exercise_gifs/2gPfomN.gif",
            ),
        )

        assertEquals("Core", entry.muscleGroupName)
        assertEquals(EquipmentType.BODYWEIGHT, entry.equipmentType)
        assertEquals("Abdominal de tres cuartos", entry.exerciseName)
        assertEquals("3/4 sit-up", entry.sourceExerciseName)
        assert(entry.description.contains("Objetivo: abs."))
        assertEquals("hip flexors, lower back", entry.secondaryMuscles)
        assert(entry.instructions.orEmpty().contains("Túmbate."))
        assertEquals(
            "file:///android_asset/exercise_gifs/2gPfomN.gif",
            entry.builtInGifUrl,
        )
    }

    @Test
    fun mapper_rejectsGifUrlsOutsideTheVerifiedCatalogue() {
        val entry = ExerciseDatasetSeedMapper.map(
            ExerciseDatasetRecord(
                id = "0002",
                name = "Exercise",
                category = "chest",
                body_part = "chest",
                equipment = "body weight",
                target = "pectorals",
                muscle_group = "pectorals",
                gif_url = "https://example.com/untrusted.gif",
            ),
        )

        assertEquals(null, entry.builtInGifUrl)
    }
}
