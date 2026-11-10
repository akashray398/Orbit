# Orbit - Accessibility Audit & Documentation

Orbit is designed to be accessible to all users, adhering to Android Accessibility Guidelines, WCAG 2.1 AA standards, TalkBack screen reader support, dynamic font scaling (up to 200%), and reduced motion preference integration.

## Accessibility Features Summary

1. **Touch Target Size**:
   - All interactive controls (buttons, icon buttons, text fields, chips, tabs) meet or exceed the mandatory 48dp x 48dp touch target threshold.
2. **TalkBack Screen Reader Support**:
   - All icons, agent orb states, live waveform, and chat messages carry explicit, descriptive `contentDescription` or `semantics` properties.
   - Voice input button supports custom TalkBack actions ("Start listening" / "Stop listening") for hands-free mode without requiring continuous press-and-hold gestures.
3. **High Contrast & Dark Mode**:
   - Palette colors adhere to WCAG 2.1 AA contrast ratios (Electric Violet `#7C5CFF` on dark navy `#070B1A`, high contrast body text `#FFFFFF` & `#F1F5F9`).
   - Color is never used as the sole indicator of state; badges and chips always pair color with text and icons (e.g. `DONE`, `FAILED`, `DENIED`, `INTERRUPTED`).
4. **Reduced Motion**:
   - System reduced motion preference or in-app override (`AppSettingsRepository.reducedMotionOverride`) replaces complex Canvas animations (satellite orbits, pulsing ripples) with clean static level meters and simple fades.
5. **Dynamic Font Scale**:
   - Compose layouts use standard SP font units and adaptive scroll containers supporting 200% font scale without text truncation or layout clipping.

## TalkBack Walkthrough Script

- **Onboarding**: Swipe through horizontal pager cards; tap "Get Started" to complete setup.
- **Chat Screen**: Focus on message bubbles ("You said: ...", "Orbit Agent replied: ..."); double-tap to open long-press context menu (Copy / Edit).
- **Voice Input**: Focus on microphone button; double-tap to toggle hands-free listening mode; speech transcript is announced live.
- **Confirmation Sheet**: Double-tap "Approve" or "Deny" to respond to pending system action requests.
- **Activity & Today**: Swipe to view daily activity logs; tap any item card to expand details or trigger action undo.

## Compliance Checklist

- [x] All clickable elements >= 48dp x 48dp
- [x] Zero unlabelled image buttons or Canvas graphics
- [x] Reduced motion fallback implemented across Canvas components
- [x] Tested at 200% font scale
- [x] Dark/Light mode contrast ratios verified
