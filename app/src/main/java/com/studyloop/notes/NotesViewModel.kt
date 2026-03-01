package com.studyloop.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyloop.core.model.NoteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(private val repo: NotesRepository) : ViewModel() {

    val notes = repo.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNote(title: String, content: String, colorHex: String = "#FFF3CD") =
        viewModelScope.launch { repo.addNote(title, content, colorHex) }

    fun deleteNote(note: NoteEntity) = viewModelScope.launch { repo.deleteNote(note) }
    fun updateNote(note: NoteEntity) = viewModelScope.launch { repo.updateNote(note) }
}
