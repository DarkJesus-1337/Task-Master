package de.syntax_institut.taskmanager.ui.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.syntax_institut.taskmanager.data.database.TaskDatabase
import de.syntax_institut.taskmanager.data.model.User
import de.syntax_institut.taskmanager.data.model.UserWithTasks
import de.syntax_institut.taskmanager.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val CURRENT_USER_ID_KEY = longPreferencesKey("current_user_id")

class UserManagementViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore
    private val database = TaskDatabase.getDatabase(application.applicationContext)
    private val userDao = database.userDao()
    private val taskDao = database.taskDao()

    private val currentUserIdFlow: Flow<Long> = dataStore.data
        .map { preferences ->
            preferences[CURRENT_USER_ID_KEY] ?: 0L
        }

    val currentUserIdStateFlow = currentUserIdFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = 0L
        )

    val allUsersWithTasks: StateFlow<List<UserWithTasks>> = userDao.getAllUsersWithTasks().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    val currentUser: StateFlow<User?> = currentUserIdStateFlow
        .map { userId ->
            allUsersWithTasks.value.find { it.user.id == userId }?.user
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    fun createUser(username: String) {
        viewModelScope.launch {
            val existingUsers = allUsersWithTasks.value
            val newId = if (existingUsers.isEmpty()) {
                1L
            } else {
                existingUsers.maxOf { it.user.id } + 1
            }
            
            val newUser = User(
                id = newId,
                username = username
            )
            
            userDao.insert(newUser)
            
            switchToUser(newId)
        }
    }

    fun switchToUser(userId: Long) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[CURRENT_USER_ID_KEY] = userId
            }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            val userWithTasks = allUsersWithTasks.value.find { it.user.id == user.id }
            userWithTasks?.tasks?.forEach { task ->
                taskDao.delete(task)
            }
            
            userDao.delete(user)
            
            if (currentUserIdStateFlow.value == user.id) {
                val remainingUsers = allUsersWithTasks.value.filter { it.user.id != user.id }
                if (remainingUsers.isNotEmpty()) {
                    switchToUser(remainingUsers.first().user.id)
                } else {
                    createUser("Neuer Benutzer")
                }
            }
        }
    }
}