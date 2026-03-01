package com.studyloop.alarm

import android.content.Context
import com.studyloop.core.database.AlarmDao
import com.studyloop.core.model.AlarmEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val dao: AlarmDao,
    @ApplicationContext private val context: Context
) {
    fun getAllAlarms(): Flow<List<AlarmEntity>> = dao.getAll()

    suspend fun addAlarm(alarm: AlarmEntity) {
        dao.insert(alarm)
        scheduleAlarm(context, alarm)
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        dao.delete(alarm)
        cancelAlarm(context, alarm.id)
    }

    suspend fun toggleAlarm(alarm: AlarmEntity) {
        val updated = alarm.copy(isActive = !alarm.isActive)
        dao.update(updated)
        if (updated.isActive) scheduleAlarm(context, updated)
        else cancelAlarm(context, updated.id)
    }
}
