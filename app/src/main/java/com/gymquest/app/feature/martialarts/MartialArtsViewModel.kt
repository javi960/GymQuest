package com.gymquest.app.feature.martialarts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.core.time.ClockProvider
import com.gymquest.app.domain.model.MartialArt
import com.gymquest.app.domain.model.MartialContent
import com.gymquest.app.domain.model.MartialContentDetail
import com.gymquest.app.domain.model.MartialContentStep
import com.gymquest.app.domain.model.MartialPractice
import com.gymquest.app.domain.model.MartialPracticeItem
import com.gymquest.app.domain.model.MartialProgress
import com.gymquest.app.domain.model.MartialStyle
import com.gymquest.app.domain.model.MartialTechnique
import com.gymquest.app.domain.model.MartialStance
import com.gymquest.app.domain.model.MediaFile
import com.gymquest.app.domain.model.enums.MartialDirection
import com.gymquest.app.domain.usecase.martial.ArchiveMartialContentUseCase
import com.gymquest.app.domain.usecase.martial.ArchiveMartialTechniqueUseCase
import com.gymquest.app.domain.usecase.martial.CreateMartialArtUseCase
import com.gymquest.app.domain.usecase.martial.CreateMartialContentUseCase
import com.gymquest.app.domain.usecase.martial.CreateMartialStyleUseCase
import com.gymquest.app.domain.usecase.martial.CreateMartialTechniqueUseCase
import com.gymquest.app.domain.usecase.martial.GetActiveMartialArtsUseCase
import com.gymquest.app.domain.usecase.martial.GetActiveMartialStylesUseCase
import com.gymquest.app.domain.usecase.martial.GetActiveMartialTechniquesUseCase
import com.gymquest.app.domain.usecase.martial.GetMartialContentDetailUseCase
import com.gymquest.app.domain.usecase.martial.ObserveMartialProgressUseCase
import com.gymquest.app.domain.usecase.martial.RegisterMartialPracticeUseCase
import com.gymquest.app.domain.usecase.martial.UpdateMartialContentUseCase
import com.gymquest.app.domain.usecase.martial.UpdateMartialTechniqueUseCase
import com.gymquest.app.domain.usecase.martial.AddMartialContentStepUseCase
import com.gymquest.app.domain.usecase.martial.DeleteMartialContentStepUseCase
import com.gymquest.app.domain.usecase.martial.UpdateMartialContentStepUseCase
import com.gymquest.app.domain.usecase.martial.ReplaceMartialContentStepsUseCase
import com.gymquest.app.domain.usecase.martial.UpdateMartialArtUseCase
import com.gymquest.app.domain.usecase.martial.UpdateMartialStyleUseCase
import com.gymquest.app.domain.usecase.martial.ArchiveMartialArtUseCase
import com.gymquest.app.domain.usecase.martial.ArchiveMartialStyleUseCase
import com.gymquest.app.domain.usecase.martial.DeleteMartialArtUseCase
import com.gymquest.app.domain.usecase.martial.DeleteMartialStyleUseCase
import com.gymquest.app.domain.usecase.martial.DeleteMartialContentUseCase
import com.gymquest.app.domain.usecase.martial.DeleteMartialTechniqueUseCase
import com.gymquest.app.domain.usecase.martial.EnsureShitoRyuCatalogUseCase
import com.gymquest.app.domain.usecase.martial.GetActiveMartialStancesUseCase
import com.gymquest.app.domain.usecase.martial.CreateMartialStanceUseCase
import com.gymquest.app.domain.usecase.martial.UpdateMartialStanceUseCase
import com.gymquest.app.domain.usecase.martial.DeleteMartialStanceUseCase
import com.gymquest.app.domain.usecase.martial.ReplaceMartialContentStepMediaUseCase
import com.gymquest.app.domain.usecase.martial.RemoveMartialContentStepMediaUseCase
import com.gymquest.app.domain.usecase.martial.ObserveMartialContentStepMediaUseCase
import com.gymquest.app.domain.usecase.martial.ReplaceMartialTechnicalContentMediaUseCase
import com.gymquest.app.domain.usecase.martial.RemoveMartialTechnicalContentMediaUseCase
import com.gymquest.app.domain.usecase.martial.ObserveMartialTechnicalContentMediaUseCase
import com.gymquest.app.domain.usecase.martial.ReplaceMartialTechniqueMediaUseCase
import com.gymquest.app.domain.usecase.martial.RemoveMartialTechniqueMediaUseCase
import com.gymquest.app.domain.usecase.martial.ObserveMartialTechniqueMediaUseCase
import com.gymquest.app.domain.usecase.martial.ReplaceMartialStanceMediaUseCase
import com.gymquest.app.domain.usecase.martial.RemoveMartialStanceMediaUseCase
import com.gymquest.app.domain.usecase.martial.ObserveMartialStanceMediaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest

data class MartialArtsUiState(
    val arts: List<MartialArt> = emptyList(),
    val styles: List<MartialStyle> = emptyList(),
    val progress: MartialProgress? = null,
    val techniques: List<MartialTechnique> = emptyList(),
    val stances: List<MartialStance> = emptyList(),
    val contentDetail: MartialContentDetail? = null,
    val mediaByStepId: Map<Long, MediaFile> = emptyMap(),
    val mediaByContentId: Map<Long, MediaFile> = emptyMap(),
    val mediaByTechniqueId: Map<Long, MediaFile> = emptyMap(),
    val mediaByStanceId: Map<Long, MediaFile> = emptyMap(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val message: String? = null,
)

class MartialArtsViewModel(
    private val getArts: GetActiveMartialArtsUseCase,
    private val getStyles: GetActiveMartialStylesUseCase,
    private val getTechniques: GetActiveMartialTechniquesUseCase,
    private val getStances: GetActiveMartialStancesUseCase,
    private val ensureShitoRyuCatalogUseCase: EnsureShitoRyuCatalogUseCase,
    private val getContentDetail: GetMartialContentDetailUseCase,
    private val createArt: CreateMartialArtUseCase,
    private val createStyle: CreateMartialStyleUseCase,
    private val createContent: CreateMartialContentUseCase,
    private val createTechnique: CreateMartialTechniqueUseCase,
    private val createStance: CreateMartialStanceUseCase,
    private val updateContentUseCase: UpdateMartialContentUseCase,
    private val updateTechniqueUseCase: UpdateMartialTechniqueUseCase,
    private val updateStanceUseCase: UpdateMartialStanceUseCase,
    private val archiveContent: ArchiveMartialContentUseCase,
    private val archiveTechnique: ArchiveMartialTechniqueUseCase,
    private val deleteArtUseCase: DeleteMartialArtUseCase,
    private val deleteStyleUseCase: DeleteMartialStyleUseCase,
    private val deleteContentUseCase: DeleteMartialContentUseCase,
    private val deleteTechniqueUseCase: DeleteMartialTechniqueUseCase,
    private val deleteStanceUseCase: DeleteMartialStanceUseCase,
    private val replaceStepMediaUseCase: ReplaceMartialContentStepMediaUseCase,
    private val removeStepMediaUseCase: RemoveMartialContentStepMediaUseCase,
    private val observeStepMediaUseCase: ObserveMartialContentStepMediaUseCase,
    private val replaceContentMediaUseCase: ReplaceMartialTechnicalContentMediaUseCase,
    private val removeContentMediaUseCase: RemoveMartialTechnicalContentMediaUseCase,
    private val observeContentMediaUseCase: ObserveMartialTechnicalContentMediaUseCase,
    private val replaceTechniqueMediaUseCase: ReplaceMartialTechniqueMediaUseCase,
    private val removeTechniqueMediaUseCase: RemoveMartialTechniqueMediaUseCase,
    private val observeTechniqueMediaUseCase: ObserveMartialTechniqueMediaUseCase,
    private val replaceStanceMediaUseCase: ReplaceMartialStanceMediaUseCase,
    private val removeStanceMediaUseCase: RemoveMartialStanceMediaUseCase,
    private val observeStanceMediaUseCase: ObserveMartialStanceMediaUseCase,
    private val addStepUseCase: AddMartialContentStepUseCase,
    private val updateStepUseCase: UpdateMartialContentStepUseCase,
    private val deleteStepUseCase: DeleteMartialContentStepUseCase,
    private val replaceStepsUseCase: ReplaceMartialContentStepsUseCase,
    private val updateArtUseCase: UpdateMartialArtUseCase,
    private val updateStyleUseCase: UpdateMartialStyleUseCase,
    private val archiveArtUseCase: ArchiveMartialArtUseCase,
    private val archiveStyleUseCase: ArchiveMartialStyleUseCase,
    private val observeProgress: ObserveMartialProgressUseCase,
    private val registerPractice: RegisterMartialPracticeUseCase,
    private val clock: ClockProvider,
) : ViewModel() {
    private val mutableState = MutableStateFlow(MartialArtsUiState())
    val uiState: StateFlow<MartialArtsUiState> = mutableState.asStateFlow()
    private var stepMediaJob: Job? = null
    private var contentMediaJob: Job? = null
    private var techniqueMediaJob: Job? = null
    private var stanceMediaJob: Job? = null

    fun loadArts() = viewModelScope.launch { load(request = { getArts() }, success = { copy(arts = it) }) }
    fun loadStyles(artId: Long) = viewModelScope.launch { load(request = { getStyles(artId) }, success = { copy(styles = it) }) }
    fun loadTechniques(styleId: Long) = viewModelScope.launch { load(request = { getTechniques(styleId) }, success = { copy(techniques = it) }); observeTechniqueMedia() }
    fun loadStances(styleId: Long) = viewModelScope.launch { load(request = { getStances(styleId) }, success = { copy(stances = it) }); observeStanceMedia() }
    fun ensureShitoRyuCatalog(styleId: Long) = viewModelScope.launch {
        save(ensureShitoRyuCatalogUseCase(styleId), "Biblioteca Shito-Ryu preparada.") {
            loadTechniques(styleId)
            loadStances(styleId)
        }
    }
    fun loadProgress(styleId: Long) = viewModelScope.launch { load(request = { observeProgress(styleId) }, success = { copy(progress = it) }) }
    fun loadContentDetail(contentId: Long) = viewModelScope.launch {
        load(request = { getContentDetail(contentId) }, success = { copy(contentDetail = it) })
        observeStepMedia()
        observeContentMedia()
    }

    private fun observeStepMedia() {
        stepMediaJob?.cancel()
        stepMediaJob = viewModelScope.launch {
            observeStepMediaUseCase().collectLatest { media ->
                val knownStepIds = mutableState.value.contentDetail?.steps.orEmpty().map { it.id }.toSet()
                mutableState.update { state -> state.copy(mediaByStepId = media.filter { it.ownerId in knownStepIds }.associateBy { it.ownerId }) }
            }
        }
    }

    private fun observeContentMedia() {
        contentMediaJob?.cancel()
        contentMediaJob = viewModelScope.launch {
            observeContentMediaUseCase().collectLatest { media ->
                mutableState.update { state -> state.copy(mediaByContentId = media.associateBy { it.ownerId }) }
            }
        }
    }

    private fun observeTechniqueMedia() {
        techniqueMediaJob?.cancel()
        techniqueMediaJob = viewModelScope.launch {
            observeTechniqueMediaUseCase().collectLatest { media ->
                mutableState.update { state -> state.copy(mediaByTechniqueId = media.associateBy { it.ownerId }) }
            }
        }
    }

    private fun observeStanceMedia() {
        stanceMediaJob?.cancel()
        stanceMediaJob = viewModelScope.launch {
            observeStanceMediaUseCase().collectLatest { media ->
                mutableState.update { state -> state.copy(mediaByStanceId = media.associateBy { it.ownerId }) }
            }
        }
    }

    fun addArt(name: String, description: String?) = viewModelScope.launch {
        val now = clock.now()
        save(createArt(MartialArt(name = name, description = description?.trim()?.ifBlank { null }, createdAt = now, updatedAt = now)), "Disciplina creada.") { loadArts() }
    }

    fun addStyle(artId: Long, name: String, description: String?) = viewModelScope.launch {
        val now = clock.now()
        save(createStyle(MartialStyle(martialArtId = artId, name = name, description = description?.trim()?.ifBlank { null }, createdAt = now, updatedAt = now)), "Estilo creado.") { loadStyles(artId) }
    }

    fun addContent(styleId: Long, name: String, type: String, description: String?) = viewModelScope.launch {
        val now = clock.now()
        save(createContent(MartialContent(martialStyleId = styleId, name = name, contentType = type, description = description?.trim()?.ifBlank { null }, createdAt = now, updatedAt = now)), "Contenido técnico creado.") { loadProgress(styleId) }
    }

    fun addTechnique(styleId: Long, name: String, translation: String?, family: com.gymquest.app.domain.model.enums.MartialTechniqueFamily? = null, description: String? = null, notes: String? = null) = viewModelScope.launch {
        val now = clock.now()
        save(createTechnique(MartialTechnique(martialStyleId = styleId, name = name, translation = translation?.trim()?.ifBlank { null }, family = family, description = description?.trim()?.ifBlank { null }, notes = notes?.trim()?.ifBlank { null }, createdAt = now, updatedAt = now)), "Técnica creada.") { loadTechniques(styleId) }
    }

    fun addStance(styleId: Long, name: String, translation: String?, description: String? = null, notes: String? = null) = viewModelScope.launch {
        val now = clock.now()
        save(createStance(MartialStance(martialStyleId = styleId, name = name, translation = translation?.trim()?.ifBlank { null }, description = description?.trim()?.ifBlank { null }, notes = notes?.trim()?.ifBlank { null }, createdAt = now, updatedAt = now)), "Posición creada.") { loadStances(styleId) }
    }

    fun updateContent(value: MartialContent) = viewModelScope.launch {
        save(updateContentUseCase(value.copy(updatedAt = clock.now())), "Contenido actualizado.") { loadContentDetail(value.id) }
    }

    fun updateTechnique(value: MartialTechnique) = viewModelScope.launch {
        save(updateTechniqueUseCase(value.copy(updatedAt = clock.now())), "Técnica actualizada.") { loadTechniques(value.martialStyleId) }
    }
    fun updateStance(value: MartialStance) = viewModelScope.launch {
        save(updateStanceUseCase(value.copy(updatedAt = clock.now())), "Posición actualizada.") { loadStances(value.martialStyleId) }
    }

    fun archiveContent(contentId: Long, styleId: Long) = viewModelScope.launch {
        save(archiveContent(contentId, clock.now()), "Contenido archivado.") { loadProgress(styleId) }
    }

    fun archiveTechnique(techniqueId: Long, styleId: Long) = viewModelScope.launch {
        save(archiveTechnique(techniqueId, clock.now()), "Técnica archivada.") { loadTechniques(styleId) }
    }
    fun deleteArt(artId: Long) = viewModelScope.launch { save(deleteArtUseCase(artId), "Disciplina eliminada.") { loadArts() } }
    fun deleteStyle(artId: Long, styleId: Long) = viewModelScope.launch { save(deleteStyleUseCase(styleId), "Estilo eliminado.") { loadStyles(artId) } }
    fun deleteContent(contentId: Long, styleId: Long) = viewModelScope.launch { save(deleteContentUseCase(contentId), "Contenido eliminado.") { loadProgress(styleId) } }
    fun deleteTechnique(techniqueId: Long, styleId: Long) = viewModelScope.launch { save(deleteTechniqueUseCase(techniqueId), "Técnica eliminada.") { loadTechniques(styleId) } }
    fun deleteStance(stanceId: Long, styleId: Long) = viewModelScope.launch { save(deleteStanceUseCase(stanceId), "Posición eliminada.") { loadStances(styleId) } }
    fun replaceStepMedia(stepId: Long, media: MediaFile) = viewModelScope.launch { save(replaceStepMediaUseCase(stepId, media), "Multimedia adjunta.") { loadContentDetail(media.ownerId) } }
    fun removeStepMedia(stepId: Long, contentId: Long) = viewModelScope.launch { save(removeStepMediaUseCase(stepId), "Multimedia eliminada.") { loadContentDetail(contentId) } }
    fun replaceContentMedia(contentId: Long, media: MediaFile) = viewModelScope.launch { save(replaceContentMediaUseCase(contentId, media), "Guía multimedia adjunta.") { observeContentMedia() } }
    fun removeContentMedia(contentId: Long) = viewModelScope.launch { save(removeContentMediaUseCase(contentId), "Guía multimedia eliminada.") { observeContentMedia() } }
    fun replaceTechniqueMedia(techniqueId: Long, media: MediaFile) = viewModelScope.launch { save(replaceTechniqueMediaUseCase(techniqueId, media), "Guía multimedia adjunta.") { observeTechniqueMedia() } }
    fun removeTechniqueMedia(techniqueId: Long) = viewModelScope.launch { save(removeTechniqueMediaUseCase(techniqueId), "Guía multimedia eliminada.") { observeTechniqueMedia() } }
    fun replaceStanceMedia(stanceId: Long, media: MediaFile) = viewModelScope.launch { save(replaceStanceMediaUseCase(stanceId, media), "Guía multimedia adjunta.") { observeStanceMedia() } }
    fun removeStanceMedia(stanceId: Long) = viewModelScope.launch { save(removeStanceMediaUseCase(stanceId), "Guía multimedia eliminada.") { observeStanceMedia() } }
    fun addStep(contentId: Long, draft: MartialContentStep, insertAt: Int? = null) = viewModelScope.launch {
        val now = clock.now()
        val current = mutableState.value.contentDetail?.steps.orEmpty().sortedBy { it.orderIndex }
        val at = (insertAt ?: current.size).coerceIn(0, current.size)
        val newStep = draft.copy(id = 0, technicalContentId = contentId, orderIndex = at, createdAt = now, updatedAt = now)
        // Replacing the complete ordered list avoids duplicate order indices after an insertion.
        save(replaceStepsUseCase(contentId, (current.take(at) + newStep + current.drop(at)).mapIndexed { index, step -> step.copy(orderIndex = index) }), "Paso añadido.") { loadContentDetail(contentId) }
    }
    fun updateStep(step: MartialContentStep) = viewModelScope.launch { save(updateStepUseCase(step.copy(updatedAt = clock.now())), "Paso actualizado.") { loadContentDetail(step.technicalContentId) } }
    fun deleteStep(stepId: Long, contentId: Long) = viewModelScope.launch {
        save(deleteStepUseCase(stepId), "Paso eliminado.") {
            val remaining = mutableState.value.contentDetail?.steps.orEmpty().filterNot { it.id == stepId }
            reorderSteps(contentId, remaining)
        }
    }
    fun reorderSteps(contentId: Long, steps: List<MartialContentStep>) = viewModelScope.launch { save(replaceStepsUseCase(contentId, steps.mapIndexed { index, step -> step.copy(orderIndex = index) }), "Secuencia reordenada.") { loadContentDetail(contentId) } }
    fun duplicateStep(contentId: Long, step: MartialContentStep) = viewModelScope.launch { val now = clock.now(); val steps = mutableState.value.contentDetail?.steps.orEmpty(); val copy = step.copy(id = 0, orderIndex = step.orderIndex + 1, createdAt = now, updatedAt = now); reorderSteps(contentId, steps.filter { it.orderIndex <= step.orderIndex } + copy + steps.filter { it.orderIndex > step.orderIndex }) }
    fun updateArt(value: MartialArt) = viewModelScope.launch { save(updateArtUseCase(value.copy(updatedAt = clock.now())), "Disciplina actualizada.") { loadArts() } }
    fun updateStyle(value: MartialStyle) = viewModelScope.launch { save(updateStyleUseCase(value.copy(updatedAt = clock.now())), "Estilo actualizado.") { loadStyles(value.martialArtId) } }
    fun archiveArt(id: Long) = viewModelScope.launch { save(archiveArtUseCase(id, clock.now()), "Disciplina archivada.") { loadArts() } }
    fun archiveStyle(id: Long, artId: Long) = viewModelScope.launch { save(archiveStyleUseCase(id, clock.now()), "Estilo archivado.") { loadStyles(artId) } }

    fun registerContentPractice(styleId: Long, contentId: Long, minutes: Int, notes: String?) = viewModelScope.launch {
        val now = clock.now()
        save(registerPractice(MartialPractice(styleId, now, minutes.coerceAtLeast(1) * 60L, listOf(MartialPracticeItem(contentId = contentId, minutes = minutes.coerceAtLeast(1), progressStatusAfter = "practiced", notes = notes?.trim()?.ifBlank { null })))), "Práctica registrada.") { loadProgress(styleId) }
    }

    private suspend fun <T> load(request: suspend () -> AppResult<T>, success: MartialArtsUiState.(T) -> MartialArtsUiState) {
        mutableState.update { it.copy(isLoading = true, message = null) }
        when (val result = request()) {
            is AppResult.Success -> mutableState.update { it.success(result.value).copy(isLoading = false) }
            is AppResult.Failure -> mutableState.update { it.copy(isLoading = false, message = result.error.message) }
        }
    }

    private suspend fun save(result: AppResult<*>, success: String, after: () -> Unit) {
        mutableState.update { it.copy(isSaving = true, message = null) }
        when (result) {
            is AppResult.Success -> { mutableState.update { it.copy(isSaving = false, message = success) }; after() }
            is AppResult.Failure -> mutableState.update { it.copy(isSaving = false, message = result.error.message) }
        }
    }
}
