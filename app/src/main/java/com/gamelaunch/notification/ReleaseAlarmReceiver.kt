package com.gamelaunch.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.gamelaunch.R

class ReleaseAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val gameId = intent.getIntExtra(EXTRA_GAME_ID, -1)
        val gameName = intent.getStringExtra(EXTRA_GAME_NAME) ?: return
        val daysAhead = intent.getIntExtra(EXTRA_DAYS_AHEAD, 1)

        val message = when (daysAhead) {
            1 -> "$gameName sale mañana"
            3 -> "$gameName sale en 3 días"
            7 -> "$gameName sale en una semana"
            else -> "$gameName sale pronto"
        }

        val notification = NotificationCompat.Builder(context, NotificationChannels.RELEASES_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Próximo lanzamiento")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(gameId, notification)
    }

    companion object {
        const val EXTRA_GAME_ID = "game_id"
        const val EXTRA_GAME_NAME = "game_name"
        const val EXTRA_DAYS_AHEAD = "days_ahead"
    }
}
