# Orbit - Architectural Decision Records (ADRs)

## ADR 01: Tool-Based AI Agent over Accessibility Service Automation
- **Context**: Autonomous Android agents can use AccessibilityServices to click screen elements or native tool APIs.
- **Decision**: Orbit exclusively uses structured native tool-calling APIs (`Tool` interface, Intents, CalendarContract, AlarmManager, Room).
- **Rationale**: Accessibility automation is fragile, breaks on UI updates, violates Play Store security policies, and poses security risks. Structured tool-calling provides deterministic execution, type safety, and clear audit logging.

## ADR 02: ASP.NET Core Backend Proxy for API Key Isolation
- **Context**: Calling LLM provider endpoints (Anthropic, Gemini) directly from an Android APK exposes API keys to decompilation.
- **Decision**: Built a provider-neutral ASP.NET Core minimal API proxy (`OrbitProxy`) that streams SSE events.
- **Rationale**: Isolates API keys server-side, enables HMAC client token authentication, enforces rate limits, and prevents cost exploitation.

## ADR 03: Pure Kotlin `core:domain` Module
- **Context**: Defining business logic and repository interfaces in Android library modules binds domain logic to framework classes.
- **Decision**: Kept `:core:domain` as a pure Kotlin JVM library (`orbit.jvm.library`) with zero Android dependencies.
- **Rationale**: Ensures fast JVM unit testing, clean architecture separation, and potential multiplatform reuse.

## ADR 04: Dual Confirmation Gate & Security Blocklist Strategy
- **Context**: Autonomous tool execution could trigger unwanted phone changes or sensitive app actions.
- **Decision**: Implemented `ActionPolicyEvaluator` with a hard security blocklist (banking, UPI, payment, password apps blocked) and `ConfirmationGate` requiring explicit user approval for Medium and High risk actions.
- **Rationale**: Guarantees user sovereignty over their device and prevents prompt-injection attacks from executing unauthorized actions.

## ADR 05: Room Database & DataStore Preferences Separation
- **Context**: App state includes structured relational data (conversations, notes, tasks, action logs) and key-value preferences.
- **Decision**: Used Room (`orbit_database.db` v3) for structured data and DataStore Preferences for user settings.
- **Rationale**: Prevents database clutter, provides fast key-value reads, and enables room-level backup exclusion for conversation history.
