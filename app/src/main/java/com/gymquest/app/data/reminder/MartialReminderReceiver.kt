package com.gymquest.app.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import com.gymquest.app.data.local.GymQuestDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MartialReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmManagerReminderScheduler.ACTION_MARTIAL_REMINDER) return
        val missionId = intent.getLongExtra(AlarmManagerReminderScheduler.EXTRA_MISSION_ID, 0)
        if (missionId <= 0 || !NotificationPermissionManager(context).isGranted()) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = GymQuestDatabase.create(context).martialReminderDao()
                val settings = dao.getSettings()
                val mission = dao.getSecondaryMission(missionId)
                if (settings?.enabled == true && mission?.status == "scheduled") {
                    val notificationBuilder = MartialNotificationBuilder(context)
                    notificationBuilder.createChannel()
                    context.getSystemService(NotificationManager::class.java).notify(MartialNotificationBuilder.NOTIFICATION_ID, notificationBuilder.build(null, settings.privacyMode == "detailed"))
                }
            } finally { pendingResult.finish() }
        }
    }
}
