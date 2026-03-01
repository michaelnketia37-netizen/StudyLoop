package com.studyloop.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyloop.core.model.TodoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(private val repo: TodoRepository) : ViewModel() {

    val todos = repo.getAllTodos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTodo(text: String) = viewModelScope.launch { repo.addTodo(text) }
    fun toggleTodo(todo: TodoEntity) = viewModelScope.launch { repo.toggleTodo(todo) }
    fun deleteTodo(todo: TodoEntity) = viewModelScope.launch { repo.deleteTodo(todo) }
}
