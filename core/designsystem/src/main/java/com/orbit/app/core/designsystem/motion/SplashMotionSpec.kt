package com.orbit.app.core.designsystem.motion

object SplashMotionSpec {
    const val TotalIntroDurationMs = 1700
    const val TapToSkipThresholdMs = 400L

    // Timeline phases normalized [0.0f .. 1.0f]
    const val Phase1End = 0.35f        // 0ms -> 600ms (Ring Sweep)
    const val Phase2Start = 0.17f      // 300ms -> 1100ms (Satellite & Core Scale)
    const val Phase2End = 0.65f
    const val Phase3Start = 0.41f      // 700ms -> 1400ms (Glow Pulse & Particle Drift)
    const val Phase3End = 0.82f
    const val Phase4Start = 0.65f      // 1100ms -> 1700ms (Wordmark & Tagline)
    const val Phase4End = 1.0f

    const val ParticleCount = 16
}
