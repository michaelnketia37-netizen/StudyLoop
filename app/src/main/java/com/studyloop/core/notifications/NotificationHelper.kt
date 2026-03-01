package com.studyloop.core.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.studyloop.App
import com.studyloop.MainActivity
import com.studyloop.R

object NotificationHelper {

    fun showAlarmNotification(context: Context, alarmId: Int, label: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("TAB", "alarm")
        }
        val pi = PendingIntent.getActivity(
            context, alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, App.CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle("⏰ StudyLoop Alarm")
            .setContentText(label)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify(alarmId, notification)
    }

    fun showReviewNotification(
        context: Context,
        reminderId: String,
        reviewNumber: Int,
        title: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("TAB", "reminder")
            putExtra("REMINDER_ID", reminderId)
        }
        val pi = PendingIntent.getActivity(
            context, reminderId.hashCode() + reviewNumber, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dayLabel = SpacedRepetitionReviewDays[reviewNumber] ?: reviewNumber
        val notification = NotificationCompat.Builder(context, App.CHANNEL_REVIEW)
            .setSmallIcon(R.drawable.ic_brain)
            .setContentTitle("🧠 Time to Review! (Day $dayLabel)")
            .setContentText("Review: $title")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify(reminderId.hashCode() + reviewNumber, notification)
    }

    private val SpacedRepetitionReviewDays = mapOf(
        0 to 1, 1 to 3, 2 to 7, 3 to 14, 4 to 30, 5 to 60, 6 to 120
    )
}
