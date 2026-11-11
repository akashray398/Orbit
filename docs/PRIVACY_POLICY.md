# Orbit - Privacy Policy

**Effective Date**: March 30, 2025

Orbit is committed to protecting your privacy and ensuring you maintain complete control over your personal data.

## 1. Information Collection and Storage

- **Audio Data**: Orbit NEVER records, stores, or transmits raw audio files to external servers. Speech recognition is processed through Android's native `SpeechRecognizer` API and preferred on-device engines (API 33+).
- **Notification Data**: Notification summaries are 100% opt-in. Sensitive banking, financial, payment, and password manager notifications are strictly filtered out and NEVER stored. Stored titles and text are sanitized locally using encryption and redaction filters before saving to Room database.
- **Local Persistence**: All conversations, notes, tasks, reminders, and activity logs are stored locally on your device in `orbit_database.db`.

## 2. Network Transmissions & Proxy Isolation

- **Zero API Keys in App**: Orbit connects to model providers exclusively through a secure backend proxy (`OrbitProxy`).
- **Redaction Filter**: Egress text is processed through `Redactor` to strip email addresses, phone numbers, credit card sequences, and OTP codes before leaving your device.

## 3. Data Control and Deletion

You retain full ownership of your data. From the Settings & Privacy screen, you can at any time:
- Clear chat conversation history
- Delete all local notes, tasks, and reminders
- Wipe all local application data
- Export your data as a JSON file

## 4. Contact Information

For privacy inquiries, please contact `privacy@orbit-agent.app`.
