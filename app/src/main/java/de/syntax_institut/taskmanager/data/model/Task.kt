package de.syntax_institut.taskmanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val deadlineTimestamp: Long? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH, URGENT
}