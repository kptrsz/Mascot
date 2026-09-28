package com.kptrsz.mascot.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MotionProfileTest {
    private val a = MotionProfile(breathAmplitude = 0f, breathRate = 0f, wobbleAmplitude = 0f, wobbleRate = 0f, glow = 0f)
    private val b = MotionProfile(breathAmplitude = 0.2f, breathRate = 2f, wobbleAmplitude = 0.2f, wobbleRate = 4f, stretch = 0.2f, glow = 1f)

    @Test
    fun lerpReturnsEndpoints() {
        assertEquals(a, MotionProfile.lerp(a, b, 0f))
        assertEquals(b, MotionProfile.lerp(a, b, 1f))
    }

    @Test
    fun lerpInterpolatesMidpoint() {
        val mid = MotionProfile.lerp(a, b, 0.5f)
        assertEquals(0.1f, mid.breathAmplitude, 1e-6f)
        assertEquals(2f, mid.wobbleRate, 1e-6f)
        assertEquals(0.5f, mid.glow, 1e-6f)
    }

    @Test
    fun lerpClampsFraction() {
        assertEquals(b, MotionProfile.lerp(a, b, 3f))
        assertEquals(a, MotionProfile.lerp(a, b, -1f))
    }

    @Test
    fun rejectsOutOfRangeValues() {
        assertFailsWith<IllegalArgumentException> { a.copy(glow = 1.5f) }
        assertFailsWith<IllegalArgumentException> { a.copy(breathRate = -1f) }
        assertFailsWith<IllegalArgumentException> { a.copy(stretch = 0.9f) }
    }

    @Test
    fun overridesReplaceOnlyGivenEmotions() {
        val custom = MotionProfiles.Default.withOverrides(mapOf(MascotEmotion.Happy to b))
        assertEquals(b, custom.profileFor(MascotEmotion.Happy))
        assertEquals(MotionProfiles.Default.profileFor(MascotEmotion.Sad), custom.profileFor(MascotEmotion.Sad))
    }

    @Test
    fun defaultProfilesCoverEveryEmotion() {
        MascotEmotion.entries.forEach { MotionProfiles.Default.profileFor(it) }
    }
}
