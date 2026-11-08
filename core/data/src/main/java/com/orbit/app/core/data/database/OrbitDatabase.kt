package com.orbit.app.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.orbit.app.core.data.database.converters.OrbitTypeConverters
import com.orbit.app.core.data.database.daos.ActionLogDao
import com.orbit.app.core.data.database.daos.ConversationDao
import com.orbit.app.core.data.database.daos.MessageDao
import com.orbit.app.core.data.database.daos.NoteDao
import com.orbit.app.core.data.database.daos.ReminderDao
import com.orbit.app.core.data.database.daos.TaskDao
import com.orbit.app.core.data.database.entities.ActionLogEntity
import com.orbit.app.core.data.database.entities.ConversationEntity
import com.orbit.app.core.data.database.entities.MessageEntity
import com.orbit.app.core.data.database.entities.NoteEntity
import com.orbit.app.core.data.database.entities.NoteFtsEntity
import com.orbit.app.core.data.database.entities.ReminderEntity
import com.orbit.app.core.data.database.entities.TaskEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        NoteEntity::class,
        NoteFtsEntity::class,
        TaskEntity::class,
        ReminderEntity::class,
        ActionLogEntity::class
    ],
    version = 1,
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
}
