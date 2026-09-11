package com.gymquest.app.feature.catalog

import com.gymquest.app.domain.model.ExerciseBase
import com.gymquest.app.domain.model.ExerciseCatalogEntry
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogFilterTest {
    @Test
    fun `filters by selected muscle group and matches exercise or variant names`() {
        val entries = listOf(
            entry(id = 1, name = "Press banca", groupId = 10),
            entry(id = 2, name = "Remo", groupId = 20, variantName = "Polea baja"),
        )

        assertEquals(listOf(1L), entries.filterCatalog(query = "press", muscleGroupId = 10, equipmentType = null).map { it.exerciseBase.id })
        assertEquals(listOf(2L), entries.filterCatalog(query = "", muscleGroupId = 20, equipmentType = null).map { it.exerciseBase.id })
        assertEquals(listOf(2L), entries.filterCatalog(query = "polea", muscleGroupId = null, equipmentType = null).map { it.exerciseBase.id })
        assertEquals(listOf(2L), entries.filterCatalog(query = "", muscleGroupId = null, equipmentType = EquipmentType.CABLE).map { it.exerciseBase.id })
    }

    @Test
    fun `matches educational exercise content`() {
        val entry = ExerciseCatalogEntry(
            exerciseBase = ExerciseBase(
                id = 1,
                name = "Press banca",
                primaryMuscleGroupId = 10,
                instructions = "Baja la barra de forma controlada.",
                techniqueTips = "Mantén las escápulas estables.",
                commonMistakes = "No rebotes la barra.",
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
            ),
            variants = emptyList(),
        )

        assertEquals(listOf(1L), listOf(entry).filterCatalog("escápulas", null, null).map { it.exerciseBase.id })
        assertEquals(listOf(1L), listOf(entry).filterCatalog("rebotes", null, null).map { it.exerciseBase.id })
    }

    private fun entry(id: Long, name: String, groupId: Long, variantName: String? = null) = ExerciseCatalogEntry(
        exerciseBase = ExerciseBase(
            id = id,
            name = name,
            primaryMuscleGroupId = groupId,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
        ),
        variants = variantName?.let {
            listOf(ExerciseVariant(
                id = id + 100,
                exerciseBaseId = id,
                name = it,
                equipmentType = EquipmentType.CABLE,
                weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
            ))
        }.orEmpty(),
    )
}
