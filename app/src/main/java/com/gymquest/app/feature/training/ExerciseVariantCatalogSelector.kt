package com.gymquest.app.feature.training

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.gymquest.app.core.ui.component.QuestFilterChip
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestSearchField
import com.gymquest.app.domain.model.ExerciseCatalogEntry
import com.gymquest.app.domain.model.MuscleGroup
import com.gymquest.app.domain.model.enums.EquipmentType

/** Selector reutilizable con los mismos filtros por cuerpo, equipo y texto que el catálogo. */
@Composable
fun ExerciseVariantCatalogSelector(
    catalog: List<ExerciseCatalogEntry>,
    muscleGroups: List<MuscleGroup>,
    selectedVariantId: Long?,
    onVariantSelected: (Long) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedGroupId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedEquipment by rememberSaveable { mutableStateOf<EquipmentType?>(null) }
    val visibleEntries = catalog.filter { entry ->
        (selectedGroupId == null || entry.exerciseBase.primaryMuscleGroupId == selectedGroupId) &&
            entry.variants.any { variant ->
                (selectedEquipment == null || variant.equipmentType == selectedEquipment) &&
                    (entry.exerciseBase.name.contains(query, true) || variant.name.contains(query, true))
            }
    }

    QuestPanel {
        QuestSearchField(query, { query = it })
        Text("Filtrar por músculo", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            QuestFilterChip("Todos", selectedGroupId == null, onClick = { selectedGroupId = null })
            muscleGroups.forEach { group ->
                val count = catalog.count { it.exerciseBase.primaryMuscleGroupId == group.id }
                if (count > 0) QuestFilterChip("${group.name} ($count)", selectedGroupId == group.id, onClick = { selectedGroupId = group.id })
            }
        }
        Text("Filtrar por equipamiento", style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            QuestFilterChip("Todos", selectedEquipment == null, onClick = { selectedEquipment = null })
            EquipmentType.entries.forEach { equipment ->
                val count = catalog.count { entry -> entry.variants.any { it.equipmentType == equipment } }
                if (count > 0) QuestFilterChip("${equipment.label()} ($count)", selectedEquipment == equipment, onClick = { selectedEquipment = equipment })
            }
        }
        Text("Selecciona el ejercicio", style = MaterialTheme.typography.titleSmall)
        visibleEntries.forEach { entry ->
            val variants = entry.variants.filter { variant ->
                (selectedEquipment == null || variant.equipmentType == selectedEquipment) &&
                    (entry.exerciseBase.name.contains(query, true) || variant.name.contains(query, true))
            }
            if (variants.isNotEmpty()) {
                Text(entry.exerciseBase.name, style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    variants.forEach { variant ->
                        QuestFilterChip(variant.name, selectedVariantId == variant.id, onClick = { onVariantSelected(variant.id) })
                    }
                }
            }
        }
        if (visibleEntries.isEmpty()) Text("No hay ejercicios con esos filtros.", style = MaterialTheme.typography.bodySmall)
    }
}

private fun EquipmentType.label(): String = when (this) {
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
