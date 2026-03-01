package com.studyloop.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyloop.core.model.ReminderEntity
import com.studyloop.core.model.ReviewEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val repo: ReminderRepository
) : ViewModel() {

    val reminders = repo.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getReviews(reminderId: String): Flow<List<ReviewEntity>> =
        repo.getReviewsFor(reminderId)

    fun addReminder(content: String, emoji: String = "📌", colorHex: String = "#6C63FF") {
        viewModelScope.launch { repo.addReminder(content, emoji, colorHex) }
    }

    fun markReviewed(reminder: ReminderEntity, reviews: List<ReviewEntity>) {
        viewModelScope.launch { repo.markReviewed(reminder, reviews) }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch { repo.deleteReminder(reminder) }
    }

    fun retentionPercent(reminder: ReminderEntity, reviews: List<ReviewEntity>): Int =
        SpacedRepetitionEngine.currentRetentionPercent(reminder.savedAt, reviews)

    fun nextReviewCountdown(reminder: ReminderEntity): String =
        SpacedRepetitionEngine.nextReviewCountdown(reminder.completedReviews, reminder.savedAt)

    fun generateCurvePoints(reminder: ReminderEntity, reviews: List<ReviewEntity>) =
        SpacedRepetitionEngine.generateCurvePoints(reminder.savedAt, reviews)
}
