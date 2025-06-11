package de.syntax_institut.taskmanager.ui.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.syntax_institut.taskmanager.data.database.TaskDatabase
import de.syntax_institut.taskmanager.data.model.Task
import de.syntax_institut.taskmanager.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val SHOW_COMPLETED_KEY = booleanPreferencesKey("show_completed")

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore
    private val dao = TaskDatabase.getDatabase(application.applicationContext).taskDao()

    private val showCompletedFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[SHOW_COMPLETED_KEY] ?: false
        }

    val showCompletedStateFlow = showCompletedFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    // StateFlow für alle Tasks
    val allTasks: StateFlow<List<Task>> = dao.getAllTasks().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    // StateFlow für erledigte Tasks
    val completedTasks: StateFlow<List<Task>> = dao.getTasksByCompletionStatus(true).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    // Kombinierter StateFlow basierend auf showCompleted-Status
    val displayedTasks: StateFlow<List<Task>> = combine(
        showCompletedStateFlow,
        allTasks,
        completedTasks
    ) { showCompleted, all, completed ->
        if (showCompleted) completed else all
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    fun toggleShowCompleted() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                val currentValue = preferences[SHOW_COMPLETED_KEY] ?: false
                preferences[SHOW_COMPLETED_KEY] = !currentValue
            }
        }
    }

    // Task-Management Funktionen
    fun insertTask(title: String) {
        viewModelScope.launch {
            dao.insert(Task(title = title))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.delete(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            dao.update(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            dao.update(task.copy(isCompleted = !task.isCompleted))
        }
    }
}