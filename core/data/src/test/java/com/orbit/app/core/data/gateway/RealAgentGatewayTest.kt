package com.orbit.app.core.data.gateway

import com.orbit.app.core.data.network.FakeLlmClient
import com.orbit.app.core.data.repository.InMemoryChatRepositoryImpl
import com.orbit.app.core.domain.model.AgentEvent
import com.orbit.app.core.domain.model.AgentSettings
import com.orbit.app.core.domain.model.ConfirmationDecision
import com.orbit.app.core.domain.repository.ActionLogRepository
import com.orbit.app.core.domain.repository.AgentSettingsRepository
import com.orbit.app.core.domain.repository.ToolRegistry
import com.orbit.app.core.domain.tools.EchoDebugTool
import com.orbit.app.core.domain.tools.GetCurrentTimeTool
import com.orbit.app.core.domain.usecase.ConversationContextBuilder
import com.orbit.app.core.domain.usecase.ToolResultSanitizer
import com.orbit.app.core.testing.FakeConfirmationGate
import com.orbit.app.core.testing.MainDispatcherRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RealAgentGatewayTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val fakeLlmClient = FakeLlmClient()
    private val toolRegistry = ToolRegistry(setOf(GetCurrentTimeTool(), EchoDebugTool()))
    private val chatRepository = InMemoryChatRepositoryImpl()
    private val fakeConfirmationGate = FakeConfirmationGate(ConfirmationDecision.Approved)
    private val contextBuilder = ConversationContextBuilder()
    private val sanitizer = ToolResultSanitizer()

    private val fakeActionLogRepo = object : ActionLogRepository {
        override val timeline: Flow<List<com.orbit.app.core.domain.model.ActionLog>> = flowOf(emptyList())
        override fun observeById(id: String) = flowOf(null)
        override suspend fun startAction(toolName: String, argumentsJson: String, riskLevel: com.orbit.app.core.domain.model.RiskLevel) =
            com.orbit.app.core.domain.result.Result.Success(com.orbit.app.core.domain.model.ActionLog("log_1", toolName, argumentsJson, "Pending"))
        override suspend fun finishAction(id: String, status: com.orbit.app.core.domain.model.ActionStatus, summary: String, undoPayloadJson: String?) =
            com.orbit.app.core.domain.result.Result.Success(Unit)
        override suspend fun markUndone(id: String) = com.orbit.app.core.domain.result.Result.Success(Unit)
        override suspend fun getPendingCount() = com.orbit.app.core.domain.result.Result.Success(0)
        override suspend fun purgeOlderThan(days: Int) = com.orbit.app.core.domain.result.Result.Success(0)
    }

    private val fakeAgentSettingsRepo = object : AgentSettingsRepository {
        override val agentSettings = flowOf(AgentSettings(maxAgentSteps = 4))
        override suspend fun setMaxAgentSteps(steps: Int) {}
        override suspend fun setConfirmMediumRisk(confirm: Boolean) {}
        override suspend fun setHistoryWindowSize(size: Int) {}
        override suspend fun setSendRedactedTextOnly(redact: Boolean) {}
        override suspend fun setRetentionDays(days: Int) {}
    }

    @Test
    fun agentGateway_executes_simple_text_streaming_flow() = runTest {
        val gateway = RealAgentGateway(
            llmClient = fakeLlmClient,
            toolRegistry = toolRegistry,
            chatRepository = chatRepository,
            actionLogRepository = fakeActionLogRepo,
            agentSettingsRepository = fakeAgentSettingsRepo,
            confirmationGate = fakeConfirmationGate,
            contextBuilder = contextBuilder,
            sanitizer = sanitizer
        )

        val events = gateway.send("Hello Orbit").toList()
        assertTrue(events.any { it is AgentEvent.StateChanged })
        assertTrue(events.any { it is AgentEvent.TextDelta })
        assertTrue(events.last() is AgentEvent.Completed)
    }
}
