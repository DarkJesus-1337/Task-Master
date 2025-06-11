package de.syntax_institut.taskmanager.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.syntax_institut.taskmanager.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task)

    @Query("SELECT * FROM tasks ORDER BY id ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isCompleted = :isCompleted ORDER BY id ASC")
    fun getTasksByCompletionStatus(isCompleted: Boolean): Flow<List<Task>>

    @Delete
    suspend fun delete(task: Task)

    @Update
    suspend fun update(task: Task)
}