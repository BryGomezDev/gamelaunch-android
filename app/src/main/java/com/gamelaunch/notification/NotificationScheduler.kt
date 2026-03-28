package com.gamelaunch.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.gamelaunch.domain.model.Game
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** daysAhead: 1, 3, or 7 */
    fun scheduleReleaseNotification(game: Game, daysAhead: Int) {
        val triggerAt = game.releaseDate
            .minusDays(daysAhead.toLong())
            .atTime(10, 0)
            .atZone(ZoneId.systemDefault())
            .toEpochSecond() * 1000L

        if (triggerAt <= System.currentTimeMillis()) return

        val intent = Intent(context, ReleaseAlarmReceiver::class.java).apply {
            putExtra(ReleaseAlarmReceiver.EXTRA_GAME_ID, game.id)
            putExtra(ReleaseAlarmReceiver.EXTRA_GAME_NAME, game.name)
            putExtra(ReleaseAlarmReceiver.EXTRA_DAYS_AHEAD, daysAhead)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            alarmRequestCode(game.id, daysAhead),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun cancelReleaseNotification(gameId: Int, daysAhead: Int) {
        val intent = Intent(context, ReleaseAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            alarmRequestCode(gameId, daysAhead),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pi)
    }

    private fun alarmRequestCode(gameId: Int, daysAhead: Int) = gameId * 10 + daysAhead
}
