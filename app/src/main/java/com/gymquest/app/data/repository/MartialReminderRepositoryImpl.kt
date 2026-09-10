package com.gymquest.app.data.repository

import com.gymquest.app.core.result.AppError
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.data.local.dao.MartialReminderDao
import com.gymquest.app.data.local.entity.MartialReminderSettingsEntity
import com.gymquest.app.data.local.entity.MartialReminderWindowEntity
import com.gymquest.app.domain.repository.MartialReminderRepository
import com.gymquest.app.domain.reminder.MartialReminderRules

class MartialReminderRepositoryImpl(private val dao: MartialReminderDao) : MartialReminderRepository {
    override suspend fun settings() = guarded { dao.getSettings() }
    override suspend fun saveSettings(settings: MartialReminderSettingsEntity) = guarded { require(settings.missionsPerDay in 0..5); require(settings.maxDailyXp in 0..100); require(settings.privacyMode in setOf("generic", "detailed")); dao.upsertSettings(settings) }
    override suspend fun windows() = guarded { dao.getReminderWindows() }
    override suspend fun addWindow(window: MartialReminderWindowEntity) = guarded {
        validateWindow(window, excludeId = null)
        dao.insertReminderWindow(window)
    }
    override suspend fun updateWindow(window: MartialReminderWindowEntity) = guarded {
        require(window.id > 0) { "La ventana a editar no existe." }
        validateWindow(window, excludeId = window.id)
        dao.upsertReminderWindow(window)
    }
    private suspend fun validateWindow(window: MartialReminderWindowEntity, excludeId: Long?) {
        val existing = dao.getReminderWindows(window.settingsId)
            .filter { it.id != excludeId }
            .map { it.startLocalTime to it.endLocalTime }
        MartialReminderRules.validateWindow(window.startLocalTime, window.endLocalTime, existing)
    }
    override suspend fun removeWindow(windowId: Long) = guarded { require(windowId > 0); dao.deleteReminderWindow(windowId); Unit }
    private suspend fun <T> guarded(block: suspend () -> T): AppResult<T> = try { AppResult.Success(block()) } catch (e: IllegalArgumentException) { AppResult.Failure(AppError.Validation(e.message ?: "Configuración no válida.")) } catch (e: Exception) { AppResult.Failure(AppError.Storage("No se pudo guardar la configuración marcial.", e)) }
}
