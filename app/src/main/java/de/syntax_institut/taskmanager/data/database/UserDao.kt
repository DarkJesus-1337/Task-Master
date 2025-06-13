package de.syntax_institut.taskmanager.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import de.syntax_institut.taskmanager.data.model.User
import de.syntax_institut.taskmanager.data.model.UserWithTasks
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUser(userId: Long = 0): Flow<User?>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserSync(userId: Long = 0): User?

    @Transaction
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsersWithTasks(): Flow<List<UserWithTasks>>

    @Transaction
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserWithTasks(userId: Long): Flow<UserWithTasks?>
}