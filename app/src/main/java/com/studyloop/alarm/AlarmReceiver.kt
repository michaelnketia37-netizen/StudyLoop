package com.studyloop.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.studyloop.core.database.AppDatabase
import com.studyloop.core.model.AlarmEntity
import com.studyloop.core.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ── Alarm fires ───────────────────────────────────────────────────────────
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", 0)
        val label   = intent.getStringExtra("LABEL") ?: "Alarm"
        NotificationHelper.showAlarmNotification(context, alarmId, label)
    }
}

// ── Reschedule after reboot ───────────────────────────────────────────────
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getInstance(context)
            val activeAlarms = db.alarmDao().getActiveAlarms()
            activeAlarms.forEach { alarm ->
                scheduleAlarm(context, alarm)
            }
        }
    }
}

// ── Schedule helpers ──────────────────────────────────────────────────────
fun scheduleAlarm(context: Context, alarm: AlarmEntity) {
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("ALARM_ID", alarm.id)
        putExtra("LABEL", alarm.label)
    }
    val pi = PendingIntent.getBroadcast(
        context, alarm.id, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerAt = nextAlarmTime(alarm.hour, alarm.minute)

    val am = context.getSystemService(AlarmManager::class.java)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!am.canScheduleExactAlarms()) {
            // Alarm will fire approximately — prompt user to grant permission
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            return
        }
    }
    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
}

fun cancelAlarm(context: Context, alarmId: Int) {
    val intent = Intent(context, AlarmReceiver::class.java)
    val pi = PendingIntent.getBroadcast(
        context, alarmId, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val am = context.getSystemService(AlarmManager::class.java)
    am.cancel(pi)
}

private fun nextAlarmTime(hour: Int, minute: Int): Long {
    val cal = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, hour)
        set(java.util.Calendar.MINUTE, minute)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
            add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
    }
    return cal.timeInMillis
}

// ── Schedule all 7 review notifications ──────────────────────────────────
fun scheduleAllReviews(context: Context, reminderId: String, title: String, savedAt: Long) {
    val times = listOf(1L, 3, 7, 14, 30, 60, 120).map { days ->
        savedAt + days * 24 * 60 * 60 * 1000L
    }
    times.forEachIndexed { index, triggerTime ->
        if (triggerTime > System.currentTimeMillis()) {
            val intent = Intent(context, com.studyloop.core.notifications.ReviewBroadcastReceiver::class.java).apply {
                putExtra("REMINDER_ID", reminderId)
                putExtra("REVIEW_NUMBER", index)
                putExtra("TITLE", title)
            }
            val pi = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode() + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val am = context.getSystemService(AlarmManager::class.java)
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi)
        }
    }
}
