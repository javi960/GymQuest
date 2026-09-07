package com.gymquest.app.domain.model

import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ExerciseCatalogEntryTest {
    @Test
    fun keepsBaseAndItsOrderedVariantsTogether() {
        val now = Instant.parse("2026-09-04T10:00:00Z")
        val base = ExerciseBase(
            id = 1,
            name = "Press de banca",
            primaryMuscleGroupId = 2,
            createdAt = now,
            updatedAt = now,
        )
        val variants = listOf(
            ExerciseVariant(
                id = 3,
                exerciseBaseId = base.id,
                name = "Barra",
                equipmentType = EquipmentType.BARBELL,
                weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
                createdAt = now,
                updatedAt = now,
            ),
            ExerciseVariant(
                id = 4,
                exerciseBaseId = base.id,
                name = "Mancuernas",
                equipmentType = EquipmentType.DUMBBELL,
                weightComparisonType = WeightComparisonType.PER_DUMBBELL,
                createdAt = now,
                updatedAt = now,
            ),
        )

        val entry = ExerciseCatalogEntry(exerciseBase = base, variants = variants)

        assertEquals(base, entry.exerciseBase)
        assertEquals(variants, entry.variants)
    }
}
