package com.gymquest.app.data.mapper

import com.gymquest.app.data.local.entity.ExerciseBaseEntity
import com.gymquest.app.data.local.entity.ExerciseVariantEntity
import com.gymquest.app.data.local.entity.GymEntity
import com.gymquest.app.data.local.entity.GymMachineEntity
import com.gymquest.app.data.local.entity.MuscleGroupEntity
import com.gymquest.app.data.local.relation.ExerciseBaseWithVariants
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class CatalogMappersTest {
    private val now = Instant.parse("2026-09-04T10:00:00Z")

    @Test
    fun exerciseMapper_roundTripsEntitiesAndCreatesDomainCatalogEntry() {
        val muscleGroup = MuscleGroupEntity(1, "Chest", "Upper body", 2, true)
        val base = ExerciseBaseEntity(2, "Press", 1, "Bench press", true, false, now, now)
        val variant = ExerciseVariantEntity(
            3,
            2,
            "Dumbbells",
            EquipmentType.DUMBBELL,
            WeightComparisonType.PER_DUMBBELL,
            "Incline",
            true,
            false,
            now,
            now,
        )

        assertEquals(muscleGroup, ExerciseMapper.toEntity(ExerciseMapper.toDomain(muscleGroup)))
        assertEquals(base, ExerciseMapper.toEntity(ExerciseMapper.toDomain(base)))
        assertEquals(variant, ExerciseMapper.toEntity(ExerciseMapper.toDomain(variant)))
        assertEquals(
            ExerciseMapper.toDomain(base),
            ExerciseMapper.toCatalogEntry(ExerciseBaseWithVariants(base, listOf(variant))).exerciseBase,
        )
        assertEquals(
            listOf(ExerciseMapper.toDomain(variant)),
            ExerciseMapper.toCatalogEntry(ExerciseBaseWithVariants(base, listOf(variant))).variants,
        )
    }

    @Test
    fun machineMapper_roundTripsGymAndMachineWithOptionalAssociations() {
        val gym = GymEntity(1, "Gym Quest", "24 hours", true, false, now, now)
        val machine = GymMachineEntity(
            2,
            1,
            null,
            "Cable station",
            "Quest",
            "Selectorized",
            WeightComparisonType.NOT_COMPARABLE_BETWEEN_MACHINES,
            "Near the entrance",
            true,
            now,
            now,
        )

        assertEquals(gym, MachineMapper.toEntity(MachineMapper.toDomain(gym)))
        assertEquals(machine, MachineMapper.toEntity(MachineMapper.toDomain(machine)))
    }
}
