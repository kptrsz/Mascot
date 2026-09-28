package com.kptrsz.mascot.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MascotColorTest {
    @Test
    fun exposesComponentsFromLongLiteral() {
        val color = MascotColor(0x80FF4020)
        assertEquals(0x80, color.alpha)
        assertEquals(0xFF, color.red)
        assertEquals(0x40, color.green)
        assertEquals(0x20, color.blue)
    }

    @Test
    fun argbRoundTrips() {
        assertEquals(MascotColor(0xFF102030), MascotColor.argb(0xFF, 0x10, 0x20, 0x30))
    }

    @Test
    fun lerpInterpolatesEachChannel() {
        val mid = MascotColor.lerp(MascotColor(0x00000000), MascotColor(0xFFFF8040), 0.5f)
        assertEquals(MascotColor.argb(128, 128, 64, 32), mid)
    }

    @Test
    fun rejectsOutOfRangeComponents() {
        assertFailsWith<IllegalArgumentException> { MascotColor.argb(256, 0, 0, 0) }
    }
}
