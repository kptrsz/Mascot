package com.kptrsz.mascot.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class MascotShapeTest {
    @Test
    fun sameSeedProducesSameHarmonics() {
        val first = ShapeHarmonics.from(MascotShape(seed = 42))
        val second = ShapeHarmonics.from(MascotShape(seed = 42))
        assertContentEquals(first.staticAmplitudes, second.staticAmplitudes)
        assertContentEquals(first.staticPhases, second.staticPhases)
        assertContentEquals(first.wobbleRateMultipliers, second.wobbleRateMultipliers)
    }

    @Test
    fun differentSeedsProduceDifferentHarmonics() {
        val first = ShapeHarmonics.from(MascotShape(seed = 1))
        val second = ShapeHarmonics.from(MascotShape(seed = 2))
        assertFalse(first.staticPhases.contentEquals(second.staticPhases))
    }

    @Test
    fun wobbleWeightsAreNormalized() {
        val harmonics = ShapeHarmonics.from(MascotShape.Cloud)
        assertEquals(1f, harmonics.wobbleWeights.sum(), 1e-5f)
    }

    @Test
    fun zeroIrregularityHasNoStaticDeformation() {
        val harmonics = ShapeHarmonics.from(MascotShape(irregularity = 0f))
        assertEquals(0f, harmonics.staticAmplitudes.maxOf { kotlin.math.abs(it) })
    }

    @Test
    fun rejectsInvalidParameters() {
        assertFailsWith<IllegalArgumentException> { MascotShape(aspectRatio = 3f) }
        assertFailsWith<IllegalArgumentException> { MascotShape(lobes = 1) }
        assertFailsWith<IllegalArgumentException> { MascotShape(resolution = 8) }
    }
}
