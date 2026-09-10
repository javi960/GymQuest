package com.gymquest.app.data.reminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymquest.app.domain.reminder.ReminderScheduler
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MartialReminderAndroidTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test fun permissionManagerReflectsThePlatformPermissionState() {
        val expected = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        assertEquals(expected, NotificationPermissionManager(context).isGranted())
    }

    @Test fun alarmSchedulerProgramsAndCancelsWithoutLeakingAnException() {
        val scheduler: ReminderScheduler = AlarmManagerReminderScheduler(context)
        scheduler.schedule(88L, Instant.now().plusSeconds(3_600), 1)
        scheduler.cancel()
    }

    @Test fun receiverIgnoresUnrelatedAndMalformedBroadcasts() {
        val receiver = MartialReminderReceiver()
        receiver.onReceive(context, Intent("unrelated.action"))
        receiver.onReceive(context, Intent(AlarmManagerReminderScheduler.ACTION_MARTIAL_REMINDER))
    }
}
