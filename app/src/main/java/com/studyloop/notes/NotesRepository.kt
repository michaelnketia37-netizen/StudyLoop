package com.studyloop.notes

import com.studyloop.core.database.NoteDao
import com.studyloop.core.model.NoteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotesRepository @Inject constructor(private val dao: NoteDao) {
    fun getAllNotes(): Flow<List<NoteEntity>> = dao.getAll()
    suspend fun addNote(title: String, content: String, colorHex: String) =
        dao.insert(NoteEntity(title = title, content = content, colorHex = colorHex))
    suspend fun updateNote(note: NoteEntity) = dao.update(note.copy(lastEditedAt = System.currentTimeMillis()))
    suspend fun deleteNote(note: NoteEntity) = dao.delete(note)
}
