package com.orbit.app.core.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.orbit.app.core.data.database.entities.ActionLogEntity
import com.orbit.app.core.domain.model.ActionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionLogDao {

    @Query("SELECT * FROM action_logs ORDER BY startedAt DESC")
    fun observeTimeline(): Flow<List<ActionLogEntity>>

    @Query("SELECT * FROM action_logs WHERE id = :id")
    fun observeById(id: String): Flow<ActionLogEntity?>

    @Query("SELECT * FROM action_logs WHERE id = :id")
    suspend fun getById(id: String): ActionLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActionLog(log: ActionLogEntity)

    @Query("UPDATE action_logs SET status = :status, resultSummary = :resultSummary, finishedAt = :finishedAt, undoPayloadJson = :undoPayload WHERE id = :id")
    suspend fun finishAction(id: String, status: ActionStatus, resultSummary: String, finishedAt: Long, undoPayload: String?)

    @Query("UPDATE action_logs SET status = 'Undone', undoneAt = :undoneAt WHERE id = :id")
    suspend fun markUndone(id: String, undoneAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM action_logs WHERE status = 'Pending'")
    suspend fun getPendingCount(): Int

    @Query("DELETE FROM action_logs WHERE startedAt < :cutoffTimestamp")
    suspend fun purgeOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM action_logs")
    suspend fun clearAllActionLogs()
}
