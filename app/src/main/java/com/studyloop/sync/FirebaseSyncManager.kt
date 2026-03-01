package com.studyloop.sync

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.studyloop.core.model.ReminderEntity
import com.studyloop.core.model.ReviewEntity

object FirebaseSyncManager {
    private val db   = Firebase.firestore
    private val auth = Firebase.auth

    private val userId get() = auth.currentUser?.uid

    fun syncReminder(reminder: ReminderEntity) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .collection("reminders").document(reminder.id)
            .set(mapOf(
                "id" to reminder.id,
                "title" to reminder.title,
                "content" to reminder.content,
                "savedAt" to reminder.savedAt,
                "completedReviews" to reminder.completedReviews,
                "colorHex" to reminder.colorHex,
                "emoji" to reminder.emoji
            ))
    }

    fun syncReview(review: ReviewEntity) {
        val uid = userId ?: return
        db.collection("users").document(uid)
            .collection("reviews").document(review.id)
            .set(mapOf(
                "id" to review.id,
                "reminderId" to review.reminderId,
                "reviewNumber" to review.reviewNumber,
                "scheduledAt" to review.scheduledAt,
                "completedAt" to review.completedAt,
                "retentionAtReview" to review.retentionAtReview
            ))
    }
}
