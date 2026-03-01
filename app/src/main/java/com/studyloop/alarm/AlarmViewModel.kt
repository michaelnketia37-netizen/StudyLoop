package com.studyloop.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyloop.core.model.AlarmEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val repo: AlarmRepository
) : ViewModel() {

    val alarms = repo.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAlarm(hour: Int, minute: Int, label: String, repeatDays: String) {
        viewModelScope.launch {
            repo.addAlarm(AlarmEntity(
                hour = hour,
                minute = minute,
                label = label,
                repeatDays = repeatDays
            ))
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) = viewModelScope.launch { repo.deleteAlarm(alarm) }

    fun toggleAlarm(alarm: AlarmEntity) = viewModelScope.launch { repo.toggleAlarm(alarm) }
}
