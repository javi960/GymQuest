package com.gymquest.app.feature.catalog

import com.gymquest.app.domain.model.ExerciseBase
import com.gymquest.app.domain.model.ExerciseCatalogEntry
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogEntryPresentationTest {
    @Test
    fun `entry without variants explains the next required step`() {
        assertEquals("Requiere variante", entry(variants = emptyList()).presentation().label)
    }

    @Test
    fun `entry with a variant is ready for training`() {
        assertEquals("Listo para entrenar", entry(variants = listOf(variant())).presentation().label)
    }

    private fun entry(variants: List<ExerciseVariant>) = ExerciseCatalogEntry(
        exerciseBase = ExerciseBase(
            id = 1,
            name = "Sentadilla",
            primaryMuscleGroupId = 2,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
        ),
        variants = variants,
    )

    private fun variant() = ExerciseVariant(
        id = 3,
        exerciseBaseId = 1,
        name = "Barra",
        equipmentType = EquipmentType.BARBELL,
        weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
    )
}
