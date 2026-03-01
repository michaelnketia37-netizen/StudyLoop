package com.studyloop.core.database

import androidx.room.*
import com.studyloop.core.model.*
import kotlinx.coroutines.flow.Flow

// ── Reminder DAO ──────────────────────────────────────────────────────────
@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity)

    @Delete
    suspend fun delete(reminder: ReminderEntity)

    @Query("SELECT * FROM reminders ORDER BY savedAt DESC")
    fun getAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: String): ReminderEntity?

    @Query("UPDATE reminders SET completedReviews = completedReviews + 1 WHERE id = :id")
    suspend fun incrementReviews(id: String)
}

// ── Review DAO ────────────────────────────────────────────────────────────
@Dao
interface ReviewDao {
    @Insert
    suspend fun insertAll(reviews: List<ReviewEntity>)

    @Query("SELECT * FROM reviews WHERE reminderId = :id ORDER BY reviewNumber")
    fun getReviewsForReminder(id: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE reminderId = :id ORDER BY reviewNumber")
    suspend fun getReviewsForReminderSync(id: String): List<ReviewEntity>

    @Query("""
        UPDATE reviews 
        SET completedAt = :time, retentionAtReview = :ret 
        WHERE id = :reviewId
    """)
    suspend fun markComplete(reviewId: String, time: Long, ret: Double)

    @Query("DELETE FROM reviews WHERE reminderId = :reminderId")
    suspend fun deleteForReminder(reminderId: String)
}

// ── Note DAO ──────────────────────────────────────────────────────────────
@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Query("SELECT * FROM notes ORDER BY lastEditedAt DESC")
    fun getAll(): Flow<List<NoteEntity>>
}

// ── Todo DAO ──────────────────────────────────────────────────────────────
@Dao
interface TodoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: TodoEntity)

    @Delete
    suspend fun delete(todo: TodoEntity)

    @Update
    suspend fun update(todo: TodoEntity)

    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAll(): Flow<List<TodoEntity>>

    @Query("UPDATE todos SET isDone = :done, completedAt = :time WHERE id = :id")
    suspend fun toggleDone(id: String, done: Boolean, time: Long?)
}

// ── Alarm DAO ─────────────────────────────────────────────────────────────
@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity)

    @Delete
    suspend fun delete(alarm: AlarmEntity)

    @Update
    suspend fun update(alarm: AlarmEntity)

    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAll(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE isActive = 1")
    suspend fun getActiveAlarms(): List<AlarmEntity>

    @Query("UPDATE alarms SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Int, active: Boolean)
}
