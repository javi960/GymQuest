package com.gymquest.app.data.reminder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

class MartialNotificationBuilder(private val context: Context) {
    fun createChannel() {
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL_ID, "Misiones de práctica", NotificationManager.IMPORTANCE_DEFAULT).apply { description = "Recordatorios locales de práctica marcial" })
    }
    fun build(contentName: String?, detailed: Boolean): Notification = Notification.Builder(context, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_popup_reminder)
        .setContentTitle(if (detailed && !contentName.isNullOrBlank()) "Practica $contentName" else "Misión secundaria disponible")
        .setContentText(if (detailed && !contentName.isNullOrBlank()) "Repasa una técnica o secuencia pendiente" else "Practica técnica durante unos minutos")
        .setAutoCancel(true)
        .build()
    companion object { const val CHANNEL_ID = "martial_practice_reminders"; const val NOTIFICATION_ID = 4107 }
}
