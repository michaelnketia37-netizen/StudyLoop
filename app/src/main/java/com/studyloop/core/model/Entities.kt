package com.studyloop.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// ── Reminder ──────────────────────────────────────────────────────────────
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val content: String,
    val screenshotUri: String? = null,
    val colorHex: String = "#6C63FF",
    val emoji: String = "📌",
    val savedAt: Long = System.currentTimeMillis(),
    val totalReviews: Int = 7,
    val completedReviews: Int = 0,
    val isConsolidated: Boolean = false
)

// ── Review ────────────────────────────────────────────────────────────────
@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val reminderId: String,
    val reviewNumber: Int,           // 0=Day1, 1=Day3, ... 6=Day120
    val scheduledAt: Long,
    val completedAt: Long? = null,
    val retentionAtReview: Double? = null
)

// ── Note ──────────────────────────────────────────────────────────────────
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val colorHex: String = "#FFF3CD",
    val savedAt: Long = System.currentTimeMillis(),
    val lastEditedAt: Long = System.currentTimeMillis(),
    val isDueForReview: Boolean = false
)

// ── Todo ──────────────────────────────────────────────────────────────────
@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isDone: Boolean = false,
    val priorityColor: String = "#6C63FF",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

// ── Alarm ─────────────────────────────────────────────────────────────────
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey val id: Int = System.currentTimeMillis().toInt(),
    val hour: Int,
    val minute: Int,
    val label: String = "Alarm",
    val isActive: Boolean = true,
    val repeatDays: String = "",     // comma-separated: "MO,TU,WE"
    val createdAt: Long = System.currentTimeMillis()
)
