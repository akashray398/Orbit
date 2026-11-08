package com.orbit.app.core.data.di

import com.orbit.app.core.data.gateway.FakeAgentGateway
import com.orbit.app.core.data.repository.InMemoryChatRepositoryImpl
import com.orbit.app.core.data.repository.OnboardingRepositoryImpl
import com.orbit.app.core.data.repository.VoiceSettingsRepositoryImpl
import com.orbit.app.core.data.voice.AndroidSpeechOutputController
import com.orbit.app.core.data.voice.AndroidVoiceInputController
import com.orbit.app.core.domain.repository.AgentGateway
import com.orbit.app.core.domain.repository.ChatRepository
import com.orbit.app.core.domain.repository.OnboardingRepository
import com.orbit.app.core.domain.repository.SpeechOutputController
import com.orbit.app.core.domain.repository.VoiceInputController
import com.orbit.app.core.domain.repository.VoiceSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
        impl: FakeAgentGateway
    ): AgentGateway

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: InMemoryChatRepositoryImpl
    ): ChatRepository

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
}
