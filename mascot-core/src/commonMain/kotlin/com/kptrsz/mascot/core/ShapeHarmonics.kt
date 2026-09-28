package com.kptrsz.mascot.core

import kotlin.math.PI
import kotlin.math.abs
import kotlin.random.Random

/**
 * Seeded sine harmonics that define the resting silhouette and how it deforms over time.
 * Pure data derived from [MascotShape]; `kotlin.random.Random(seed)` is deterministic on every platform.
 */
internal class ShapeHarmonics private constructor(
    val orders: IntArray,
    val staticAmplitudes: FloatArray,
    val staticPhases: FloatArray,
    val wobbleWeights: FloatArray,
    val wobbleRateMultipliers: FloatArray,
) {
    val size: Int get() = orders.size

    companion object {
        private const val MIN_ORDER = 2
        private const val MAX_ORDER = 8

        fun from(shape: MascotShape): ShapeHarmonics {
            val random = Random(shape.seed)
            val count = MAX_ORDER - MIN_ORDER + 1
            val orders = IntArray(count) { MIN_ORDER + it }
            val staticAmplitudes = FloatArray(count)
            val staticPhases = FloatArray(count)
            val wobbleWeights = FloatArray(count)
            val wobbleRateMultipliers = FloatArray(count)
            var weightSum = 0f
            for (i in 0 until count) {
                // Energy concentrates around the dominant lobe count and decays with distance from it.
                val falloff = 1f / (1f + abs(orders[i] - shape.lobes))
                staticAmplitudes[i] = shape.irregularity * falloff * random.nextFloat(0.5f, 1f)
                staticPhases[i] = random.nextFloat(0f, TWO_PI)
                wobbleWeights[i] = falloff * random.nextFloat(0.6f, 1f)
                // Mixed signs make neighbouring harmonics drift in opposite directions: organic, not spinning.
                val direction = if (random.nextBoolean()) 1f else -1f
                wobbleRateMultipliers[i] = direction * random.nextFloat(0.7f, 1.3f)
                weightSum += wobbleWeights[i]
            }
            // Normalize so MotionProfile.wobbleAmplitude is the peak deformation regardless of shape.
            for (i in 0 until count) wobbleWeights[i] /= weightSum
            return ShapeHarmonics(orders, staticAmplitudes, staticPhases, wobbleWeights, wobbleRateMultipliers)
        }
    }
}

internal const val TWO_PI: Float = (2 * PI).toFloat()

private fun Random.nextFloat(from: Float, until: Float): Float = from + nextFloat() * (until - from)
