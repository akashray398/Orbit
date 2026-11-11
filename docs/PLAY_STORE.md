# Orbit - Google Play Store Documentation & Listing Assets

## App Listing Metadata

- **Title**: Orbit - Personal AI Agent
- **Short Description** (78 chars): Handle your phone alarms, reminders, notes, and tasks with a voice AI agent.
- **Full Description**:
  Orbit is an intelligent personal AI agent designed to automate everyday phone tasks through secure tool-calling. Speak naturally or type commands to manage your alarms, create exact reminders, write notes with instant search, check device status, and receive privacy-first notification summaries.

  ### Key Features
  - 🤖 **AI Tool-Calling Engine**: Translates natural language goals into precise phone actions.
  - 🎙️ **Voice Commands & Live Waveform**: Speak in English, Hindi, or Punjabi with real-time audio waveform visualizers.
  - 🔒 **Privacy & Safety First**: Zero raw audio storage. Medium and High risk actions require explicit user confirmation.
  - 📝 **Durable Memory & Notes**: Room database persistence with FTS4 full-text search.
  - 📊 **Agent Activity Timeline**: Transparent action log showing everything Orbit performed with instant one-tap undo support.

## Permissions & Rationale

| Permission | Usage Justification |
| --- | --- |
| `RECORD_AUDIO` | Speech recognition for voice commands (processed on-device when supported). |
| `SCHEDULE_EXACT_ALARM` | Scheduling exact notification alerts for user-requested reminders. |
| `POST_NOTIFICATIONS` | Displaying high-importance reminder alerts and daily brief notifications. |
| `READ_CALENDAR` / `WRITE_CALENDAR` | Reading upcoming events and adding calendar entries. |
| `ACCESS_NOTIFICATION_POLICY` | Modifying Do Not Disturb modes upon explicit user request. |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Capturing un-sensitive notifications for local AI summaries (opt-in only). |

## Asset Requirements

- **App Icon**: 512 x 512 px PNG (`ic_orbit_logo` centered over `#070B1A`).
- **Feature Graphic**: 1024 x 500 px PNG (Orbit logo mark centered over aurora gradient fill).
- **Screenshots**: 8 phone screens (Splash, Onboarding, Chat & Orb, Voice Waveform, Confirmation Sheet, Today Dashboard, Activity Timeline, Settings & Privacy).
