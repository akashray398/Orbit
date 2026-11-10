package com.orbit.app.core.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.orbit.app.core.data.database.entities.RoutineEntity
import com.orbit.app.core.data.database.entities.RoutineRunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun observeRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE enabled = 1")
    suspend fun getEnabledRoutines(): List<RoutineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity)

    @Query("SELECT * FROM routine_runs WHERE routineId = :routineId ORDER BY startedAt DESC LIMIT 10")
    fun observeRuns(routineId: String): Flow<List<RoutineRunEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: RoutineRunEntity)
}
