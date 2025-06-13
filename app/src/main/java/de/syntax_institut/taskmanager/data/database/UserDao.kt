package de.syntax_institut.taskmanager.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.syntax_institut.taskmanager.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE id = 0")
    fun getUser(): Flow<User?>

    @Query("SELECT * FROM users WHERE id = 0")
    suspend fun getUserSync(): User?
}