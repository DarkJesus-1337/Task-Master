package de.syntax_institut.taskmanager.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.syntax_institut.taskmanager.data.model.Task
import de.syntax_institut.taskmanager.data.model.TaskPriority
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task)

    @Query("SELECT * FROM tasks ORDER BY priority DESC, deadlineTimestamp ASC, id ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted ORDER BY priority DESC, deadlineTimestamp ASC, id ASC")
    fun getTasksByCompletionStatus(isCompleted: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE deadlineTimestamp IS NOT NULL AND deadlineTimestamp <= :currentTime AND isCompleted = 0 ORDER BY deadlineTimestamp ASC")
    fun getOverdueTasks(currentTime: Long = System.currentTimeMillis()): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE deadlineTimestamp IS NOT NULL AND deadlineTimestamp BETWEEN :startTime AND :endTime AND isCompleted = 0 ORDER BY deadlineTimestamp ASC")
    fun getTasksDueToday(startTime: Long, endTime: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY deadlineTimestamp ASC, id ASC")
    fun getTasksByPriority(priority: TaskPriority): Flow<List<Task>>

    @Query("SELECT DISTINCT category FROM tasks WHERE category != '' ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Delete
    suspend fun delete(task: Task)

    @Update
    suspend fun update(task: Task)
}