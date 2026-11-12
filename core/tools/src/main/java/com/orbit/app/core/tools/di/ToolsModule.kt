package com.orbit.app.core.tools.di

import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.tools.AddCalendarEventTool
import com.orbit.app.core.tools.AddTaskTool
import com.orbit.app.core.tools.AppendToNoteTool
import com.orbit.app.core.tools.CancelReminderTool
import com.orbit.app.core.tools.CompleteTaskTool
import com.orbit.app.core.tools.CreateNoteTool
import com.orbit.app.core.tools.CreateReminderTool
import com.orbit.app.core.tools.GetDeviceStatusTool
import com.orbit.app.core.tools.GetUpcomingEventsTool
import com.orbit.app.core.tools.ListTasksTool
import com.orbit.app.core.tools.MakeCallTool
import com.orbit.app.core.tools.OpenAppTool
import com.orbit.app.core.tools.ReadNoteTool
import com.orbit.app.core.tools.SearchNotesTool
import com.orbit.app.core.tools.SetAlarmTool
import com.orbit.app.core.tools.SetDoNotDisturbTool
import com.orbit.app.core.tools.SetTimerTool
import com.orbit.app.core.tools.SummarizeNotificationsTool
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

    @Binds
    @IntoSet
    abstract fun bindCreateNoteTool(tool: CreateNoteTool): Tool

    @Binds
    @IntoSet
    abstract fun bindAppendToNoteTool(tool: AppendToNoteTool): Tool

    @Binds
    @IntoSet
    abstract fun bindSearchNotesTool(tool: SearchNotesTool): Tool

    @Binds
    @IntoSet
    abstract fun bindReadNoteTool(tool: ReadNoteTool): Tool

    @Binds
    @IntoSet
    abstract fun bindAddTaskTool(tool: AddTaskTool): Tool

    @Binds
    @IntoSet
    abstract fun bindListTasksTool(tool: ListTasksTool): Tool

    @Binds
    @IntoSet
    abstract fun bindCompleteTaskTool(tool: CompleteTaskTool): Tool

    @Binds
    @IntoSet
    abstract fun bindSummarizeNotificationsTool(tool: SummarizeNotificationsTool): Tool

    @Binds
    @IntoSet
    abstract fun bindGetDeviceStatusTool(tool: GetDeviceStatusTool): Tool

    @Binds
    @IntoSet
    abstract fun bindSetDoNotDisturbTool(tool: SetDoNotDisturbTool): Tool

    @Binds
    @IntoSet
    abstract fun bindGetUpcomingEventsTool(tool: GetUpcomingEventsTool): Tool

    @Binds
    @IntoSet
    abstract fun bindMakeCallTool(tool: MakeCallTool): Tool
}
