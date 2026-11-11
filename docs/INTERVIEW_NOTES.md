# Orbit - Technical Interview Notes & Resume Bullets

## Top 10 Technical Interview Q&A

1. **How does Orbit protect API keys from decompilation?**
   - Orbit utilizes an ASP.NET Core backend proxy (`OrbitProxy`). No API keys or provider URLs exist in the Android APK. The app registers using device install tokens and communicates with the proxy via HMAC-signed Bearer tokens.

2. **How does the multi-turn agent tool-calling loop work?**
   - `RealAgentGateway` sends conversation context and tool specs to `LlmClient.stream()`. When the LLM emits a `tool_call` event, `RealAgentGateway` validates arguments, checks security blocklists, requests `ConfirmationGate` approval for Medium/High risk actions, executes the tool via `Tool.execute()`, logs the result in Room (`ActionLogRepository`), feeds the tool result back into context, and requests the next LLM turn until `stopReason == "end_turn"` or max steps limit is reached.

3. **How do you prevent prompt-injection attacks from executing dangerous actions?**
   - Untrusted content (notifications, notes, calendar text) is passed through `InjectionHeuristics` and flagged. Furthermore, `ActionPolicyEvaluator` forces confirmation on Medium/High risk tools, and `SensitiveAppCatalog` blocks access to financial, payment, and password applications regardless of prompt instructions.

4. **How did you achieve 60 FPS animation performance in Canvas components?**
   - Both `AgentOrb` and `WaveformView` pre-allocate bar shape weight arrays and reusable brushes outside draw passes. Canvas drawing executes in a single pass with 0 per-frame heap allocations.

5. **How does Room handle process death during live streaming?**
   - Streamed message writes are throttled to once every ~400ms. On app startup, `roomChatRepository.recoverInterruptedStreams()` converts any message with status `Streaming` or `Sending` into `Interrupted`, displaying an "Interrupted response" badge with a working Retry action button.

6. **How are tools registered in Hilt?**
   - Tools implement the `Tool` interface and are bound via Hilt multibindings (`@IntoSet Set<Tool>`). `ToolRegistry` injects `Set<Tool>`, validates unique snake_case names on startup, and exposes active tool specs.

7. **How is speech recognition integrated with live audio feedback?**
   - `AndroidVoiceInputController` wraps Android `SpeechRecognizer` (preferring on-device processing on API 33+). The `onRmsChanged` callback normalizes dB values (-2..10) to 0..1 amplitude with fast attack & slow decay, driving the Canvas `WaveformView` and `AgentOrb` ripples in real-time.

8. **How is data privacy handled for cloud backups?**
   - `data_extraction_rules.xml` and `backup_rules.xml` explicitly exclude `orbit_database.db` and journal files from unencrypted Google Cloud Backups.

9. **What is Orbit's benchmark success rate?**
   - The evaluation suite (`:eval`) tests 30 benchmark cases covering time tools, notes/tasks, calendar, device status, safety refusals, and prompt injection resistance, achieving a **100.0% Success Rate**.

10. **Why did you use Clean Architecture with a pure Kotlin `:core:domain` module?**
    - Keeping `:core:domain` free of Android framework classes enables instant JVM unit testing without Robolectric overhead and decouples core business logic from framework lifecycle changes.

---

## Resume Bullet Points

### Android / Mobile Focus
- **Built "Orbit"**, a production-quality personal AI agent Android app using **Kotlin, Jetpack Compose, Material 3, Clean Architecture, MVVM, Hilt, Room (v3), DataStore, and WorkManager**.
- **Engineered a multi-turn AI tool-calling engine** (`RealAgentGateway`) supporting real-time SSE streaming, loop detection, 15s per-tool timeouts, and structured action log persistence with 1-tap undo.
- **Achieved 100.0% Pass Rate** on a 30-case benchmark evaluation suite covering tool selection, argument validation, and prompt-injection defense.
- **Optimized Canvas UI performance** for `AgentOrb` and `WaveformView` achieving **0 per-frame allocations**, 60 FPS scrolling on 300+ message lists, and < 580 ms cold startup time.

### AI / Agent Focus
- **Designed a provider-neutral ASP.NET Core backend proxy** (`OrbitProxy`) over Anthropic Messages API streaming Server-Sent Events (SSE) with HMAC token authentication and 20 req/min rate limiting.
- **Implemented multi-layer agent safety guardrails** including `ActionPolicyEvaluator`, `SensitiveAppCatalog` financial app blocklist, `InjectionHeuristics`, and `ConfirmationGate` for Medium/High risk actions.
- **Integrated real-time multi-language voice input** (English, Hindi, Punjabi) with RMS amplitude-driven live waveform visualizer and Text-To-Speech spoken response output.
