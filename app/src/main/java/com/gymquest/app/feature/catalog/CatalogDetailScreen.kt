package com.gymquest.app.feature.catalog

import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymquest.app.core.ui.component.EmptyAdventureState
import com.gymquest.app.core.ui.component.QuestAction
import com.gymquest.app.core.ui.component.QuestActionButton
import com.gymquest.app.core.ui.component.QuestButton
import com.gymquest.app.core.ui.component.QuestConfirmationDialog
import com.gymquest.app.core.ui.component.QuestFilterChip
import com.gymquest.app.core.ui.component.QuestPanel
import com.gymquest.app.core.ui.component.QuestScreen
import com.gymquest.app.core.ui.component.QuestSectionHeader
import com.gymquest.app.core.ui.component.QuestTextField
import com.gymquest.app.core.ui.component.StatBadge
import com.gymquest.app.domain.model.ExerciseBase
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.model.MediaFile
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import com.gymquest.app.domain.model.enums.MediaType

@Composable
fun CatalogDetailScreen(exerciseBaseId: Long, onAddVariant: (Long) -> Unit) {
    val viewModel = rememberCatalogViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val entry = state.catalog.firstOrNull { it.exerciseBase.id == exerciseBaseId }
    val context = LocalContext.current
    var mediaPickerError by remember { mutableStateOf<String?>(null) }
    val mediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val mimeType = context.contentResolver.getType(uri)?.lowercase()
        val mediaType = when {
            mimeType == "image/gif" -> MediaType.GIF
            mimeType?.startsWith("video/") == true -> MediaType.VIDEO
            else -> null
        }
        if (mediaType == null) {
            mediaPickerError = "Elige solo un GIF o un vídeo local."
            return@rememberLauncherForActivityResult
        }
        try {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.replaceExerciseBaseMedia(
                exerciseBaseId,
                ExerciseMediaDraft(
                    uri = uri.toString(),
                    mimeType = mimeType ?: return@rememberLauncherForActivityResult,
                    title = context.contentResolver.displayNameForDetail(uri) ?: "Guía local",
                    mediaType = mediaType,
                ),
            )
            mediaPickerError = null
        } catch (_: SecurityException) {
            mediaPickerError = "No se pudo conservar el acceso al archivo seleccionado."
        }
    }
    QuestScreen {
        if (state.isLoading) {
            EmptyAdventureState("Cargando ejercicio", "Preparando la ficha del ejercicio.")
        } else if (entry == null) {
            EmptyAdventureState("Ejercicio no disponible", "Es posible que se haya archivado.")
        } else {
            CatalogDetailContent(
                entry.exerciseBase,
                entry.variants,
                media = state.mediaByExerciseBaseId[exerciseBaseId],
                builtInGifUrl = entry.exerciseBase.builtInGifUrl,
                mediaPickerError = mediaPickerError,
                onChooseMedia = { mediaLauncher.launch(arrayOf("image/gif", "video/*")) },
                onRemoveMedia = { viewModel.removeExerciseBaseMedia(exerciseBaseId) },
                onUpdateBase = viewModel::updateExerciseBase,
                onArchiveBase = viewModel::archiveExerciseBase,
                onAddVariant = { onAddVariant(exerciseBaseId) },
                onUpdateVariant = viewModel::updateExerciseVariant,
                onArchiveVariant = viewModel::archiveExerciseVariant,
            )
        }
    }
}

@Composable
private fun CatalogDetailContent(
    base: ExerciseBase,
    variants: List<ExerciseVariant>,
    media: MediaFile?,
    builtInGifUrl: String?,
    mediaPickerError: String?,
    onChooseMedia: () -> Unit,
    onRemoveMedia: () -> Unit,
    onUpdateBase: (ExerciseBase) -> Unit,
    onArchiveBase: (Long) -> Unit,
    onAddVariant: () -> Unit,
    onUpdateVariant: (ExerciseVariant) -> Unit,
    onArchiveVariant: (Long) -> Unit,
) {
    var editingBase by remember(base.id) { mutableStateOf(false) }
    var baseName by remember(base.id) { mutableStateOf(base.name) }
    var description by remember(base.id) { mutableStateOf(base.description.orEmpty()) }
    var secondaryMuscles by remember(base.id) { mutableStateOf(base.secondaryMuscles.orEmpty()) }
    var instructions by remember(base.id) { mutableStateOf(base.instructions.orEmpty()) }
    var techniqueTips by remember(base.id) { mutableStateOf(base.techniqueTips.orEmpty()) }
    var commonMistakes by remember(base.id) { mutableStateOf(base.commonMistakes.orEmpty()) }
    var editVariant by remember { mutableStateOf<ExerciseVariant?>(null) }
    var archiveBaseRequested by remember { mutableStateOf(false) }
    var archiveVariantId by remember { mutableStateOf<Long?>(null) }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { QuestSectionHeader("Ficha del ejercicio", "Datos, guía visual y variantes para entrenar.") }
        item {
            QuestPanel {
                if (editingBase) {
                    QuestTextField(baseName, { baseName = it }, "Nombre", singleLine = true)
                    QuestTextField(description, { description = it }, "Descripción")
                    QuestTextField(secondaryMuscles, { secondaryMuscles = it }, "Músculos secundarios (separados por comas)")
                    QuestTextField(instructions, { instructions = it }, "Pasos de ejecución (uno por línea)")
                    QuestTextField(techniqueTips, { techniqueTips = it }, "Consejos de técnica")
                    QuestTextField(commonMistakes, { commonMistakes = it }, "Errores comunes")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuestActionButton(QuestAction.Save, label = "Guardar", onClick = {
                            onUpdateBase(base.copy(
                                name = baseName.trim(),
                                description = description.trim().ifBlank { null },
                                secondaryMuscles = secondaryMuscles.trim().ifBlank { null },
                                instructions = instructions.trim().ifBlank { null },
                                techniqueTips = techniqueTips.trim().ifBlank { null },
                                commonMistakes = commonMistakes.trim().ifBlank { null },
                            ))
                            editingBase = false
                        })
                        QuestButton("Cancelar", { editingBase = false }, action = QuestAction.Back)
                    }
                } else {
                    Text(base.name, style = MaterialTheme.typography.titleLarge)
                    base.description?.let { Text(it) }
                    base.secondaryMuscles?.let { Text("Músculos secundarios: $it", style = MaterialTheme.typography.bodySmall) }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuestButton("Editar", { editingBase = true }, action = QuestAction.Edit)
                        QuestButton("Archivar", { archiveBaseRequested = true }, action = QuestAction.Delete)
                    }
                }
            }
        }
        item {
            ExerciseGuideSection(
                media = media,
                builtInGifUrl = builtInGifUrl,
                pickerError = mediaPickerError,
                onChooseMedia = onChooseMedia,
                onRemoveMedia = onRemoveMedia,
            )
        }
        base.instructions?.takeIf { it.isNotBlank() }?.let { guide ->
            item {
                QuestPanel {
                    Text("Cómo hacerlo", style = MaterialTheme.typography.titleMedium)
                    guide.lineSequence().filter(String::isNotBlank).forEachIndexed { index, step ->
                        Text("${index + 1}. $step")
                    }
                }
            }
        }
        base.techniqueTips?.takeIf { it.isNotBlank() }?.let { tips ->
            item { ExerciseTheoryPanel("Consejos de técnica", tips) }
        }
        base.commonMistakes?.takeIf { it.isNotBlank() }?.let { mistakes ->
            item { ExerciseTheoryPanel("Errores comunes", mistakes) }
        }
        item { QuestSectionHeader("Variantes", "Configura equipo y forma de comparar el peso.") }
        items(variants, key = { it.id }) { variant ->
            VariantCard(variant, editVariant?.id == variant.id, onEdit = { editVariant = variant }, onCancelEdit = { editVariant = null }, onUpdate = onUpdateVariant, onArchive = { archiveVariantId = variant.id })
        }
        item { QuestButton("Nueva variante", onAddVariant, action = QuestAction.Add) }
    }
    if (archiveBaseRequested) QuestConfirmationDialog(
        title = "Archivar ejercicio",
        message = "Dejará de aparecer en el catálogo.",
        confirmLabel = "Archivar",
        onConfirm = { onArchiveBase(base.id); archiveBaseRequested = false },
        onDismiss = { archiveBaseRequested = false },
        confirmAction = QuestAction.Delete,
    )
    archiveVariantId?.let { id -> QuestConfirmationDialog(
        title = "Archivar variante",
        message = "Dejará de aparecer como opción en el catálogo.",
        confirmLabel = "Archivar",
        onConfirm = { onArchiveVariant(id); archiveVariantId = null },
        onDismiss = { archiveVariantId = null },
        confirmAction = QuestAction.Delete,
    ) }
}

@Composable
private fun ExerciseGuideSection(
    media: MediaFile?,
    builtInGifUrl: String?,
    pickerError: String?,
    onChooseMedia: () -> Unit,
    onRemoveMedia: () -> Unit,
) {
    QuestPanel {
        Text("Guía visual", style = MaterialTheme.typography.titleMedium)
        if (media == null) {
            if (builtInGifUrl != null) {
                CatalogBuiltInGifPreview(builtInGifUrl)
                Text("Demostración incluida en la aplicación. Puedes sustituirla por tu propio archivo local.", style = MaterialTheme.typography.bodySmall)
            } else {
                Text("Añade un GIF o vídeo local para consultar la técnica en esta ficha.")
            }
            QuestButton("Añadir GIF o vídeo", onChooseMedia, action = QuestAction.Add)
        } else {
            Text(media.title ?: "Archivo local", style = MaterialTheme.typography.bodyMedium)
            CatalogLocalGuidePreview(media)
            Text(
                if (media.mediaType == MediaType.VIDEO) "Vídeo local seleccionado." else "GIF local seleccionado.",
                style = MaterialTheme.typography.bodySmall,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuestButton("Sustituir archivo", onChooseMedia, action = QuestAction.Edit)
                QuestButton("Eliminar guía", onRemoveMedia, action = QuestAction.Delete)
            }
        }
        pickerError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

/** Shows only the allow-listed, catalogue-owned GIF URLs seeded with the application. */
@Composable
internal fun CatalogBuiltInGifPreview(url: String) {
    if (!url.startsWith("file:///android_asset/exercise_gifs/") || !url.endsWith(".gif")) {
        Text("La demostración visual no está disponible.", color = MaterialTheme.colorScheme.error)
        return
    }
    var webView by remember(url) { mutableStateOf<WebView?>(null) }
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        factory = { viewContext ->
            WebView(viewContext).apply {
                settings.javaScriptEnabled = false
                settings.domStorageEnabled = false
                webViewClient = WebViewClient()
                contentDescription = "Demostración animada del ejercicio"
                loadUrl(url)
                webView = this
            }
        },
        update = { view -> if (view.url != url) view.loadUrl(url) },
    )
    DisposableEffect(url) {
        onDispose {
            webView?.stopLoading()
            webView?.destroy()
            webView = null
        }
    }
}

@Composable
private fun ExerciseTheoryPanel(title: String, content: String) {
    QuestPanel {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(content)
    }
}

/** Displays the user's persisted local guide directly in the exercise sheet. */
@Composable
internal fun CatalogLocalGuidePreview(media: MediaFile) {
    val context = LocalContext.current
    val uri = remember(media.localUri) { Uri.parse(media.localUri) }
    var previewError by remember(media.localUri) { mutableStateOf<String?>(null) }

    when (media.mediaType) {
        MediaType.GIF -> {
            var imageView by remember(media.localUri) { mutableStateOf<ImageView?>(null) }
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(240.dp),
                factory = { viewContext ->
                    ImageView(viewContext).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        contentDescription = "Vista previa del GIF del ejercicio"
                        imageView = this
                        runCatching {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                ImageDecoder.decodeDrawable(ImageDecoder.createSource(viewContext.contentResolver, uri))
                            } else null
                        }.onSuccess { drawable ->
                            if (drawable != null) {
                                setImageDrawable(drawable)
                                (drawable as? AnimatedImageDrawable)?.start()
                            } else setImageURI(uri)
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
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        contentDescription = "Reproductor de vídeo del ejercicio"
                        setMediaController(MediaController(viewContext).also { it.setAnchorView(this) })
                        setOnPreparedListener { player -> player.isLooping = false; start() }
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
    previewError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}

@Composable
private fun VariantCard(variant: ExerciseVariant, editing: Boolean, onEdit: () -> Unit, onCancelEdit: () -> Unit, onUpdate: (ExerciseVariant) -> Unit, onArchive: () -> Unit) {
    var name by remember(variant.id) { mutableStateOf(variant.name) }
    var notes by remember(variant.id) { mutableStateOf(variant.notes.orEmpty()) }
    QuestPanel {
        if (editing) {
            QuestTextField(name, { name = it }, "Nombre de variante", singleLine = true)
            QuestTextField(notes, { notes = it }, "Notas")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuestActionButton(QuestAction.Save, label = "Guardar", onClick = { onUpdate(variant.copy(name = name.trim(), notes = notes.trim().ifBlank { null })); onCancelEdit() })
                QuestButton("Cancelar", onCancelEdit, action = QuestAction.Back)
            }
        } else {
            Text(variant.name, style = MaterialTheme.typography.titleMedium)
            Text("${variant.equipmentType.label()} - ${variant.weightComparisonType.label()}", style = MaterialTheme.typography.bodySmall)
            variant.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuestButton("Editar", onEdit, action = QuestAction.Edit)
                QuestButton("Archivar", onArchive, action = QuestAction.Delete)
            }
        }
    }
}

@Composable
internal fun VariantConfigurationSelector(equipment: EquipmentType, weight: WeightComparisonType, onEquipment: (EquipmentType) -> Unit, onWeight: (WeightComparisonType) -> Unit) {
    Text("Equipo", style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EquipmentType.entries.forEach { QuestFilterChip(it.label(), equipment == it, { onEquipment(it) }) }
    }
    Text("Cómo comparar el peso", style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WeightComparisonType.entries.forEach { QuestFilterChip(it.label(), weight == it, { onWeight(it) }) }
    }
}

internal fun EquipmentType.label() = when (this) {
    EquipmentType.BARBELL -> "Barra"; EquipmentType.DUMBBELL -> "Mancuernas"; EquipmentType.MACHINE -> "Máquina"; EquipmentType.CABLE -> "Polea"; EquipmentType.BODYWEIGHT -> "Peso corporal"; EquipmentType.MULTIPOWER -> "Multipower"; EquipmentType.KETTLEBELL -> "Kettlebell"; EquipmentType.ELASTIC_BAND -> "Banda elástica"; EquipmentType.OTHER -> "Otro"
}
internal fun WeightComparisonType.label() = when (this) {
    WeightComparisonType.TOTAL_WEIGHT -> "Peso total"; WeightComparisonType.PER_DUMBBELL -> "Por mancuerna"; WeightComparisonType.PER_SIDE -> "Por lado"; WeightComparisonType.BODYWEIGHT -> "Peso corporal"; WeightComparisonType.ASSISTED_WEIGHT -> "Peso asistido"; WeightComparisonType.NOT_COMPARABLE_BETWEEN_MACHINES -> "No comparable entre máquinas"
}

private fun android.content.ContentResolver.displayNameForDetail(uri: Uri): String? =
    query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        cursor.takeIf { it.moveToFirst() }?.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
    }
