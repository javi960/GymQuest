package com.gymquest.app.data.external

import com.gymquest.app.domain.model.enums.EquipmentType
import org.junit.Assert.assertEquals
import org.junit.Test

class ExternalExerciseMetadataAdapterTest {
    @Test
    fun adapter_keepsDatasetBodyPartGranularityInSpanish() {
        val upperArms = ExternalExerciseMetadataAdapter.adapt(ExternalExerciseMetadata(id = "1", name = "Curl", bodyPart = "upper arms"))
        val lowerArms = ExternalExerciseMetadataAdapter.adapt(ExternalExerciseMetadata(id = "2", name = "Curl de muñeca", bodyPart = "lower arms"))
        val upperLegs = ExternalExerciseMetadataAdapter.adapt(ExternalExerciseMetadata(id = "3", name = "Sentadilla", bodyPart = "upper legs"))

        assertEquals("Brazos superiores", upperArms.bodyPartFilter)
        assertEquals("Antebrazos", lowerArms.bodyPartFilter)
        assertEquals("Piernas superiores", upperLegs.bodyPartFilter)
    }

    @Test
    fun adapter_normalizesExternalTaxonomyWithoutRetainingMediaUrls() {
        val result = ExternalExerciseMetadataAdapter.adapt(
            ExternalExerciseMetadata(
                id = " external-row ",
                name = " Remo con banda ",
                muscle = "lats",
                bodyPart = "back",
                equipment = "band",
                category = "strength",
                instructions = listOf("  Controla el regreso. ", ""),
            ),
        )

        assertEquals("external-row", result.sourceId)
        assertEquals("Espalda", result.primaryMuscleFilter)
        assertEquals("Espalda", result.bodyPartFilter)
        assertEquals(EquipmentType.ELASTIC_BAND, result.equipmentType)
        assertEquals("Fuerza", result.categoryFilter)
        assertEquals(listOf("Controla el regreso."), result.instructions)
    }
}
