package com.gymquest.app.domain.repository

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.data.local.entity.MartialReminderSettingsEntity
import com.gymquest.app.data.local.entity.MartialReminderWindowEntity

interface MartialReminderRepository {
    suspend fun settings(): AppResult<MartialReminderSettingsEntity?>
    suspend fun saveSettings(settings: MartialReminderSettingsEntity): AppResult<Unit>
    suspend fun windows(): AppResult<List<MartialReminderWindowEntity>>
    suspend fun addWindow(window: MartialReminderWindowEntity): AppResult<Long>
    suspend fun updateWindow(window: MartialReminderWindowEntity): AppResult<Unit>
    suspend fun removeWindow(windowId: Long): AppResult<Unit>
}
