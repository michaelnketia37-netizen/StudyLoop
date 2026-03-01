package com.studyloop.reminder

import android.content.Context
import com.studyloop.alarm.scheduleAllReviews
import com.studyloop.core.database.ReminderDao
import com.studyloop.core.database.ReviewDao
import com.studyloop.core.model.ReminderEntity
import com.studyloop.core.model.ReviewEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao,
    private val reviewDao: ReviewDao,
    @ApplicationContext private val context: Context
) {
    fun getAllReminders(): Flow<List<ReminderEntity>> = reminderDao.getAll()

    fun getReviewsFor(reminderId: String): Flow<List<ReviewEntity>> =
        reviewDao.getReviewsForReminder(reminderId)

    suspend fun addReminder(content: String, emoji: String = "📌", colorHex: String = "#6C63FF") {
        val title = content.split(" ").take(4).joinToString(" ") +
            if (content.split(" ").size > 4) "…" else ""

        val reminder = ReminderEntity(
            content = content,
            title = title,
            emoji = emoji,
            colorHex = colorHex
        )
        reminderDao.insert(reminder)

        // Schedule 7 review notifications
        val schedule = SpacedRepetitionEngine.calculateReviewSchedule(reminder.savedAt)
        val reviews = schedule.mapIndexed { i, time ->
            ReviewEntity(
                id = UUID.randomUUID().toString(),
                reminderId = reminder.id,
                reviewNumber = i,
                scheduledAt = time
            )
        }
        reviewDao.insertAll(reviews)
        scheduleAllReviews(context, reminder.id, title, reminder.savedAt)
    }

    suspend fun markReviewed(reminder: ReminderEntity, reviews: List<ReviewEntity>) {
        val nextReview = reviews.firstOrNull { it.completedAt == null } ?: return
        val retention = SpacedRepetitionEngine.retentionBoostAfterReview(nextReview.reviewNumber)
        reviewDao.markComplete(nextReview.id, System.currentTimeMillis(), retention)
        reminderDao.incrementReviews(reminder.id)
    }

    suspend fun deleteReminder(reminder: ReminderEntity) {
        reviewDao.deleteForReminder(reminder.id)
        reminderDao.delete(reminder)
    }
}
