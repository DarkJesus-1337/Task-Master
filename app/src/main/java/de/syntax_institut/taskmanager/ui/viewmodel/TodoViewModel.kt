package de.syntax_institut.taskmanager.ui.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.syntax_institut.taskmanager.data.database.TaskDatabase
import de.syntax_institut.taskmanager.data.model.Task
import de.syntax_institut.taskmanager.data.model.TaskPriority
import de.syntax_institut.taskmanager.data.model.User
import de.syntax_institut.taskmanager.data.model.UserWithTasks
import de.syntax_institut.taskmanager.dataStore
import de.syntax_institut.taskmanager.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

private val SHOW_ONLY_PENDING_KEY = booleanPreferencesKey("show_only_pending")
private val FILTER_CATEGORY_KEY = stringPreferencesKey("filter_category")
private val FILTER_USER_ID_KEY = longPreferencesKey("filter_user_id")
private val CURRENT_USER_ID_KEY = longPreferencesKey("current_user_id")


class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore
    private val database = TaskDatabase.getDatabase(application.applicationContext)
    private val taskDao = database.taskDao()
    private val userDao = database.userDao()

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

    init {
        viewModelScope.launch {
            val existingUser = userDao.getUserSync(0L)
            if (existingUser == null) {
                val defaultUser = User(id = 0, username = "Standard Benutzer")
                userDao.insert(defaultUser)
                dataStore.edit { preferences ->
                    preferences[CURRENT_USER_ID_KEY] = 0L
                }
            }
        }
    }

    val currentUser: StateFlow<User?> = currentUserIdFlow
        .map { userId ->
            userDao.getUserSync(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = null
        )

    val allUsersWithTasks: StateFlow<List<UserWithTasks>> = userDao.getAllUsersWithTasks().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    val allUsers: StateFlow<List<User>> = allUsersWithTasks
        .map { usersWithTasks -> usersWithTasks.map { it.user } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    private val showOnlyPendingFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[SHOW_ONLY_PENDING_KEY] ?: false
        }

    private val filterCategoryFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[FILTER_CATEGORY_KEY] ?: ""
        }

    private val filterUserIdFlow: Flow<Long> = dataStore.data
        .map { preferences ->
            preferences[FILTER_USER_ID_KEY] ?: -1L
        }

    val showOnlyPendingStateFlow = showOnlyPendingFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = false
        )

    val filterCategoryStateFlow = filterCategoryFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = ""
        )

    val filterUserIdStateFlow = filterUserIdFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = -1L
        )

    val allTasks: StateFlow<List<Task>> = allUsersWithTasks
        .map { usersWithTasks ->
            usersWithTasks.flatMap { it.tasks }
                .sortedWith(
                    compareByDescending<Task> { it.priority.ordinal }
                        .thenBy { it.deadlineTimestamp ?: Long.MAX_VALUE }
                        .thenBy { it.id }
                )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    val pendingTasks: StateFlow<List<Task>> = allTasks
        .map { tasks -> tasks.filter { !it.isCompleted } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    val overdueTasks: StateFlow<List<Task>> = allTasks
        .map { tasks ->
            tasks.filter { task ->
                task.deadlineTimestamp?.let {
                    DateUtils.isOverdue(it) && !task.isCompleted
                } ?: false
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    val todayTasks: StateFlow<List<Task>> = allTasks
        .map { tasks ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay = calendar.timeInMillis

            tasks.filter { task ->
                task.deadlineTimestamp?.let { deadline ->
                    deadline in startOfDay..endOfDay && !task.isCompleted
                } ?: false
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<String>> = allTasks
        .map { tasks ->
            tasks.map { it.category }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    val displayedTasks: StateFlow<List<Task>> = combine(
        showOnlyPendingStateFlow,
        filterCategoryStateFlow,
        filterUserIdStateFlow,
        allTasks,
        pendingTasks
    ) { showOnlyPending: Boolean, filterCategory: String, filterUserId: Long, all: List<Task>, pending: List<Task> ->
        var tasks = if (showOnlyPending) {
            pending.filter { !it.isCompleted }
        } else {
            all
        }

        if (filterCategory.isNotEmpty()) {
            tasks = tasks.filter { it.category == filterCategory }
        }

        if (filterUserId != -1L) {
            tasks = tasks.filter { it.userId == filterUserId }
        }

        tasks
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    val taskStatistics = allTasks.map { tasks: List<Task> ->
        TaskStatistics(
            total = tasks.size,
            completed = tasks.count { it.isCompleted },
            pending = tasks.count { !it.isCompleted },
            overdue = tasks.count { task ->
                task.deadlineTimestamp?.let {
                    DateUtils.isOverdue(it) && !task.isCompleted
                } ?: false
            },
            dueToday = tasks.count { task ->
                task.deadlineTimestamp?.let { deadline ->
                    val daysUntil = DateUtils.getDaysUntilDeadline(deadline)
                    daysUntil == 0L && !task.isCompleted
                } ?: false
            },
            highPriority = tasks.count {
                it.priority == TaskPriority.HIGH || it.priority == TaskPriority.URGENT
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = TaskStatistics()
    )

    fun toggleShowOnlyPending() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                val currentValue = preferences[SHOW_ONLY_PENDING_KEY] ?: false
                preferences[SHOW_ONLY_PENDING_KEY] = !currentValue
            }
        }
    }

    fun setFilterCategory(category: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[FILTER_CATEGORY_KEY] = category
            }
        }
    }

    fun setFilterUserId(userId: Long) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[FILTER_USER_ID_KEY] = if (userId == -1L) -1L else userId
            }
        }
    }

    fun clearFilters() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[FILTER_CATEGORY_KEY] = ""
                preferences[FILTER_USER_ID_KEY] = -1L
                preferences[SHOW_ONLY_PENDING_KEY] = false
            }
        }
    }

    fun insertTask(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.delete(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            taskDao.update(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(
                isCompleted = !task.isCompleted,
                completedAt = if (!task.isCompleted) System.currentTimeMillis() else null
            )
            taskDao.update(updatedTask)
        }
    }
}

data class TaskStatistics(
    val total: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0,
    val overdue: Int = 0,
    val dueToday: Int = 0,
    val highPriority: Int = 0
) {
    val completionRate: Float
        get() = if (total > 0) completed.toFloat() / total.toFloat() else 0f
}