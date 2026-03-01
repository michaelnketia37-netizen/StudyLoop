package com.studyloop.todo

import com.studyloop.core.database.TodoDao
import com.studyloop.core.model.TodoEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(private val dao: TodoDao) {
    fun getAllTodos(): Flow<List<TodoEntity>> = dao.getAll()
    suspend fun addTodo(text: String, priorityColor: String = "#6C63FF") =
        dao.insert(TodoEntity(text = text, priorityColor = priorityColor))
    suspend fun toggleTodo(todo: TodoEntity) {
        val done = !todo.isDone
        dao.toggleDone(todo.id, done, if (done) System.currentTimeMillis() else null)
    }
    suspend fun deleteTodo(todo: TodoEntity) = dao.delete(todo)
}
