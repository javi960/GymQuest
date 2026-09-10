package com.gymquest.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gymquest.app.app.GymQuestApp
import com.gymquest.app.core.ui.component.CharacterHeader
import com.gymquest.app.core.ui.component.EmptyAdventureState
import com.gymquest.app.core.ui.component.MissionCard
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestActionButton
import com.gymquest.app.core.ui.component.QuestButton
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.StatBadge
import com.gymquest.app.core.ui.component.StatBadgeRow
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.domain.model.CharacterCategoryRules
import com.gymquest.app.domain.model.MasteryProgress
import com.gymquest.app.domain.model.ProgressSummary
import com.gymquest.app.core.ui.theme.QuestTheme

@Composable
fun HomeScreen(
    onOpenSession: () -> Unit,
    onOpenCatalog: () -> Unit,
) {
    val application = LocalContext.current.applicationContext as GymQuestApp
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    observeProgressSummary = application.appContainer.observeProgressSummaryUseCase,
                    observeActiveSession = application.appContainer.observeActiveSessionUseCase,
                )
            }
        },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(
        state = state,
        onAction = { action -> if (action is HomeAction.OpenSession) onOpenSession() },
        onOpenCatalog = onOpenCatalog,
        onRetry = viewModel::retry,
    )
}

@Composable
internal fun HomeContent(
    state: HomeUiState,
    onAction: (HomeAction) -> Unit,
    onOpenCatalog: () -> Unit,
    onRetry: () -> Unit,
) {
    val stats = state.summary?.characterStats
    val level = stats?.level ?: 1
    val category = stats?.categoryDefinition ?: CharacterCategoryRules.definitionFor(level)
    val totalXp = stats?.totalXp ?: 0L
    val previousThreshold = 100L * (level - 1L) * (level - 1L)
    val nextThreshold = 100L * level * level
    val isFirstAdventure = !state.hasActiveSession &&
        (stats?.totalWorkouts ?: 0) == 0 &&
        (state.summary?.totalSets ?: 0) == 0
    QuestScreen {
        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                CharacterHeader(
                    name = "GymQuest",
                    level = level,
                    currentXp = (totalXp - previousThreshold).coerceAtLeast(0).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                    targetXp = (nextThreshold - previousThreshold).coerceAtLeast(1).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
                    subtitle = if (state.hasActiveSession) "Sesion activa lista para continuar" else "Aventura local de fuerza, tecnica y constancia",
                    portraitFileName = category.imageFileName,
                    portraitContentDescription = "Personaje: ${category.name}",
                )
            }
            item {
                MissionCard(
                    title = when {
                        state.hasActiveSession -> "Sesión en curso"
                        isFirstAdventure -> "Primer paso"
                        else -> "Misión principal"
                    },
                    description = when {
                        state.hasActiveSession -> "Continúa tu sesión activa: ${state.activeSession?.exercises?.size ?: 0} ejercicios preparados."
                        isFirstAdventure -> "Crea un ejercicio y su variante para poder registrar tu primera serie."
                        else -> "Inicia una sesión y registra peso, reps y descanso sin distraerte."
                    },
                    reward = if (isFirstAdventure) "Preparar tu primer entrenamiento" else "+ XP al completar series reales",
                    action = {
                        QuestActionButton(
                            action = when {
                                state.hasActiveSession -> QuestAction.Resume
                                isFirstAdventure -> QuestAction.Catalog
                                else -> QuestAction.Start
                            },
                            label = when {
                                state.hasActiveSession -> "Continuar sesión"
                                isFirstAdventure -> "Crear primer ejercicio"
                                else -> "Iniciar sesión"
                            },
                            onClick = {
                                if (isFirstAdventure) onOpenCatalog else onAction(HomeAction.OpenSession)
                            },
                        )
                    },
                )
            }
            state.errorMessage?.let { message ->
                item {
                    EmptyAdventureState(
                        title = "No se pudo actualizar el inicio",
                        description = message,
                        action = { QuestButton(text = "Reintentar", onClick = onRetry) },
                    )
                }
            }
            if (!state.isLoading && state.summary != null) {
                item {
                    StatBadgeRow {
                        StatBadge(label = "sesiones", value = stats?.totalWorkouts?.toString() ?: "0")
                        StatBadge(label = "series", value = state.summary.totalSets.toString())
                        StatBadge(label = "records", value = state.summary.personalRecordCount.toString())
                    }
                }
            }
            state.summary?.let { summary ->
                progressSummaryContent(summary)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.progressSummaryContent(summary: ProgressSummary) {
    val stats = summary.characterStats
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
        return
    }
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
                records.take(MAX_RECORD_ITEMS).forEach { record -> PersonalRecordRow(record) }
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
            summary.masteryProgress.take(MAX_MASTERY_ITEMS).forEach { progress -> MasteryRow(progress) }
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
