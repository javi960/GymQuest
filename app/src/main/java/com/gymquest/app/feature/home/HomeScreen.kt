package com.gymquest.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymquest.app.app.GymQuestApp
import com.gymquest.app.core.ui.component.CharacterHeader
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestActionButton
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.domain.model.CharacterCategoryRules
import com.gymquest.app.domain.model.TrainingExperience

/** Start page for the two active GymQuest libraries: Dojo and exercise catalogue. */
@Composable
fun HomeScreen(
    onOpenDojo: () -> Unit,
    onOpenCatalog: () -> Unit,
) {
    val app = LocalContext.current.applicationContext as GymQuestApp
    val practices by app.appContainer.observeMartialPracticeCountUseCase()
        .collectAsStateWithLifecycle(initialValue = 0L)
    val trainingExperience by app.appContainer.weeklyTrainingRepository.observeExperienceSummary()
        .collectAsStateWithLifecycle(initialValue = com.gymquest.app.data.local.projection.TrainingExperienceSummary(0, 0))
    val dojoXp = practices * XP_PER_DOJO_PRACTICE
    val gymXp = TrainingExperience.total(trainingExperience)
    val totalXp = dojoXp + gymXp
    val level = levelFor(totalXp)
    val character = CharacterCategoryRules.definitionFor(level.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
    val previousThreshold = XP_PER_LEVEL_UNIT * (level - 1L) * (level - 1L)
    val nextThreshold = XP_PER_LEVEL_UNIT * level * level
    QuestScreen {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                CharacterHeader(
                    name = "GymQuest",
                    level = level.toInt(),
                    currentXp = (totalXp - previousThreshold).toInt(),
                    targetXp = (nextThreshold - previousThreshold).toInt(),
                    subtitle = "${character.name} · ${trainingExperience.completedExercises} ejercicios (+${TrainingExperience.XP_PER_COMPLETED_EXERCISE} XP c/u) · ${trainingExperience.completedSessions} sesiones (+${TrainingExperience.XP_PER_COMPLETED_SESSION} XP c/u)",
                    portraitFileName = character.imageFileName,
                    portraitContentDescription = "${character.name}, nivel $level",
                )
            }
            item {
                QuestPanel {
                    QuestSectionHeader(
                        title = "GymQuest",
                        subtitle = "Tu biblioteca local de ejercicios y artes marciales.",
                    )
                }
            }
            item {
                QuestPanel {
                    Text("Experiencia", style = MaterialTheme.typography.titleMedium)
                    Text("Gimnasio: $gymXp XP · Dojo: $dojoXp XP")
                    Text("El personaje cambia automáticamente al alcanzar una nueva categoría.", style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                HomeDestinationCard(
                    title = "Dojo",
                    description = "Consulta y organiza artes marciales, estilos, katas, técnicas y posiciones.",
                    action = QuestAction.Dojo,
                    label = "Abrir Dojo",
                    onClick = onOpenDojo,
                )
            }
            item {
                HomeDestinationCard(
                    title = "Catálogo de ejercicios",
                    description = "Explora, crea y edita grupos musculares, ejercicios y variantes de equipamiento.",
                    action = QuestAction.Catalog,
                    label = "Abrir catálogo",
                    onClick = onOpenCatalog,
                )
            }
        }
    }
}

private fun levelFor(totalXp: Long): Long {
    var level = 1L
    while (totalXp >= XP_PER_LEVEL_UNIT * level * level) level++
    return level
}

private const val XP_PER_DOJO_PRACTICE = 50L
private const val XP_PER_LEVEL_UNIT = 100L

@Composable
private fun HomeDestinationCard(
    title: String,
    description: String,
    action: QuestAction,
    label: String,
    onClick: () -> Unit,
) {
    QuestPanel {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(description, style = MaterialTheme.typography.bodyMedium)
            QuestActionButton(action = action, label = label, onClick = onClick)
        }
    }
}
