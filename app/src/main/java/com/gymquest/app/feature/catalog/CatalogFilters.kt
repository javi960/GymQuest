package com.gymquest.app.feature.catalog

import com.gymquest.app.domain.model.ExerciseCatalogEntry
import com.gymquest.app.domain.model.enums.EquipmentType

fun List<ExerciseCatalogEntry>.filterCatalog(
    query: String,
    muscleGroupId: Long?,
    equipmentType: EquipmentType?,
): List<ExerciseCatalogEntry> = asSequence()
    .filter { entry -> muscleGroupId == null || entry.exerciseBase.primaryMuscleGroupId == muscleGroupId }
    .filter { entry -> equipmentType == null || entry.variants.any { it.equipmentType == equipmentType } }
    .filter { entry ->
        query.isBlank() || entry.exerciseBase.name.contains(query, ignoreCase = true) ||
            entry.exerciseBase.description.orEmpty().contains(query, ignoreCase = true) ||
            entry.exerciseBase.secondaryMuscles.orEmpty().contains(query, ignoreCase = true) ||
            entry.exerciseBase.instructions.orEmpty().contains(query, ignoreCase = true) ||
            entry.exerciseBase.techniqueTips.orEmpty().contains(query, ignoreCase = true) ||
            entry.exerciseBase.commonMistakes.orEmpty().contains(query, ignoreCase = true) ||
            entry.variants.any { it.name.contains(query, ignoreCase = true) }
    }
    .sortedBy { it.exerciseBase.name.lowercase() }
    .toList()

fun EquipmentType.catalogLabel(): String = when (this) {
    EquipmentType.BARBELL -> "Barra"
    EquipmentType.DUMBBELL -> "Mancuernas"
    EquipmentType.MACHINE -> "Máquina"
    EquipmentType.CABLE -> "Polea"
    EquipmentType.BODYWEIGHT -> "Peso corporal"
    EquipmentType.MULTIPOWER -> "Multipower"
    EquipmentType.KETTLEBELL -> "Kettlebell"
    EquipmentType.ELASTIC_BAND -> "Banda elástica"
    EquipmentType.OTHER -> "Otro"
}
