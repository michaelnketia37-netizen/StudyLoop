package com.studyloop.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReviewBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId  = intent.getStringExtra("REMINDER_ID") ?: return
        val reviewNum   = intent.getIntExtra("REVIEW_NUMBER", 0)
        val title       = intent.getStringExtra("TITLE") ?: "Reminder"

        NotificationHelper.showReviewNotification(context, reminderId, reviewNum, title)
    }
}
