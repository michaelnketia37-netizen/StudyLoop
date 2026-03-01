package com.studyloop

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()

        // ── AdMob initialisation ──────────────────────────────
        MobileAds.initialize(this)

        // ── Notification Channels ─────────────────────────────
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)

            // Alarms channel
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ALARM,
                    "Alarms",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "StudyLoop alarm notifications"
                    enableVibration(true)
                }
            )

            // Review reminders channel
            nm.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_REVIEW,
                    "Review Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Spaced repetition review reminders"
                }
            )
        }
    }

    companion object {
        const val CHANNEL_ALARM  = "studyloop_alarm"
        const val CHANNEL_REVIEW = "studyloop_review"
    }
}
