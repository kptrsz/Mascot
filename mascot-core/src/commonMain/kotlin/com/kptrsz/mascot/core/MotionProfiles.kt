package com.kptrsz.mascot.core

/**
 * Maps an emotion to the motion used to express it.
 *
 * Implement this to fully customize the mascot's body language, or wrap
 * [MotionProfiles.Default] with [MotionProfiles.withOverrides] to tweak single emotions.
 */
public fun interface MotionProfiles {
    public fun profileFor(emotion: MascotEmotion): MotionProfile

    public companion object {
        /** Built-in body language tuned for a small, friendly companion. */
        public val Default: MotionProfiles = MotionProfiles { emotion ->
            when (emotion) {
                MascotEmotion.Neutral -> NEUTRAL
                MascotEmotion.Calm -> CALM
                MascotEmotion.Happy -> HAPPY
                MascotEmotion.Excited -> EXCITED
                MascotEmotion.Curious -> CURIOUS
                MascotEmotion.Surprised -> SURPRISED
                MascotEmotion.Sad -> SAD
                MascotEmotion.Sleepy -> SLEEPY
            }
        }

        private val NEUTRAL = MotionProfile(
            breathAmplitude = 0.025f, breathRate = 0.3f,
            wobbleAmplitude = 0.035f, wobbleRate = 0.25f,
            glow = 0.3f,
        )
        private val CALM = MotionProfile(
            breathAmplitude = 0.03f, breathRate = 0.18f,
            wobbleAmplitude = 0.02f, wobbleRate = 0.12f,
            swayDegrees = 2f, swayRate = 0.1f,
            stretch = -0.03f, glow = 0.25f,
        )
        private val HAPPY = MotionProfile(
            breathAmplitude = 0.02f, breathRate = 0.5f,
            wobbleAmplitude = 0.05f, wobbleRate = 0.45f,
            bounceHeight = 0.18f, bounceRate = 1.1f, squashStretch = 0.5f,
            swayDegrees = 4f, swayRate = 0.55f,
            stretch = 0.03f, glow = 0.5f,
        )
        private val EXCITED = MotionProfile(
            breathAmplitude = 0.02f, breathRate = 0.9f,
            wobbleAmplitude = 0.08f, wobbleRate = 0.9f,
            bounceHeight = 0.45f, bounceRate = 2.0f, squashStretch = 0.9f,
            swayDegrees = 7f, swayRate = 1.0f,
            stretch = 0.06f, glow = 0.8f,
        )
        private val CURIOUS = MotionProfile(
            breathAmplitude = 0.02f, breathRate = 0.4f,
            wobbleAmplitude = 0.04f, wobbleRate = 0.35f,
            bounceHeight = 0.05f, bounceRate = 0.5f, squashStretch = 0.3f,
            swayDegrees = 12f, swayRate = 0.3f,
            stretch = 0.08f, glow = 0.45f,
        )
        private val SURPRISED = MotionProfile(
            breathAmplitude = 0.015f, breathRate = 1.2f,
            wobbleAmplitude = 0.1f, wobbleRate = 1.4f,
            bounceHeight = 0.25f, bounceRate = 1.4f, squashStretch = 0.8f,
            stretch = 0.2f, glow = 0.7f,
        )
        private val SAD = MotionProfile(
            breathAmplitude = 0.02f, breathRate = 0.15f,
            wobbleAmplitude = 0.015f, wobbleRate = 0.1f,
            swayDegrees = 1.5f, swayRate = 0.08f,
            stretch = -0.18f, glow = 0.1f,
        )
        private val SLEEPY = MotionProfile(
            breathAmplitude = 0.06f, breathRate = 0.12f,
            wobbleAmplitude = 0.01f, wobbleRate = 0.06f,
            swayDegrees = 3f, swayRate = 0.06f,
            stretch = -0.25f, glow = 0.12f,
        )
    }
}

/** Returns profiles that use [overrides] where present and fall back to this mapping otherwise. */
public fun MotionProfiles.withOverrides(overrides: Map<MascotEmotion, MotionProfile>): MotionProfiles {
    if (overrides.isEmpty()) return this
    val fallback = this
    return MotionProfiles { emotion -> overrides[emotion] ?: fallback.profileFor(emotion) }
}
