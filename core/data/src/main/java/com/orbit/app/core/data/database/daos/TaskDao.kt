package com.orbit.app.core.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.orbit.app.core.data.database.entities.ReminderEntity
import com.orbit.app.core.data.database.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE status = 'Open' ORDER BY dueAt ASC, priority DESC")
    fun observeOpenTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'Open' ORDER BY dueAt ASC, priority DESC")
    suspend fun getOpenTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE status = 'Done' ORDER BY completedAt DESC")
    fun observeCompletedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'Open' AND dueAt IS NOT NULL AND dueAt <= :endOfDayTimestamp ORDER BY dueAt ASC")
    fun observeDueToday(endOfDayTimestamp: Long): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET status = 'Done', completedAt = :completedAt WHERE id = :id")
    suspend fun completeTask(id: String, completedAt: Long = System.currentTimeMillis())

    @Query("UPDATE tasks SET status = 'Open', completedAt = NULL WHERE id = :id")
    suspend fun reopenTask(id: String)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    @Transaction
    suspend fun insertTaskWithReminder(task: TaskEntity, reminder: ReminderEntity, reminderDao: ReminderDao) {
        insertTask(task)
        reminderDao.insertReminder(reminder)
    }
}
