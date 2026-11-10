package com.orbit.app.core.data.di

import android.content.Context
import androidx.room.Room
import com.orbit.app.core.common.coroutine.DispatcherProvider
import com.orbit.app.core.data.database.MIGRATION_1_2
import com.orbit.app.core.data.database.MIGRATION_2_3
import com.orbit.app.core.data.database.OrbitDatabase
import com.orbit.app.core.data.database.daos.ActionLogDao
import com.orbit.app.core.data.database.daos.ConversationDao
import com.orbit.app.core.data.database.daos.MessageDao
import com.orbit.app.core.data.database.daos.NoteDao
import com.orbit.app.core.data.database.daos.NotificationDao
import com.orbit.app.core.data.database.daos.ReminderDao
import com.orbit.app.core.data.database.daos.RoutineDao
import com.orbit.app.core.data.database.daos.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.asExecutor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideOrbitDatabase(
        @ApplicationContext context: Context,
        dispatchers: DispatcherProvider
    ): OrbitDatabase {
        return Room.databaseBuilder(
            context,
            OrbitDatabase::class.java,
            "orbit_database.db"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .setQueryExecutor(dispatchers.io.asExecutor())
            .setTransactionExecutor(dispatchers.io.asExecutor())
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides
    fun provideConversationDao(db: OrbitDatabase): ConversationDao = db.conversationDao()

    @Provides
    fun provideMessageDao(db: OrbitDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideNoteDao(db: OrbitDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideTaskDao(db: OrbitDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideReminderDao(db: OrbitDatabase): ReminderDao = db.reminderDao()

    @Provides
    fun provideActionLogDao(db: OrbitDatabase): ActionLogDao = db.actionLogDao()

    @Provides
    fun provideNotificationDao(db: OrbitDatabase): NotificationDao = db.notificationDao()

    @Provides
    fun provideRoutineDao(db: OrbitDatabase): RoutineDao = db.routineDao()
}
