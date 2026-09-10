package com.gymquest.app.feature.progress

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gymquest.app.app.GymQuestApp
import com.gymquest.app.core.ui.component.CharacterHeader
import com.gymquest.app.core.ui.component.EmptyAdventureState
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.StatBadge
import com.gymquest.app.core.ui.theme.QuestTheme
import com.gymquest.app.domain.model.MasteryProgress
import com.gymquest.app.domain.model.ProgressSummary

@Composable
fun ProgressScreen() {
    val application = LocalContext.current.applicationContext as GymQuestApp
    val viewModel: ProgressViewModel = viewModel(
        factory = viewModelFactory { initializer { ProgressViewModel(application.appContainer.observeProgressSummaryUseCase) } },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProgressContent(state = state, onRetry = viewModel::retry)
}

@Composable
internal fun ProgressContent(state: ProgressUiState, onRetry: () -> Unit) {
    QuestScreen {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (state.isLoading) {
                item {
                    EmptyAdventureState(
                        title = "Cargando progreso",
                        description = "Preparando las estadisticas de tu aventura.",
                    )
                }
                return@LazyColumn
            }
            state.errorMessage?.let { message ->
                item {
                    EmptyAdventureState(
                        title = "No se pudo cargar el progreso",
                        description = message,
                        action = { com.gymquest.app.core.ui.component.QuestButton(text = "Reintentar", onClick = onRetry) },
                    )
                }
                return@LazyColumn
            }
            val summary = requireNotNull(state.summary)
            val stats = summary.characterStats
            val previousThreshold = 100L * (stats.level - 1L) * (stats.level - 1L)
            val nextThreshold = 100L * stats.level * stats.level
            val levelXp = (stats.totalXp - previousThreshold).coerceAtLeast(0)
            val levelTargetXp = (nextThreshold - previousThreshold).coerceAtLeast(1)
            val xpRemaining = (nextThreshold - stats.totalXp).coerceAtLeast(0)
            item {
                CharacterHeader(
                    name = "Aspirante de hierro",
                    level = stats.level,
                    currentXp = levelXp.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                    targetXp = levelTargetXp.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                    subtitle = "${stats.totalXp} XP total · Faltan $xpRemaining XP para el nivel ${stats.level + 1}",
                )
            }
            item {
                QuestSectionHeader(
                    title = "Resumen de progreso",
                    subtitle = "Acumulado de sesiones completadas. Cada indicador incluye su valor y unidad.",
                )
            }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatBadge(label = "volumen", value = "${summary.totalVolume.formatKg()} kg")
                    StatBadge(label = "series", value = summary.totalSets.toString())
                    StatBadge(label = "records", value = summary.personalRecordCount.toString())
                    StatBadge(label = "variantes", value = stats.discoveredVariants.toString())
                }
            }
            if (stats.totalWorkouts == 0 || summary.masteryProgress.isEmpty()) {
                item {
                    EmptyAdventureState(
                        title = "Aun no hay progreso",
                        description = "Completa una sesion con al menos una serie para calcular XP, records y dominio.",
                    )
                }
            } else {
                val records = summary.masteryProgress.filter {
                    it.mastery.personalRecordWeight != null || it.mastery.personalRecordVolume != null
                }
                if (records.isNotEmpty()) {
                    item {
                        QuestSectionHeader(
                            title = "Récords personales",
                            subtitle = "Mejor carga, repeticiones y volumen por variante. Las unidades se muestran de forma explícita.",
                        )
                    }
                    item {
                        QuestPanel {
                            records.take(MAX_RECORD_ITEMS).forEach { record ->
                                PersonalRecordRow(record)
                            }
                            if (records.size > MAX_RECORD_ITEMS) {
                                Text(
                                    text = "Mostrando los $MAX_RECORD_ITEMS primeros récords de ${records.size} variantes con marca personal.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = QuestTheme.tokens.colors.textSecondary,
                                )
                            }
                        }
                    }
                }
                item {
                    QuestSectionHeader(
                        title = "Dominio por ejercicio",
                        subtitle = "Escala: nivel de dominio por variante registrada. El texto indica rango, nivel y series acumuladas.",
                    )
                }
                item {
                    QuestPanel {
                        summary.masteryProgress.take(MAX_MASTERY_ITEMS).forEach { progress ->
                            MasteryRow(progress)
                        }
                        if (summary.masteryProgress.size > MAX_MASTERY_ITEMS) {
                            Text(
                                text = "Mostrando las $MAX_MASTERY_ITEMS variantes con mayor volumen de ${summary.masteryProgress.size} descubiertas.",
                                style = MaterialTheme.typography.bodySmall,
                                color = QuestTheme.tokens.colors.textSecondary,
                            )
                        }
                    }
                }
                item {
                    QuestPanel {
                        QuestSectionHeader(
                            title = "Descubrimientos",
                            subtitle = "${stats.discoveredVariants} variantes descubiertas al completar su primera serie.",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalRecordRow(progress: MasteryProgress) {
    val record = progress.mastery
    val weight = record.personalRecordWeight?.let { "${it.formatKg()} kg" } ?: "sin carga"
    val reps = record.personalRecordReps?.let { "$it repeticiones" } ?: "sin repeticiones"
    val volume = record.personalRecordVolume?.let { "${it.formatKg()} kg de volumen" } ?: "sin volumen"
    Text(progress.variantName, style = MaterialTheme.typography.titleMedium)
    Text(
        text = "Mejor carga: $weight · Mejor serie: $reps · Mejor volumen: $volume",
        style = MaterialTheme.typography.bodySmall,
        color = QuestTheme.tokens.colors.textSecondary,
    )
}

@Composable
private fun MasteryRow(progress: MasteryProgress) {
    val level = progress.mastery.level
    val nextLevelAtSets = level * SETS_PER_MASTERY_LEVEL
    val previousLevelAtSets = (level - 1) * SETS_PER_MASTERY_LEVEL
    val setsWithinLevel = (progress.mastery.totalSets - previousLevelAtSets).coerceAtLeast(0)
    val setsRequired = (nextLevelAtSets - previousLevelAtSets).coerceAtLeast(1)
    val fraction = (setsWithinLevel.toFloat() / setsRequired).coerceIn(0f, 1f)
    val description = "${progress.variantName}: ${progress.mastery.masteryRank.displayName()}, nivel $level, ${progress.mastery.totalSets} series acumuladas. Faltan ${(nextLevelAtSets - progress.mastery.totalSets).coerceAtLeast(0)} series para el siguiente nivel."

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = description
                progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
            },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(progress.variantName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text("Nv. $level", style = MaterialTheme.typography.labelLarge, color = QuestTheme.tokens.colors.xpGold)
        }
        Text(
            text = "${progress.mastery.masteryRank.displayName()} · ${progress.mastery.totalSets} series · ${progress.mastery.accumulatedVolume.formatKg()} kg",
            style = MaterialTheme.typography.bodySmall,
            color = QuestTheme.tokens.colors.textSecondary,
        )
        LinearProgressIndicator(
            progress = { fraction },
            color = QuestTheme.tokens.colors.positive,
            trackColor = QuestTheme.tokens.colors.panelBorder,
            modifier = Modifier.fillMaxWidth().height(8.dp),
        )
        Text(
            text = "$setsWithinLevel de $setsRequired series hacia el nivel ${level + 1}",
            style = MaterialTheme.typography.bodySmall,
            color = QuestTheme.tokens.colors.textSecondary,
        )
    }
}

private fun com.gymquest.app.domain.model.enums.MasteryRank.displayName(): String = when (this) {
    com.gymquest.app.domain.model.enums.MasteryRank.NOVICE -> "Novato"
    com.gymquest.app.domain.model.enums.MasteryRank.APPRENTICE -> "Aprendiz"
    com.gymquest.app.domain.model.enums.MasteryRank.INTERMEDIATE -> "Intermedio"
    com.gymquest.app.domain.model.enums.MasteryRank.ADVANCED -> "Avanzado"
    com.gymquest.app.domain.model.enums.MasteryRank.EXPERT -> "Experto"
    com.gymquest.app.domain.model.enums.MasteryRank.MASTER -> "Maestro"
}

private const val SETS_PER_MASTERY_LEVEL = 10
private const val MAX_MASTERY_ITEMS = 5
private const val MAX_RECORD_ITEMS = 3

private fun Double.formatKg(): String =
    if (this % 1.0 == 0.0) toLong().toString() else "%.1f".format(java.util.Locale.US, this)
