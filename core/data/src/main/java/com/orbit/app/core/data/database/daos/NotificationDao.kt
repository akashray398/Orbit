package com.orbit.app.core.data.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.orbit.app.core.data.database.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Query("SELECT * FROM captured_notifications WHERE postedAt >= :cutoffTimestamp ORDER BY postedAt DESC LIMIT :limit")
    fun observeRecentNotifications(cutoffTimestamp: Long, limit: Int = 50): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM captured_notifications WHERE postedAt >= :cutoffTimestamp ORDER BY postedAt DESC LIMIT :limit")
    suspend fun getRecentNotifications(cutoffTimestamp: Long, limit: Int = 50): List<NotificationEntity>

    @Query("SELECT COUNT(*) FROM captured_notifications")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("DELETE FROM captured_notifications WHERE postedAt < :cutoffTimestamp")
    suspend fun purgeOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM captured_notifications")
    suspend fun clearAllNotifications()
}
