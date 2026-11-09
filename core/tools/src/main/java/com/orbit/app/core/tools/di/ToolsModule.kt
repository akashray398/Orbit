package com.orbit.app.core.tools.di

import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.tools.AddCalendarEventTool
import com.orbit.app.core.tools.CancelReminderTool
import com.orbit.app.core.tools.CreateReminderTool
import com.orbit.app.core.tools.OpenAppTool
import com.orbit.app.core.tools.SetAlarmTool
import com.orbit.app.core.tools.SetTimerTool
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class ToolsModule {

    @Binds
    @IntoSet
    abstract fun bindSetAlarmTool(tool: SetAlarmTool): Tool

    @Binds
    @IntoSet
    abstract fun bindSetTimerTool(tool: SetTimerTool): Tool

    @Binds
    @IntoSet
    abstract fun bindCreateReminderTool(tool: CreateReminderTool): Tool

    @Binds
    @IntoSet
    abstract fun bindCancelReminderTool(tool: CancelReminderTool): Tool

    @Binds
    @IntoSet
    abstract fun bindAddCalendarEventTool(tool: AddCalendarEventTool): Tool

    @Binds
    @IntoSet
    abstract fun bindOpenAppTool(tool: OpenAppTool): Tool
}
