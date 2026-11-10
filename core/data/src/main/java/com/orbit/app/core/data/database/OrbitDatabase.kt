package com.orbit.app.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.orbit.app.core.data.database.converters.OrbitTypeConverters
import com.orbit.app.core.data.database.daos.ActionLogDao
import com.orbit.app.core.data.database.daos.ConversationDao
import com.orbit.app.core.data.database.daos.MessageDao
import com.orbit.app.core.data.database.daos.NoteDao
import com.orbit.app.core.data.database.daos.NotificationDao
import com.orbit.app.core.data.database.daos.ReminderDao
import com.orbit.app.core.data.database.daos.RoutineDao
import com.orbit.app.core.data.database.daos.TaskDao
import com.orbit.app.core.data.database.entities.ActionLogEntity
import com.orbit.app.core.data.database.entities.ConversationEntity
import com.orbit.app.core.data.database.entities.MessageEntity
import com.orbit.app.core.data.database.entities.NoteEntity
import com.orbit.app.core.data.database.entities.NoteFtsEntity
import com.orbit.app.core.data.database.entities.NotificationEntity
import com.orbit.app.core.data.database.entities.ReminderEntity
import com.orbit.app.core.data.database.entities.RoutineEntity
import com.orbit.app.core.data.database.entities.RoutineRunEntity
import com.orbit.app.core.data.database.entities.TaskEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `captured_notifications` (
                `id` TEXT NOT NULL,
                `packageName` TEXT NOT NULL,
                `appLabel` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `text` TEXT NOT NULL,
                `postedAt` INTEGER NOT NULL,
                `category` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_captured_notifications_postedAt` ON `captured_notifications` (`postedAt`)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `routines` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `timeOfDay` TEXT NOT NULL,
                `daysOfWeek` INTEGER NOT NULL,
                `enabled` INTEGER NOT NULL,
                `customPrompt` TEXT,
                `lastRunAt` INTEGER,
                `lastStatus` TEXT,
                `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `routine_runs` (
                `id` TEXT NOT NULL,
                `routineId` TEXT NOT NULL,
                `startedAt` INTEGER NOT NULL,
                `finishedAt` INTEGER,
                `status` TEXT NOT NULL,
                `resultText` TEXT NOT NULL,
                `usedAi` INTEGER NOT NULL,
                `tokenCount` INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        db.execSQL("ALTER TABLE `action_logs` ADD COLUMN `source` TEXT NOT NULL DEFAULT 'Chat'")
    }
}

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        NoteEntity::class,
        NoteFtsEntity::class,
        TaskEntity::class,
        ReminderEntity::class,
        ActionLogEntity::class,
        NotificationEntity::class,
        RoutineEntity::class,
        RoutineRunEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(OrbitTypeConverters::class)
abstract class OrbitDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun reminderDao(): ReminderDao
    abstract fun actionLogDao(): ActionLogDao
    abstract fun notificationDao(): NotificationDao
    abstract fun routineDao(): RoutineDao
}
