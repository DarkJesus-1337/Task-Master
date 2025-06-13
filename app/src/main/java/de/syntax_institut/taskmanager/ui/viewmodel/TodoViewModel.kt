package de.syntax_institut.taskmanager.ui.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.syntax_institut.taskmanager.data.database.TaskDatabase
import de.syntax_institut.taskmanager.data.model.Task
import de.syntax_institut.taskmanager.data.model.TaskPriority
import de.syntax_institut.taskmanager.data.model.User
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
private val FILTER_PRIORITY_KEY = stringPreferencesKey("filter_priority")

enum class TaskFilter {
    ALL, COMPLETED, PENDING, OVERDUE, TODAY, HIGH_PRIORITY
}

class TodoViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore
    private val database = TaskDatabase.getDatabase(application.applicationContext)
    private val taskDao = database.taskDao()
    private val userDao = database.userDao()

    init {
        viewModelScope.launch {
            val existingUser = userDao.getUserSync()
            if (existingUser == null) {
                insertUser(User(
                    id = 0,
                    username = "Bob"
                ))
            }
        }
    }

    val currentUser: StateFlow<User?> = userDao.getUser().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null
    )

    private fun insertUser(user: User) {
        viewModelScope.launch {
            userDao.insert(user)
        }
    }

    private val showOnlyPendingFlow: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[SHOW_ONLY_PENDING_KEY] ?: false
        }

    private val filterCategoryFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[FILTER_CATEGORY_KEY] ?: ""
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

    val allTasks: StateFlow<List<Task>> = taskDao.getAllTasks().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    val completedTasks: StateFlow<List<Task>> = taskDao.getTasksByCompletionStatus(true).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    val pendingTasks: StateFlow<List<Task>> = taskDao.getTasksByCompletionStatus(false).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    val overdueTasks: StateFlow<List<Task>> = taskDao.getOverdueTasks().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    val todayTasks: StateFlow<List<Task>> = run {
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

        taskDao.getTasksDueToday(startOfDay, endOfDay).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )
    }

    val categories: StateFlow<List<String>> = taskDao.getAllCategories().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    val displayedTasks: StateFlow<List<Task>> = combine(
        showOnlyPendingStateFlow,
        filterCategoryStateFlow,
        allTasks,
        pendingTasks
    ) { showOnlyPending: Boolean, filterCategory: String, all: List<Task>, pending: List<Task> ->
        var tasks = if (showOnlyPending) {
            pending.filter { !it.isCompleted }
        } else {
            all
        }

        if (filterCategory.isNotEmpty()) {
            tasks = tasks.filter { it.category == filterCategory }
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

    fun clearFilters() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[FILTER_CATEGORY_KEY] = ""
                preferences[SHOW_ONLY_PENDING_KEY] = false
            }
        }
    }

    fun insertTask(task: Task) {
        viewModelScope.launch {
            taskDao.insert(task)
        }
    }

    fun insertTask(title: String) {
        viewModelScope.launch {
            taskDao.insert(Task(title = title))
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

    fun getTasksByFilter(filter: TaskFilter): StateFlow<List<Task>> {
        return when (filter) {
            TaskFilter.ALL -> allTasks
            TaskFilter.COMPLETED -> completedTasks
            TaskFilter.PENDING -> pendingTasks
            TaskFilter.OVERDUE -> overdueTasks
            TaskFilter.TODAY -> todayTasks
            TaskFilter.HIGH_PRIORITY -> combine(
                taskDao.getTasksByPriority(TaskPriority.HIGH),
                taskDao.getTasksByPriority(TaskPriority.URGENT)
            ) { high: List<Task>, urgent: List<Task> ->
                (high + urgent).sortedBy { it.deadlineTimestamp ?: Long.MAX_VALUE }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = emptyList()
            )
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