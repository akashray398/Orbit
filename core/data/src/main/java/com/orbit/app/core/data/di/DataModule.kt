package com.orbit.app.core.data.di

import com.orbit.app.core.data.gateway.RealAgentGateway
import com.orbit.app.core.data.logging.CrashReporterImpl
import com.orbit.app.core.data.network.LlmClientImpl
import com.orbit.app.core.data.repository.ActionLogRepositoryImpl
import com.orbit.app.core.data.repository.AgentSettingsRepositoryImpl
import com.orbit.app.core.data.repository.AppSettingsRepositoryImpl
import com.orbit.app.core.data.repository.DataMaintenanceRepositoryImpl
import com.orbit.app.core.data.repository.NoteRepositoryImpl
import com.orbit.app.core.data.repository.OnboardingRepositoryImpl
import com.orbit.app.core.data.repository.ReminderRepositoryImpl
import com.orbit.app.core.data.repository.RoomChatRepositoryImpl
import com.orbit.app.core.data.repository.TaskRepositoryImpl
import com.orbit.app.core.data.repository.VoiceSettingsRepositoryImpl
import com.orbit.app.core.data.voice.AndroidSpeechOutputController
import com.orbit.app.core.data.voice.AndroidVoiceInputController
import com.orbit.app.core.domain.model.ConfirmationGate
import com.orbit.app.core.domain.model.Tool
import com.orbit.app.core.domain.repository.ActionLogRepository
import com.orbit.app.core.domain.repository.AgentGateway
import com.orbit.app.core.domain.repository.AgentSettingsRepository
import com.orbit.app.core.domain.repository.AppSettingsRepository
import com.orbit.app.core.domain.repository.ChatRepository
import com.orbit.app.core.domain.repository.CrashReporter
import com.orbit.app.core.domain.repository.DataMaintenanceRepository
import com.orbit.app.core.domain.repository.LlmClient
import com.orbit.app.core.domain.repository.NoteRepository
import com.orbit.app.core.domain.repository.OnboardingRepository
import com.orbit.app.core.domain.repository.ReminderRepository
import com.orbit.app.core.domain.repository.SpeechOutputController
import com.orbit.app.core.domain.repository.TaskRepository
import com.orbit.app.core.domain.repository.VoiceInputController
import com.orbit.app.core.domain.repository.VoiceSettingsRepository
import com.orbit.app.core.domain.tools.EchoDebugTool
import com.orbit.app.core.domain.tools.GetCurrentTimeTool
import com.orbit.app.core.domain.usecase.PendingConfirmationSource
import com.orbit.app.core.domain.usecase.UiConfirmationGate
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(
        impl: OnboardingRepositoryImpl
    ): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindAgentGateway(
        impl: RealAgentGateway
    ): AgentGateway

    @Binds
    @Singleton
    abstract fun bindConfirmationGate(
        gate: UiConfirmationGate
    ): ConfirmationGate

    @Binds
    @Singleton
    abstract fun bindPendingConfirmationSource(
        gate: UiConfirmationGate
    ): PendingConfirmationSource

    @Binds
    @IntoSet
    abstract fun bindGetCurrentTimeTool(
        tool: GetCurrentTimeTool
    ): Tool

    @Binds
    @IntoSet
    abstract fun bindEchoDebugTool(
        tool: EchoDebugTool
    ): Tool

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: RoomChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindLlmClient(
        impl: LlmClientImpl
    ): LlmClient

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        impl: NoteRepositoryImpl
    ): NoteRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        impl: ReminderRepositoryImpl
    ): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindActionLogRepository(
        impl: ActionLogRepositoryImpl
    ): ActionLogRepository

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(
        impl: AppSettingsRepositoryImpl
    ): AppSettingsRepository

    @Binds
    @Singleton
    abstract fun bindAgentSettingsRepository(
        impl: AgentSettingsRepositoryImpl
    ): AgentSettingsRepository

    @Binds
    @Singleton
    abstract fun bindDataMaintenanceRepository(
        impl: DataMaintenanceRepositoryImpl
    ): DataMaintenanceRepository

    @Binds
    @Singleton
    abstract fun bindVoiceInputController(
        impl: AndroidVoiceInputController
    ): VoiceInputController

    @Binds
    @Singleton
    abstract fun bindSpeechOutputController(
        impl: AndroidSpeechOutputController
    ): SpeechOutputController

    @Binds
    @Singleton
    abstract fun bindVoiceSettingsRepository(
        impl: VoiceSettingsRepositoryImpl
    ): VoiceSettingsRepository

    @Binds
    @Singleton
    abstract fun bindCrashReporter(
        impl: CrashReporterImpl
    ): CrashReporter
}
