package de.syntax_institut.taskmanager.ui.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import de.syntax_institut.taskmanager.dataStore

private val SHOW_COMPLETED_KEY = booleanPreferencesKey("show_completed")

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore

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

    fun toggleShowCompleted() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                val currentValue = preferences[SHOW_COMPLETED_KEY] ?: false
                preferences[SHOW_COMPLETED_KEY] = !currentValue
            }
        }
    }
}