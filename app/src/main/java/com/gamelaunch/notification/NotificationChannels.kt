package com.gamelaunch.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannels {
    const val RELEASES_CHANNEL_ID = "game_releases"

    fun create(context: Context) {
        val channel = NotificationChannel(
            RELEASES_CHANNEL_ID,
            "Lanzamientos de juegos",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notificaciones de próximos lanzamientos de juegos"
        }
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }
}
