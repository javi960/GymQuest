package com.gymquest.app.data.reminder

import com.gymquest.app.core.time.ClockProvider
import com.gymquest.app.data.local.GymQuestDatabase
import com.gymquest.app.data.local.entity.MartialSecondaryMissionEntity
import com.gymquest.app.domain.model.MartialReminderWindow
import com.gymquest.app.domain.reminder.MartialReminderPlanner
import com.gymquest.app.domain.reminder.MartialReminderRules
import com.gymquest.app.domain.reminder.ReminderScheduler
import java.time.ZoneId
import java.time.ZonedDateTime

/** Single entry point for persisted mission creation and inexact alarm re-planning. */
class MartialReminderCoordinator(
    private val database: GymQuestDatabase,
    private val scheduler: ReminderScheduler,
    private val permission: NotificationPermissionManager,
    private val clock: ClockProvider,
) {
    suspend fun replan() {
        val dao = database.martialReminderDao()
        val settings = dao.getSettings()
        if (settings?.enabled != true || !permission.isGranted()) { scheduler.cancel(); return }
        val now = ZonedDateTime.ofInstant(clock.now(), ZoneId.systemDefault())
        val start = now.toLocalDate().atStartOfDay(now.zone).toInstant()
        val end = now.toLocalDate().plusDays(1).atStartOfDay(now.zone).toInstant()
        if (!MartialReminderRules.canSchedule(dao.countMissions(start, end), settings.missionsPerDay)) { scheduler.cancel(); return }
        val content = dao.nextEligibleContent() ?: run { scheduler.cancel(); return }
        val windows = dao.getReminderWindows().map {
            MartialReminderWindow(java.time.LocalTime.parse(it.startLocalTime), java.time.LocalTime.parse(it.endLocalTime), it.enabled, it.sortOrder)
        }
        val plan = MartialReminderPlanner.nextPlan(now, windows) ?: run { scheduler.cancel(); return }
        val missionId = dao.insertSecondaryMission(MartialSecondaryMissionEntity(technicalContentId = content.id, scheduledFor = plan.triggerAt.toInstant(), createdAt = clock.now(), updatedAt = clock.now()))
        scheduler.schedule(missionId, plan.triggerAt.toInstant(), plan.window.toMillis())
    }

    suspend fun complete(missionId: Long): Long {
        val now = ZonedDateTime.ofInstant(clock.now(), ZoneId.systemDefault())
        val start = now.toLocalDate().atStartOfDay(now.zone).toInstant()
        val end = now.toLocalDate().plusDays(1).atStartOfDay(now.zone).toInstant()
        val max = database.martialReminderDao().getSettings()?.maxDailyXp ?: 0
        val xp = database.martialReminderDao().completeMissionWithDailyCap(missionId, start, end, max, clock.now())
        replan()
        return xp
    }
}
