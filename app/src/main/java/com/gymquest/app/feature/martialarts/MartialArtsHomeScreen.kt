package com.gymquest.app.feature.martialarts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.gymquest.app.app.GymQuestApp
import com.gymquest.app.core.time.SystemClockProvider
import com.gymquest.app.core.ui.component.EmptyAdventureState
import com.gymquest.app.core.ui.component.CharacterHeader
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.icon
import com.gymquest.app.core.ui.component.QuestActionButton
import com.gymquest.app.core.ui.component.QuestButton
import com.gymquest.app.core.ui.component.QuestIconButton
import com.gymquest.app.core.ui.component.QuestConfirmationDialog
import com.gymquest.app.core.ui.component.QuestNumericField
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.QuestSingleChoiceMenu
import com.gymquest.app.core.ui.component.QuestTextField
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.delay
import com.gymquest.app.domain.model.MartialBelt
import com.gymquest.app.domain.model.enums.MartialDirection
import com.gymquest.app.domain.model.enums.MartialSide
import com.gymquest.app.domain.model.enums.MartialTechniqueFamily
import com.gymquest.app.domain.model.MartialContentStep
import com.gymquest.app.domain.model.MartialTechnique
import com.gymquest.app.domain.model.MartialStance
import com.gymquest.app.domain.model.MediaFile
import com.gymquest.app.domain.model.enums.MediaOwnerType
import com.gymquest.app.domain.model.enums.MediaType
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView

@Composable
fun MartialArtsHomeScreen(onOpenArt: (Long) -> Unit) {
    val model = martialViewModel()
    val state by model.uiState.collectAsStateWithLifecycle()
    val beltModel = martialBeltViewModel()
    val beltState by beltModel.uiState.collectAsStateWithLifecycle()
    var showForm by remember { mutableStateOf(false) }
    var artToDelete by remember { mutableStateOf<com.gymquest.app.domain.model.MartialArt?>(null) }
    var artToEdit by remember { mutableStateOf<com.gymquest.app.domain.model.MartialArt?>(null) }
    var artToArchive by remember { mutableStateOf<com.gymquest.app.domain.model.MartialArt?>(null) }
    androidx.compose.runtime.LaunchedEffect(Unit) { model.loadArts() }
    QuestScreen { Box(Modifier.fillMaxSize()) { LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { DojoHeader() }
        if (state.isLoading) item { Text("Cargando dojo…") }
        state.arts.forEach { art -> item { QuestPanel { Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) { BeltAvatar(beltState.belt); Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(art.name, style = androidx.compose.material3.MaterialTheme.typography.titleLarge); art.description?.let { Text(it) }; Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { QuestIconButton(QuestAction.OpenDetail, { onOpenArt(art.id) }); QuestIconButton(QuestAction.Edit, { artToEdit = art }); QuestIconButton(QuestAction.Pause, { artToArchive = art }); QuestIconButton(QuestAction.Delete, { artToDelete = art }) } } } } } }
        if (!state.isLoading && state.arts.isEmpty()) item { EmptyAdventureState("Tu dojo está vacío", "Crea una disciplina para guardar estilos, técnicas y prácticas sin conexión.") }
        state.message?.let { item { Text(it) } }
    }; FloatingActionButton(onClick = { showForm = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) { Icon(QuestAction.Add.icon(), contentDescription = "Añadir disciplina") } } }
    if (showForm) MartialFormDialog("Nueva disciplina", "Nombre", "Descripción opcional", { showForm = false }) { name, description -> model.addArt(name, description); showForm = false }
    artToDelete?.let { art -> QuestConfirmationDialog("Borrar disciplina", "Se eliminarán ${art.name}, todos sus estilos, contenidos, técnicas y prácticas asociadas. Esta acción no se puede deshacer.", "Borrar", { model.deleteArt(art.id); artToDelete = null }, { artToDelete = null }, QuestAction.Delete) }
    artToEdit?.let { art -> MartialFormDialog("Editar disciplina", "Nombre", "Descripción opcional", { artToEdit = null }, art.name, art.description.orEmpty()) { name, description -> model.updateArt(art.copy(name = name, description = description?.ifBlank { null })); artToEdit = null } }
    artToArchive?.let { art -> QuestConfirmationDialog("Archivar disciplina", "${art.name} dejará de aparecer en el Dojo, pero conservará sus datos.", "Archivar", { model.archiveArt(art.id); artToArchive = null }, { artToArchive = null }) }
}

@Composable private fun DojoHeader() { val context = LocalContext.current; val id = remember { context.resources.getIdentifier("dojo", "drawable", context.packageName) }; QuestPanel { if (id != 0) Image(painterResource(id), "Ilustración del dojo", Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Crop); Text("Dojo", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall); Text("Biblioteca y práctica marcial offline.") } }
@Composable private fun BeltAvatar(belt: MartialBelt, modifier: Modifier = Modifier.size(88.dp)) { val context = LocalContext.current; val id = remember(belt) { context.resources.getIdentifier(belt.avatarFileName.removeSuffix(".png"), "drawable", context.packageName) }; if (id != 0) Image(painterResource(id), "Avatar cinturón ${belt.label}", modifier) else Icon(QuestAction.MartialArts.icon(), "Avatar marcial", modifier) }

@Composable
fun MartialArtScreen(artId: Long, onOpenStyle: (Long) -> Unit) {
    val model = martialViewModel()
    val state by model.uiState.collectAsStateWithLifecycle()
    val beltModel = martialBeltViewModel()
    val beltState by beltModel.uiState.collectAsStateWithLifecycle()
    var showForm by remember { mutableStateOf(false) }
    var selectedBelt by remember(beltState.belt) { mutableStateOf(beltState.belt) }
    var beltToConfirm by remember { mutableStateOf<MartialBelt?>(null) }
    var styleToDelete by remember { mutableStateOf<com.gymquest.app.domain.model.MartialStyle?>(null) }
    var styleToEdit by remember { mutableStateOf<com.gymquest.app.domain.model.MartialStyle?>(null) }
    var styleToArchive by remember { mutableStateOf<com.gymquest.app.domain.model.MartialStyle?>(null) }
    androidx.compose.runtime.LaunchedEffect(artId) { model.loadStyles(artId) }
    QuestScreen { Box(Modifier.fillMaxSize()) { LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { QuestSectionHeader("Estilos", "Elige el estilo que vas a entrenar.") }
        item { QuestPanel { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) { BeltAvatar(beltState.belt, Modifier.size(140.dp)); Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { QuestSectionHeader("Grado de la disciplina", "El personaje cambia al confirmar el cinturón alcanzado."); QuestSingleChoiceMenu(selectedBelt, MartialBelt.entries, "Cinturón actual", MartialBelt::label, { selectedBelt = it }, enabled = !beltState.isSaving); QuestActionButton(QuestAction.Save, { beltToConfirm = selectedBelt }, label = "Cambiar personaje", enabled = !beltState.isSaving && selectedBelt != beltState.belt) } } } }
        if (state.isLoading) item { Text("Cargando estilos…") }
        state.styles.forEach { style -> item { QuestPanel { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Text(style.name, style = androidx.compose.material3.MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)); QuestIconButton(QuestAction.OpenDetail, { onOpenStyle(style.id) }); QuestIconButton(QuestAction.Edit, { styleToEdit = style }); QuestIconButton(QuestAction.Pause, { styleToArchive = style }); QuestIconButton(QuestAction.Delete, { styleToDelete = style }) } } } }
        if (!state.isLoading && state.styles.isEmpty()) item { EmptyAdventureState("Aún no hay estilos", "Añade un estilo para registrar su biblioteca y sus prácticas.") }
        state.message?.let { item { Text(it) } }
    }; FloatingActionButton(onClick = { showForm = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) { Icon(QuestAction.Add.icon(), contentDescription = "Añadir estilo") } } }
    if (showForm) MartialFormDialog("Nuevo estilo", "Nombre", "Descripción opcional", { showForm = false }) { name, description -> model.addStyle(artId, name, description); showForm = false }
    styleToDelete?.let { style -> QuestConfirmationDialog("Borrar estilo", "Se eliminarán ${style.name}, sus contenidos, técnicas y prácticas asociadas. Esta acción no se puede deshacer.", "Borrar", { model.deleteStyle(artId, style.id); styleToDelete = null }, { styleToDelete = null }, QuestAction.Delete) }
    styleToEdit?.let { style -> MartialFormDialog("Editar estilo", "Nombre", "Descripción opcional", { styleToEdit = null }, style.name, style.description.orEmpty()) { name, description -> model.updateStyle(style.copy(name = name, description = description?.ifBlank { null })); styleToEdit = null } }
    styleToArchive?.let { style -> QuestConfirmationDialog("Archivar estilo", "${style.name} dejará de aparecer en el listado, pero conservará sus datos.", "Archivar", { model.archiveStyle(style.id, artId); styleToArchive = null }, { styleToArchive = null }) }
    beltToConfirm?.let { belt -> QuestConfirmationDialog("Cambiar personaje", "Se mostrará el personaje del cinturón ${belt.label.lowercase()} en el Dojo.", "Confirmar", { beltModel.confirmBelt(belt); beltToConfirm = null }, { beltToConfirm = null }) }
}

@Composable
fun MartialStyleScreen(
    styleId: Long,
    onOpenContent: (Long) -> Unit,
    onOpenTechnique: (Long) -> Unit,
    onOpenStance: (Long) -> Unit,
    onCreateContent: () -> Unit,
    onCreateTechnique: () -> Unit,
    onCreateStance: () -> Unit,
) {
    val model = martialViewModel()
    val state by model.uiState.collectAsStateWithLifecycle()
    var selectedLibraryFilter by remember { mutableStateOf(MartialLibraryFilter.All) }
    androidx.compose.runtime.LaunchedEffect(styleId) { model.ensureShitoRyuCatalog(styleId); model.loadProgress(styleId); model.loadTechniques(styleId); model.loadStances(styleId) }
    val progress = state.progress
    QuestScreen {
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 104.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { QuestSectionHeader(progress?.style?.name ?: "Biblioteca técnica", "El dominio técnico se mantiene separado de la XP de fuerza.") }
                item { QuestPanel { Text("Última práctica: ${progress?.lastPracticeAt?.let(::relativePractice) ?: "nunca"}"); Text("Contenido descubierto: ${progress?.discoveredCount ?: 0} / ${progress?.content?.size ?: 0}") } }
                item {
                    MartialLibraryFilterSelector(
                        selectedFilter = selectedLibraryFilter,
                        onFilterSelected = { selectedLibraryFilter = it },
                    )
                }
                if (selectedLibraryFilter == MartialLibraryFilter.All || selectedLibraryFilter == MartialLibraryFilter.Kata) {
                    item { QuestSectionHeader("Katas y formas", "Secuencias ordenadas para practicar.") }
                    if (progress?.content.isNullOrEmpty()) item { EmptyAdventureState("Sin katas ni formas", "Crea un kata, forma, drill o cualquier unidad de práctica.") }
                    progress?.content.orEmpty().forEach { content ->
                        item { MartialLibraryNameCard(content.name) { onOpenContent(content.id) } }
                    }
                }
                if (selectedLibraryFilter == MartialLibraryFilter.All || selectedLibraryFilter == MartialLibraryFilter.Techniques) {
                    item { QuestSectionHeader("Técnicas", "Recursos reutilizables de este estilo.") }
                    if (state.techniques.isEmpty()) item { EmptyAdventureState("Sin técnicas", "Añade técnicas a la biblioteca para reutilizarlas en tus secuencias.") }
                    state.techniques.forEach { technique ->
                        item { MartialLibraryNameCard(technique.name) { onOpenTechnique(technique.id) } }
                    }
                }
                if (selectedLibraryFilter == MartialLibraryFilter.All || selectedLibraryFilter == MartialLibraryFilter.Stances) {
                    item { QuestSectionHeader("Posiciones", "Biblioteca reutilizable de piernas.") }
                    if (state.stances.isEmpty()) item { EmptyAdventureState("Sin posiciones", "Añade posiciones para reutilizarlas en tus secuencias.") }
                    state.stances.forEach { stance ->
                        item { MartialLibraryNameCard(stance.name) { onOpenStance(stance.id) } }
                    }
                }
                state.message?.let { item { Text(it) } }
            }
            MartialStyleCreateFab(
                onCreateContent = onCreateContent,
                onCreateTechnique = onCreateTechnique,
                onCreateStance = onCreateStance,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }
}

private enum class MartialLibraryFilter(val label: String) {
    All("Todo"),
    Kata("Kata"),
    Techniques("Técnicas"),
    Stances("Posiciones"),
}

@Composable
private fun MartialLibraryFilterSelector(
    selectedFilter: MartialLibraryFilter,
    onFilterSelected: (MartialLibraryFilter) -> Unit,
) {
    QuestPanel {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(MartialLibraryFilter.All, MartialLibraryFilter.Kata).forEach { filter ->
                    QuestButton(
                        text = if (filter == selectedFilter) "✓ ${filter.label}" else filter.label,
                        onClick = { onFilterSelected(filter) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(MartialLibraryFilter.Techniques, MartialLibraryFilter.Stances).forEach { filter ->
                    QuestButton(
                        text = if (filter == selectedFilter) "✓ ${filter.label}" else filter.label,
                        onClick = { onFilterSelected(filter) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun MartialStyleCreateFab(
    onCreateContent: () -> Unit,
    onCreateTechnique: () -> Unit,
    onCreateStance: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (expanded) {
            ExtendedFloatingActionButton(
                text = { Text("Nuevo kata / forma") },
                icon = { Icon(QuestAction.Add.icon(), contentDescription = null) },
                onClick = { expanded = false; onCreateContent() },
            )
            ExtendedFloatingActionButton(
                text = { Text("Nueva técnica") },
                icon = { Icon(QuestAction.Add.icon(), contentDescription = null) },
                onClick = { expanded = false; onCreateTechnique() },
            )
            ExtendedFloatingActionButton(
                text = { Text("Nueva posición") },
                icon = { Icon(QuestAction.Add.icon(), contentDescription = null) },
                onClick = { expanded = false; onCreateStance() },
            )
        }
        FloatingActionButton(onClick = { expanded = !expanded }) {
            Icon(
                QuestAction.Add.icon(),
                contentDescription = if (expanded) "Cerrar opciones de creación" else "Crear elemento",
            )
        }
    }
}

@Composable
fun MartialContentDetailScreen(contentId: Long, styleId: Long) {
    val model = martialViewModel()
    val state by model.uiState.collectAsStateWithLifecycle()
    var form by remember { mutableStateOf<MartialForm?>(null) }
    var mediaTarget by remember { mutableStateOf<MartialContentStep?>(null) }
    var entityMediaError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val mediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val target = mediaTarget ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            val type = when (context.contentResolver.getType(uri)) { "image/gif" -> MediaType.GIF; else -> if ((context.contentResolver.getType(uri) ?: "").startsWith("video/")) MediaType.VIDEO else MediaType.IMAGE }
            model.replaceStepMedia(target.id, MediaFile(ownerType = MediaOwnerType.MARTIAL_CONTENT_STEP, ownerId = contentId, mediaType = type, localUri = uri.toString(), title = "Movimiento ${target.orderIndex + 1}", createdAt = Instant.now()))
        }
        mediaTarget = null
    }
    val entityMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val type = localGuideMediaType(context, uri)
        if (type == null) {
            entityMediaError = "Elige solo un GIF o un vídeo local."
            return@rememberLauncherForActivityResult
        }
        try {
            context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            model.replaceContentMedia(contentId, MediaFile(ownerType = MediaOwnerType.MARTIAL_TECHNICAL_CONTENT, ownerId = contentId, mediaType = type, localUri = uri.toString(), title = "Guía de contenido", createdAt = Instant.now()))
            entityMediaError = null
        } catch (_: SecurityException) {
            entityMediaError = "No se pudo conservar el acceso al archivo seleccionado."
        }
    }
    androidx.compose.runtime.LaunchedEffect(contentId, styleId) { model.loadContentDetail(contentId); model.loadTechniques(styleId); model.loadStances(styleId) }
    val detail = state.contentDetail
    QuestScreen { LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (detail == null) item { QuestSectionHeader("Contenido técnico", "Cargando detalle…") }
        detail?.let { value ->
            item { QuestPanel { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(value.content.name, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                        Text(value.content.contentType)
                    }
                    QuestIconButton(QuestAction.Edit, { form = MartialForm.EditContent(value.content) })
                    QuestIconButton(QuestAction.Delete, { form = MartialForm.ArchiveContent(value.content) })
                }
            } } }
            item {
                QuestPanel {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuestSectionHeader("Teoría / descripción", "Apuntes y teoría sobre este kata o forma.")
                        Text(value.content.description ?: "Aún no hay teoría o descripción. Usa el icono de editar para añadirla.")
                    }
                }
            }
            item { Row(verticalAlignment = Alignment.CenterVertically) { QuestSectionHeader("Secuencia", "${value.steps.size} paso(s) ordenados.", Modifier.weight(1f)); QuestIconButton(QuestAction.Add, { form = MartialForm.Step() }) } }
            if (value.steps.isNotEmpty()) item { QuestButton("Practicar paso a paso", { form = MartialForm.PracticeSequence(value.steps) }, action = QuestAction.Start) }
            if (value.steps.isEmpty()) item { EmptyAdventureState("Secuencia sin pasos", "Aún no se han definido movimientos. El contenido sigue disponible para práctica y notas.") }
            value.steps.forEach { step ->
                item {
                    QuestPanel {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                QuestSectionHeader(
                                    "Paso ${step.orderIndex + 1}",
                                    stepSummary(step, state.stances, state.techniques),
                                    Modifier.weight(1f),
                                )
                                QuestIconButton(QuestAction.Edit, { form = MartialForm.EditStep(step) })
                                QuestIconButton(QuestAction.Delete, { form = MartialForm.DeleteStep(step) })
                            }
                            step.description?.let { Text(it) }
                            Text(
                                listOfNotNull(
                                    directionSpanish(step.direction),
                                    step.side?.spanishLabel(),
                                    step.displacement,
                                    step.turnDegrees?.let { "giro $it°" },
                                    step.angleDegrees?.let { "ángulo $it°" },
                                    if (step.hasKiai) "kiai" else null,
                                    if (step.hasPause) "pausa" else null,
                                ).joinToString(" · "),
                            )
                            QuestButton(
                                text = if (step.mediaFileId == null) "Adjuntar imagen, GIF o vídeo" else "Cambiar multimedia",
                                onClick = {
                                    mediaTarget = step
                                    mediaLauncher.launch(arrayOf("image/*", "video/*"))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                action = QuestAction.Add,
                            )
                            if (step.mediaFileId != null) {
                                QuestButton(
                                    "Quitar multimedia",
                                    { model.removeStepMedia(step.id, contentId) },
                                    action = QuestAction.Delete,
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                QuestButton("Duplicar", { model.duplicateStep(contentId, step) })
                                QuestButton(
                                    "↑",
                                    { model.reorderSteps(contentId, moveStep(value.steps, step.orderIndex, -1)) },
                                    enabled = step.orderIndex > 0,
                                )
                                QuestButton(
                                    "↓",
                                    { model.reorderSteps(contentId, moveStep(value.steps, step.orderIndex, 1)) },
                                    enabled = step.orderIndex < value.steps.lastIndex,
                                )
                            }
                        }
                    }
                }
            }
            item { QuestPanel { MartialLocalGuideSection(media = state.mediaByContentId[contentId], error = entityMediaError, onChoose = { entityMediaLauncher.launch(arrayOf("image/gif", "video/*")) }, onRemove = { model.removeContentMedia(contentId) }) } }
        }
        state.message?.let { item { Text(it) } }
    } }
    when (val current = form) {
        is MartialForm.EditContent -> MartialContentDialog({ form = null }, current.content.name, current.content.contentType, current.content.description.orEmpty()) { name, type, description -> model.updateContent(current.content.copy(name = name, contentType = type, description = description?.ifBlank { null })); form = null }
        is MartialForm.ArchiveContent -> QuestConfirmationDialog("Borrar contenido", "Se eliminará ${current.content.name}, sus pasos y prácticas asociadas. Esta acción no se puede deshacer.", "Borrar", { model.deleteContent(current.content.id, styleId); form = null }, { form = null }, QuestAction.Delete)
        is MartialForm.Step -> MartialStepDialog(onDismiss = { form = null }, techniques = state.techniques, stances = state.stances) { step -> model.addStep(contentId, step, current.insertAt); form = null }
        is MartialForm.EditStep -> MartialStepDialog(step = current.step, onDismiss = { form = null }, techniques = state.techniques, stances = state.stances) { step -> model.updateStep(step.copy(id = current.step.id, technicalContentId = contentId, orderIndex = current.step.orderIndex)); form = null }
        is MartialForm.DeleteStep -> QuestConfirmationDialog("Borrar paso", "Se eliminará este paso de la secuencia. Puedes volver a crearlo después.", "Borrar", { model.deleteStep(current.step.id, contentId); form = null }, { form = null }, QuestAction.Delete)
        is MartialForm.PracticeSequence -> MartialSequencePracticeDialog(current.steps, state.stances, state.techniques) { form = null }
        else -> Unit
    }
}

private sealed interface MartialForm {
    data object Content : MartialForm
    data object Technique : MartialForm
    data object Stance : MartialForm
    data class Step(val insertAt: Int? = null) : MartialForm
    data class Practice(val contentId: Long, val contentName: String) : MartialForm
    data class PracticeSequence(val steps: List<MartialContentStep>) : MartialForm
    data class ContentDetail(val content: com.gymquest.app.domain.model.MartialContent) : MartialForm
    data class TechniqueDetail(val technique: MartialTechnique) : MartialForm
    data class StanceDetail(val stance: MartialStance) : MartialForm
    data class EditTechnique(val technique: MartialTechnique) : MartialForm
    data class ArchiveTechnique(val technique: MartialTechnique) : MartialForm
    data class EditStance(val stance: MartialStance) : MartialForm
    data class DeleteStance(val stance: MartialStance) : MartialForm
    data class EditContent(val content: com.gymquest.app.domain.model.MartialContent) : MartialForm
    data class ArchiveContent(val content: com.gymquest.app.domain.model.MartialContent) : MartialForm
    data class EditStep(val step: MartialContentStep) : MartialForm
    data class DeleteStep(val step: MartialContentStep) : MartialForm
}

@Composable
private fun MartialLibraryNameCard(name: String, onOpen: () -> Unit) {
    QuestPanel(
        modifier = Modifier.clickable(onClick = onOpen),
    ) {
        Text(
            text = name,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Dedicated screen used by the floating action button; creation never happens in a dialog. */
@Composable
fun MartialContentCreateScreen(styleId: Long, onFinished: () -> Unit) {
    val model = martialViewModel()
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Kata") }
    var description by remember { mutableStateOf("") }
    QuestScreen {
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { QuestSectionHeader("Nuevo kata / forma", "Crea una secuencia para este estilo.") }
            item { QuestPanel {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuestTextField(name, { name = it }, "Nombre", singleLine = true)
                    QuestTextField(type, { type = it }, "Tipo (kata, forma, drill)", singleLine = true)
                    QuestTextField(description, { description = it }, "Teoría / descripción (opcional)")
                    QuestActionButton(QuestAction.Save, {
                        model.addContent(styleId, name, type, description.ifBlank { null })
                        onFinished()
                    }, enabled = name.isNotBlank() && type.isNotBlank())
                }
            } }
        }
    }
}

@Composable
fun MartialTechniqueCreateScreen(styleId: Long, onFinished: () -> Unit) {
    val model = martialViewModel()
    MartialTechniqueEditor(title = "Nueva técnica", initial = null, onSave = { name, translation, family, description, notes ->
        model.addTechnique(styleId, name, translation, family, description, notes)
        onFinished()
    })
}

@Composable
fun MartialStanceCreateScreen(styleId: Long, onFinished: () -> Unit) {
    val model = martialViewModel()
    MartialStanceEditor(title = "Nueva posición", initial = null, onSave = { name, translation, description, notes ->
        model.addStance(styleId, name, translation, description, notes)
        onFinished()
    })
}

@Composable
fun MartialTechniqueDetailScreen(techniqueId: Long, styleId: Long, onFinished: () -> Unit) {
    val model = martialViewModel()
    val state by model.uiState.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var mediaPickerError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val mediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val type = localGuideMediaType(context, uri)
        if (type == null) {
            mediaPickerError = "Elige solo un GIF o un vídeo local."
            return@rememberLauncherForActivityResult
        }
        try {
            context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            model.replaceTechniqueMedia(techniqueId, MediaFile(ownerType = MediaOwnerType.MARTIAL_TECHNIQUE, ownerId = techniqueId, mediaType = type, localUri = uri.toString(), title = "Guía de técnica", createdAt = Instant.now()))
            mediaPickerError = null
        } catch (_: SecurityException) {
            mediaPickerError = "No se pudo conservar el acceso al archivo seleccionado."
        }
    }
    androidx.compose.runtime.LaunchedEffect(styleId) { model.loadTechniques(styleId) }
    val technique = state.techniques.firstOrNull { it.id == techniqueId }
    if (technique == null) {
        QuestScreen { Text("Cargando técnica…", Modifier.padding(20.dp)) }
        return
    }
    if (editing) {
        MartialTechniqueEditor("Editar técnica", technique) { name, translation, family, description, notes ->
            model.updateTechnique(technique.copy(name = name, translation = translation, family = family, description = description, notes = notes))
            editing = false
        }
    } else {
        QuestScreen { LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { QuestPanel { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(technique.name, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                        Text("Técnica reutilizable")
                    }
                    QuestIconButton(QuestAction.Edit, { editing = true })
                    QuestIconButton(QuestAction.Delete, { confirmDelete = true })
                }
                technique.translation?.let { Text(it) }
                technique.family?.let { Text("Familia: ${it.spanishLabel()}") }
                technique.description?.let { Text(it) }
                technique.notes?.let { Text("Notas: $it") }
            } } }
            item { QuestPanel { MartialLocalGuideSection(media = state.mediaByTechniqueId[techniqueId], error = mediaPickerError, onChoose = { mediaLauncher.launch(arrayOf("image/gif", "video/*")) }, onRemove = { model.removeTechniqueMedia(techniqueId) }) } }
        } }
    }
    if (confirmDelete) QuestConfirmationDialog("Borrar técnica", "Se eliminará ${technique.name} y sus enlaces técnicos y prácticas asociadas. Esta acción no se puede deshacer.", "Borrar", {
        model.deleteTechnique(technique.id, styleId); onFinished()
    }, { confirmDelete = false }, QuestAction.Delete)
}

@Composable
fun MartialStanceDetailScreen(stanceId: Long, styleId: Long, onFinished: () -> Unit) {
    val model = martialViewModel()
    val state by model.uiState.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var mediaPickerError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val mediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val type = localGuideMediaType(context, uri)
        if (type == null) {
            mediaPickerError = "Elige solo un GIF o un vídeo local."
            return@rememberLauncherForActivityResult
        }
        try {
            context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            model.replaceStanceMedia(stanceId, MediaFile(ownerType = MediaOwnerType.MARTIAL_STANCE, ownerId = stanceId, mediaType = type, localUri = uri.toString(), title = "Guía de posición", createdAt = Instant.now()))
            mediaPickerError = null
        } catch (_: SecurityException) {
            mediaPickerError = "No se pudo conservar el acceso al archivo seleccionado."
        }
    }
    androidx.compose.runtime.LaunchedEffect(styleId) { model.loadStances(styleId) }
    val stance = state.stances.firstOrNull { it.id == stanceId }
    if (stance == null) {
        QuestScreen { Text("Cargando posición…", Modifier.padding(20.dp)) }
        return
    }
    if (editing) {
        MartialStanceEditor("Editar posición", stance) { name, translation, description, notes ->
            model.updateStance(stance.copy(name = name, translation = translation, description = description, notes = notes))
            editing = false
        }
    } else {
        QuestScreen { LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { QuestPanel { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stance.name, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                        Text("Posición reutilizable")
                    }
                    QuestIconButton(QuestAction.Edit, { editing = true })
                    QuestIconButton(QuestAction.Delete, { confirmDelete = true })
                }
                stance.translation?.let { Text(it) }
                stance.description?.let { Text(it) }
                stance.notes?.let { Text("Notas: $it") }
            } } }
            item { QuestPanel { MartialLocalGuideSection(media = state.mediaByStanceId[stanceId], error = mediaPickerError, onChoose = { mediaLauncher.launch(arrayOf("image/gif", "video/*")) }, onRemove = { model.removeStanceMedia(stanceId) }) } }
        } }
    }
    if (confirmDelete) QuestConfirmationDialog("Borrar posición", "Se eliminará ${stance.name}. Los movimientos conservarán sus demás datos.", "Borrar", {
        model.deleteStance(stance.id, styleId); onFinished()
    }, { confirmDelete = false }, QuestAction.Delete)
}

private fun localGuideMediaType(context: android.content.Context, uri: android.net.Uri): MediaType? = when {
    context.contentResolver.getType(uri)?.equals("image/gif", ignoreCase = true) == true -> MediaType.GIF
    context.contentResolver.getType(uri)?.startsWith("video/", ignoreCase = true) == true -> MediaType.VIDEO
    else -> null
}

@Composable
private fun MartialLocalGuideSection(
    media: MediaFile?,
    error: String?,
    onChoose: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Guía visual", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        if (media == null) {
            Text("Añade un GIF o vídeo local para consultar cómo se realiza.")
            QuestButton("Añadir GIF o vídeo", onChoose, action = QuestAction.Add)
        } else {
            MartialLocalGuidePreview(media)
            Text(media.title ?: "Archivo local")
            Text(if (media.mediaType == MediaType.VIDEO) "Vídeo local seleccionado." else "GIF local seleccionado.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuestButton("Sustituir archivo", onChoose, action = QuestAction.Edit)
                QuestButton("Eliminar guía", onRemove, action = QuestAction.Delete)
            }
        }
        error?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
    }
}

/**
 * Renders locally persisted guide media in place. VideoView owns its player and is released
 * when the card leaves composition; GIFs use the platform animated drawable on Android 9+.
 */
@Composable
private fun MartialLocalGuidePreview(media: MediaFile) {
    val context = LocalContext.current
    val uri = remember(media.localUri) { android.net.Uri.parse(media.localUri) }
    var previewError by remember(media.localUri) { mutableStateOf<String?>(null) }

    when (media.mediaType) {
        MediaType.GIF -> {
            var imageView by remember(media.localUri) { mutableStateOf<ImageView?>(null) }
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                factory = { viewContext ->
                    ImageView(viewContext).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        contentDescription = "Vista previa del GIF de la guía"
                        imageView = this
                        runCatching {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                ImageDecoder.decodeDrawable(
                                    ImageDecoder.createSource(viewContext.contentResolver, uri),
                                )
                            } else {
                                null
                            }
                        }.onSuccess { drawable ->
                            if (drawable != null) {
                                setImageDrawable(drawable)
                                (drawable as? AnimatedImageDrawable)?.start()
                            } else {
                                // Android 8 and older show a still preview when GIF animation is unavailable.
                                setImageURI(uri)
                            }
                        }.onFailure {
                            previewError = "No se pudo abrir este GIF local. Puedes sustituirlo por otro archivo."
                        }
                    }
                },
            )
            DisposableEffect(media.localUri) {
                onDispose {
                    (imageView?.drawable as? AnimatedImageDrawable)?.stop()
                    imageView?.setImageDrawable(null)
                    imageView = null
                }
            }
        }

        MediaType.VIDEO -> {
            var videoView by remember(media.localUri) { mutableStateOf<VideoView?>(null) }
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                factory = { viewContext ->
                    VideoView(viewContext).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                        contentDescription = "Reproductor de vídeo de la guía"
                        val controls = MediaController(viewContext).also { it.setAnchorView(this) }
                        setMediaController(controls)
                        setOnPreparedListener { player ->
                            player.isLooping = false
                            // Start immediately so the attached guide is useful without another navigation step.
                            start()
                        }
                        setOnErrorListener { _, _, _ ->
                            previewError = "No se pudo reproducir este vídeo local. Puedes sustituirlo por otro archivo."
                            true
                        }
                        setVideoURI(uri)
                        requestFocus()
                        videoView = this
                    }
                },
            )
            DisposableEffect(media.localUri) {
                onDispose {
                    videoView?.setOnPreparedListener(null)
                    videoView?.setOnErrorListener(null)
                    videoView?.stopPlayback()
                    videoView = null
                }
            }
        }

        else -> previewError = "Este tipo de archivo no admite vista previa en la guía."
    }
    previewError?.let { Text(it, color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
}

@Composable
private fun MartialTechniqueEditor(
    title: String,
    initial: MartialTechnique?,
    onSave: (String, String?, MartialTechniqueFamily?, String?, String?) -> Unit,
) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var translation by remember(initial) { mutableStateOf(initial?.translation.orEmpty()) }
    var family by remember(initial) { mutableStateOf(initial?.family ?: MartialTechniqueFamily.OTHER) }
    var description by remember(initial) { mutableStateOf(initial?.description.orEmpty()) }
    var notes by remember(initial) { mutableStateOf(initial?.notes.orEmpty()) }
    QuestScreen { LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { QuestSectionHeader(title, "Información de la biblioteca técnica.") }
        item { QuestPanel { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            QuestTextField(name, { name = it }, "Nombre", singleLine = true)
            QuestTextField(translation, { translation = it }, "Traducción opcional")
            QuestSingleChoiceMenu(family, MartialTechniqueFamily.entries, "Familia", MartialTechniqueFamily::spanishLabel, { family = it })
            QuestTextField(description, { description = it }, "Descripción")
            QuestTextField(notes, { notes = it }, "Notas personales (opcional)")
            QuestActionButton(QuestAction.Save, { onSave(name, translation.ifBlank { null }, family, description.ifBlank { null }, notes.ifBlank { null }) }, enabled = name.isNotBlank())
        } } }
    } }
}

@Composable
private fun MartialStanceEditor(
    title: String,
    initial: MartialStance?,
    onSave: (String, String?, String?, String?) -> Unit,
) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var translation by remember(initial) { mutableStateOf(initial?.translation.orEmpty()) }
    var description by remember(initial) { mutableStateOf(initial?.description.orEmpty()) }
    var notes by remember(initial) { mutableStateOf(initial?.notes.orEmpty()) }
    QuestScreen { LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { QuestSectionHeader(title, "Información de la biblioteca de posiciones.") }
        item { QuestPanel { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            QuestTextField(name, { name = it }, "Nombre", singleLine = true)
            QuestTextField(translation, { translation = it }, "Traducción opcional")
            QuestTextField(description, { description = it }, "Descripción")
            QuestTextField(notes, { notes = it }, "Notas personales (opcional)")
            QuestActionButton(QuestAction.Save, { onSave(name, translation.ifBlank { null }, description.ifBlank { null }, notes.ifBlank { null }) }, enabled = name.isNotBlank())
        } } }
    } }
}

@Composable
private fun MartialContentDetailDialog(
    content: com.gymquest.app.domain.model.MartialContent,
    onOpen: () -> Unit,
    onPractice: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(content.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Tipo: ${content.contentType}")
                Text("Estado: ${content.progressStatus.replace('_', ' ')}")
                content.description?.let { Text(it) }
                QuestButton("Ver secuencia", onOpen, action = QuestAction.OpenDetail)
                QuestButton("Registrar práctica", onPractice, action = QuestAction.Start)
                QuestButton("Editar", onEdit, action = QuestAction.Edit)
                QuestButton("Eliminar", onDelete, action = QuestAction.Delete)
            }
        },
        confirmButton = { QuestButton("Cerrar", onDismiss, action = QuestAction.Cancel) },
    )
}

@Composable
private fun MartialTechniqueDetailDialog(
    technique: MartialTechnique,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(technique.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                technique.translation?.let { Text(it) }
                technique.family?.let { Text("Familia: ${it.spanishLabel()}") }
                technique.description?.let { Text(it) }
                technique.notes?.let { Text("Notas: $it") }
                QuestButton("Editar", onEdit, action = QuestAction.Edit)
                QuestButton("Eliminar", onDelete, action = QuestAction.Delete)
            }
        },
        confirmButton = { QuestButton("Cerrar", onDismiss, action = QuestAction.Cancel) },
    )
}

@Composable
private fun MartialStanceDetailDialog(
    stance: MartialStance,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stance.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                stance.translation?.let { Text(it) }
                stance.description?.let { Text(it) }
                stance.notes?.let { Text("Notas: $it") }
                QuestButton("Editar", onEdit, action = QuestAction.Edit)
                QuestButton("Eliminar", onDelete, action = QuestAction.Delete)
            }
        },
        confirmButton = { QuestButton("Cerrar", onDismiss, action = QuestAction.Cancel) },
    )
}

@Composable
private fun MartialTechniqueDialog(initial: MartialTechnique? = null, onDismiss: () -> Unit, onSave: (String, String?, MartialTechniqueFamily?, String?, String?) -> Unit) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var translation by remember(initial) { mutableStateOf(initial?.translation.orEmpty()) }
    var family by remember(initial) { mutableStateOf(initial?.family ?: MartialTechniqueFamily.OTHER) }
    var description by remember(initial) { mutableStateOf(initial?.description.orEmpty()) }
    var notes by remember(initial) { mutableStateOf(initial?.notes.orEmpty()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (initial == null) "Nueva técnica" else "Editar técnica") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            QuestTextField(name, { name = it }, "Nombre", singleLine = true)
            QuestTextField(translation, { translation = it }, "Traducción opcional")
            QuestSingleChoiceMenu(family, MartialTechniqueFamily.entries, "Familia", MartialTechniqueFamily::spanishLabel, { family = it })
            QuestTextField(description, { description = it }, "Descripción")
            QuestTextField(notes, { notes = it }, "Notas personales (opcional)")
        }
    }, confirmButton = { QuestActionButton(QuestAction.Save, { onSave(name, translation.ifBlank { null }, family, description.ifBlank { null }, notes.ifBlank { null }) }, enabled = name.isNotBlank()) }, dismissButton = { QuestButton("Cancelar", onDismiss, action = QuestAction.Cancel) })
}

@Composable
private fun MartialStanceDialog(initial: MartialStance? = null, onDismiss: () -> Unit, onSave: (String, String?, String?, String?) -> Unit) {
    var name by remember(initial) { mutableStateOf(initial?.name.orEmpty()) }
    var translation by remember(initial) { mutableStateOf(initial?.translation.orEmpty()) }
    var description by remember(initial) { mutableStateOf(initial?.description.orEmpty()) }
    var notes by remember(initial) { mutableStateOf(initial?.notes.orEmpty()) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (initial == null) "Nueva posición" else "Editar posición") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            QuestTextField(name, { name = it }, "Nombre", singleLine = true)
            QuestTextField(translation, { translation = it }, "Traducción opcional")
            QuestTextField(description, { description = it }, "Descripción")
            QuestTextField(notes, { notes = it }, "Notas personales (opcional)")
        }
    }, confirmButton = { QuestActionButton(QuestAction.Save, { onSave(name, translation.ifBlank { null }, description.ifBlank { null }, notes.ifBlank { null }) }, enabled = name.isNotBlank()) }, dismissButton = { QuestButton("Cancelar", onDismiss, action = QuestAction.Cancel) })
}

@Composable
private fun MartialStepDialog(step: MartialContentStep? = null, techniques: List<MartialTechnique>, stances: List<MartialStance>, onDismiss: () -> Unit, onSave: (MartialContentStep) -> Unit) {
    val defaultStance = stances.firstOrNull()
    val defaultTechnique = techniques.firstOrNull()
    var stanceId by remember(step, stances) { mutableStateOf(step?.stanceId ?: defaultStance?.id) }
    var techniqueId by remember(step, techniques) { mutableStateOf(step?.techniqueId ?: defaultTechnique?.id) }
    var direction by remember(step) { mutableStateOf(step?.direction ?: MartialDirection.FRONT) }
    var side by remember(step) { mutableStateOf(step?.side ?: MartialSide.BOTH) }
    var displacement by remember(step) { mutableStateOf(step?.displacement.orEmpty()) }
    var turn by remember(step) { mutableStateOf(step?.turnDegrees?.toString().orEmpty()) }
    var angle by remember(step) { mutableStateOf(step?.angleDegrees?.toString().orEmpty()) }
    var description by remember(step) { mutableStateOf(step?.description.orEmpty()) }
    var kiai by remember(step) { mutableStateOf(step?.hasKiai ?: false) }
    var pause by remember(step) { mutableStateOf(step?.hasPause ?: false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (step == null) "Movimiento de kata" else "Editar movimiento") }, text = {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { if (stances.isEmpty()) Text("Añade posiciones a la biblioteca del estilo para seleccionarlas aquí.") else QuestSingleChoiceMenu(stanceId, listOf(null) + stances.map { it.id }, "Posición de piernas", { id -> stances.firstOrNull { it.id == id }?.name ?: "Sin posición" }, { stanceId = it }) }
            item { if (techniques.isEmpty()) Text("Añade técnicas a la biblioteca del estilo para seleccionarlas aquí.") else QuestSingleChoiceMenu(techniqueId, listOf(null) + techniques.map { it.id }, "Técnica / brazos", { id -> techniques.firstOrNull { it.id == id }?.name ?: "Sin técnica" }, { techniqueId = it }) }
            item { QuestSingleChoiceMenu(direction, MartialDirection.entries, "Dirección", ::directionSpanish, { direction = it }) }
            item { QuestSingleChoiceMenu(side, MartialSide.entries, "Lado", MartialSide::spanishLabel, { side = it }) }
            item { QuestTextField(displacement, { displacement = it }, "Desplazamiento (opcional)") }
            item { QuestNumericField(turn, { turn = it }, "Giro", "°", integerOnly = true) }
            item { QuestNumericField(angle, { angle = it }, "Ángulo", "°", integerOnly = true) }
            item { androidx.compose.material3.Checkbox(checked = kiai, onCheckedChange = { kiai = it }); Text("Kiai") }
            item { androidx.compose.material3.Checkbox(checked = pause, onCheckedChange = { pause = it }); Text("Pausa") }
            item { QuestTextField(description, { description = it }, "Notas / descripción") }
            item { Text("Multimedia local: se podrá adjuntar tras guardar el movimiento.") }
        }
    }, confirmButton = { QuestActionButton(QuestAction.Save, {
        onSave(MartialContentStep(technicalContentId = step?.technicalContentId ?: 1, orderIndex = step?.orderIndex ?: 0, stanceId = stanceId, techniqueId = techniqueId, direction = direction, side = side, displacement = displacement.ifBlank { null }, turnDegrees = turn.toIntOrNull(), angleDegrees = angle.toIntOrNull(), description = description.ifBlank { null }, hasKiai = kiai, hasPause = pause, createdAt = step?.createdAt ?: Instant.now(), updatedAt = Instant.now()))
    }) }, dismissButton = { QuestButton("Cancelar", onDismiss, action = QuestAction.Cancel) })
}

@Composable
private fun MartialSequencePracticeDialog(steps: List<MartialContentStep>, stances: List<MartialStance>, techniques: List<MartialTechnique>, onDismiss: () -> Unit) {
    var index by remember { mutableStateOf(0) }
    var autoplay by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(autoplay, index) { if (autoplay && index < steps.lastIndex) { delay(3000); index += 1 } else if (index == steps.lastIndex) autoplay = false }
    val step = steps.getOrNull(index) ?: return
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Práctica: movimiento ${index + 1} / ${steps.size}") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text(stepSummary(step, stances, techniques), style = androidx.compose.material3.MaterialTheme.typography.titleLarge); Text("Dirección: ${directionSpanish(step.direction)}"); step.description?.let { Text(it) }; Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { QuestButton("← Anterior", { index = (index - 1).coerceAtLeast(0) }, enabled = index > 0); QuestButton("Siguiente →", { index = (index + 1).coerceAtMost(steps.lastIndex) }, enabled = index < steps.lastIndex, action = QuestAction.Start) }; QuestButton(if (autoplay) "Detener reproducción" else "▶ Reproducir kata", { autoplay = !autoplay }, action = if (autoplay) QuestAction.Pause else QuestAction.Start) } }, confirmButton = { QuestButton("Cerrar", onDismiss, action = QuestAction.Cancel) })
}

private fun MartialTechniqueFamily.spanishLabel() = when (this) { MartialTechniqueFamily.PUNCH -> "Puño"; MartialTechniqueFamily.KICK -> "Patada"; MartialTechniqueFamily.BLOCK -> "Recepción"; MartialTechniqueFamily.OPEN_HAND -> "Mano abierta"; MartialTechniqueFamily.ARM_POSTURE -> "Postura de brazos"; MartialTechniqueFamily.CONTROL -> "Control"; MartialTechniqueFamily.OTHER -> "Otra" }
private fun MartialSide.spanishLabel() = when (this) { MartialSide.LEFT -> "Izquierdo"; MartialSide.RIGHT -> "Derecho"; MartialSide.BOTH -> "Ambos" }
private fun directionSpanish(value: MartialDirection) = when (value) { MartialDirection.FRONT -> "Frente"; MartialDirection.BACK -> "Atrás"; MartialDirection.LEFT -> "Izquierda"; MartialDirection.RIGHT -> "Derecha"; MartialDirection.FRONT_LEFT -> "Diagonal frontal izquierda"; MartialDirection.FRONT_RIGHT -> "Diagonal frontal derecha"; MartialDirection.BACK_LEFT -> "Diagonal trasera izquierda"; MartialDirection.BACK_RIGHT -> "Diagonal trasera derecha" }
private fun stepSummary(step: MartialContentStep, stances: List<MartialStance>, techniques: List<MartialTechnique>) = listOfNotNull(stances.firstOrNull { it.id == step.stanceId }?.name, techniques.firstOrNull { it.id == step.techniqueId }?.name).joinToString(" + ").ifBlank { "Movimiento sin técnica ni posición" }
private fun moveStep(steps: List<MartialContentStep>, from: Int, delta: Int): List<MartialContentStep> { val ordered = steps.sortedBy { it.orderIndex }.toMutableList(); val target = (from + delta).coerceIn(0, ordered.lastIndex); val item = ordered.removeAt(from); ordered.add(target, item); return ordered }

@Composable
private fun MartialFormDialog(title: String, nameLabel: String, secondaryLabel: String, onDismiss: () -> Unit, initialName: String = "", initialSecondary: String = "", onSave: (String, String?) -> Unit) {
    var name by remember { mutableStateOf(initialName) }; var secondary by remember { mutableStateOf(initialSecondary) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { QuestTextField(name, { name = it }, nameLabel, singleLine = true); QuestTextField(secondary, { secondary = it }, secondaryLabel) } }, confirmButton = { QuestActionButton(QuestAction.Save, { onSave(name, secondary) }, enabled = name.isNotBlank()) }, dismissButton = { QuestButton("Cancelar", onDismiss, action = QuestAction.Cancel) })
}

@Composable
private fun MartialContentDialog(onDismiss: () -> Unit, initialName: String = "", initialType: String = "Kata", initialDescription: String = "", onSave: (String, String, String?) -> Unit) {
    var name by remember { mutableStateOf(initialName) }; var type by remember { mutableStateOf(initialType) }; var description by remember { mutableStateOf(initialDescription) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Nuevo contenido técnico") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { QuestTextField(name, { name = it }, "Nombre", singleLine = true); QuestTextField(type, { type = it }, "Tipo (kata, forma, drill)", singleLine = true); QuestTextField(description, { description = it }, "Teoría / descripción (opcional)") } }, confirmButton = { QuestActionButton(QuestAction.Save, { onSave(name, type, description) }, enabled = name.isNotBlank() && type.isNotBlank()) }, dismissButton = { QuestButton("Cancelar", onDismiss, action = QuestAction.Cancel) })
}

@Composable
private fun MartialPracticeDialog(contentName: String, onDismiss: () -> Unit, onSave: (Int, String?) -> Unit) {
    var minutes by remember { mutableStateOf("10") }; var notes by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Practicar $contentName") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { QuestNumericField(minutes, { minutes = it }, "Duración", "min", integerOnly = true); QuestTextField(notes, { notes = it }, "Notas opcionales") } }, confirmButton = { QuestActionButton(QuestAction.Save, { onSave(minutes.toIntOrNull() ?: 0, notes) }, enabled = (minutes.toIntOrNull() ?: 0) > 0) }, dismissButton = { QuestButton("Cancelar", onDismiss, action = QuestAction.Cancel) })
}

private fun relativePractice(value: Instant): String { val days = Duration.between(value, Instant.now()).toDays().coerceAtLeast(0); return when (days) { 0L -> "hoy"; 1L -> "ayer"; else -> "hace $days días" } }

@Composable
private fun martialViewModel(): MartialArtsViewModel {
    val container = (LocalContext.current.applicationContext as GymQuestApp).appContainer
    return viewModel(factory = viewModelFactory { initializer { MartialArtsViewModel(getArts = container.getActiveMartialArtsUseCase, getStyles = container.getActiveMartialStylesUseCase, getTechniques = container.getActiveMartialTechniquesUseCase, getStances = container.getActiveMartialStancesUseCase, ensureShitoRyuCatalogUseCase = container.ensureShitoRyuCatalogUseCase, getContentDetail = container.getMartialContentDetailUseCase, createArt = container.createMartialArtUseCase, createStyle = container.createMartialStyleUseCase, createContent = container.createMartialContentUseCase, createTechnique = container.createMartialTechniqueUseCase, createStance = container.createMartialStanceUseCase, updateContentUseCase = container.updateMartialContentUseCase, updateTechniqueUseCase = container.updateMartialTechniqueUseCase, updateStanceUseCase = container.updateMartialStanceUseCase, archiveContent = container.archiveMartialContentUseCase, archiveTechnique = container.archiveMartialTechniqueUseCase, deleteArtUseCase = container.deleteMartialArtUseCase, deleteStyleUseCase = container.deleteMartialStyleUseCase, deleteContentUseCase = container.deleteMartialContentUseCase, deleteTechniqueUseCase = container.deleteMartialTechniqueUseCase, deleteStanceUseCase = container.deleteMartialStanceUseCase, replaceStepMediaUseCase = container.replaceMartialContentStepMediaUseCase, removeStepMediaUseCase = container.removeMartialContentStepMediaUseCase, observeStepMediaUseCase = container.observeMartialContentStepMediaUseCase, replaceContentMediaUseCase = container.replaceMartialTechnicalContentMediaUseCase, removeContentMediaUseCase = container.removeMartialTechnicalContentMediaUseCase, observeContentMediaUseCase = container.observeMartialTechnicalContentMediaUseCase, replaceTechniqueMediaUseCase = container.replaceMartialTechniqueMediaUseCase, removeTechniqueMediaUseCase = container.removeMartialTechniqueMediaUseCase, observeTechniqueMediaUseCase = container.observeMartialTechniqueMediaUseCase, replaceStanceMediaUseCase = container.replaceMartialStanceMediaUseCase, removeStanceMediaUseCase = container.removeMartialStanceMediaUseCase, observeStanceMediaUseCase = container.observeMartialStanceMediaUseCase, addStepUseCase = container.addMartialContentStepUseCase, updateStepUseCase = container.updateMartialContentStepUseCase, deleteStepUseCase = container.deleteMartialContentStepUseCase, replaceStepsUseCase = container.replaceMartialContentStepsUseCase, updateArtUseCase = container.updateMartialArtUseCase, updateStyleUseCase = container.updateMartialStyleUseCase, archiveArtUseCase = container.archiveMartialArtUseCase, archiveStyleUseCase = container.archiveMartialStyleUseCase, observeProgress = container.observeMartialProgressUseCase, registerPractice = container.registerMartialPracticeUseCase, clock = SystemClockProvider) } })
}

@Composable
private fun martialBeltViewModel(): MartialBeltViewModel {
    val container = (LocalContext.current.applicationContext as GymQuestApp).appContainer
    return viewModel(factory = viewModelFactory {
        initializer { MartialBeltViewModel(container.getCurrentMartialBeltUseCase, container.setCurrentMartialBeltUseCase) }
    })
}
