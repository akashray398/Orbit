package com.orbit.app.core.designsystem.motion

object SplashMotionSpec {
    const val TotalIntroDurationMs = 2800
    const val TapToSkipThresholdMs = 500L

    // Timeline phases normalized [0.0f .. 1.0f]
    const val Phase1End = 0.30f        // 0ms -> 840ms (Ring Sweep)
    const val Phase2Start = 0.15f      // 420ms -> 1540ms (Satellite & Core Scale)
    const val Phase2End = 0.55f
    const val Phase3Start = 0.30f      // 840ms -> 2100ms (Glow Pulse & Particle Drift)
    const val Phase3End = 0.75f
    const val Phase4Start = 0.40f      // 1120ms -> 2800ms (Wordmark & Tagline)
    const val Phase4End = 1.0f

    const val ParticleCount = 16
}
