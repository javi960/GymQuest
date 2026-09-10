package com.gymquest.app.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.gymquest.app.domain.reminder.ReminderScheduler
import java.time.Instant

class AlarmManagerReminderScheduler(context: Context) : ReminderScheduler {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)
    override fun schedule(missionId: Long, earliestTriggerAt: Instant, windowLengthMillis: Long) {
        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, earliestTriggerAt.toEpochMilli(), windowLengthMillis.coerceAtLeast(MIN_WINDOW_MILLIS), pendingIntent(missionId))
    }
    override fun cancel() { alarmManager.cancel(pendingIntent(0)) }
    private fun pendingIntent(missionId: Long): PendingIntent = PendingIntent.getBroadcast(appContext, REQUEST_CODE, Intent(appContext, MartialReminderReceiver::class.java).setAction(ACTION_MARTIAL_REMINDER).putExtra(EXTRA_MISSION_ID, missionId), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    companion object { const val ACTION_MARTIAL_REMINDER = "com.gymquest.app.ACTION_MARTIAL_REMINDER"; const val EXTRA_MISSION_ID = "mission_id"; private const val REQUEST_CODE = 4107; private const val MIN_WINDOW_MILLIS = 10 * 60 * 1000L }
}
