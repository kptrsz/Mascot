package com.kptrsz.mascot.core

/**
 * Describes how the mascot moves while expressing a single emotion.
 *
 * All rates are in Hz (cycles per second) *before* the global
 * [MascotAppearance.speed] multiplier is applied. Spatial values are relative to
 * the mascot's radius, so a profile looks the same at any render size.
 *
 * @property breathAmplitude Uniform scale oscillation, e.g. `0.03` = ±3 %.
 * @property breathRate Breathing cycles per second.
 * @property wobbleAmplitude Radial outline deformation, e.g. `0.05` = ±5 % of radius.
 * @property wobbleRate Base speed of the outline deformation.
 * @property bounceHeight Hop height as a fraction of the radius. `0` disables hopping.
 * @property bounceRate Hops per second.
 * @property squashStretch How strongly the body squashes on landing and stretches in air, `0..1`.
 * @property swayDegrees Peak rotation around the ground contact point.
 * @property swayRate Sway cycles per second.
 * @property stretch Static posture: positive is taller/alert, negative is flatter/droopy. Area is preserved.
 * @property glow Intensity of the ambient glow, `0..1`.
 */
public data class MotionProfile(
    val breathAmplitude: Float,
    val breathRate: Float,
    val wobbleAmplitude: Float,
    val wobbleRate: Float,
    val bounceHeight: Float = 0f,
    val bounceRate: Float = 0f,
    val squashStretch: Float = 0f,
    val swayDegrees: Float = 0f,
    val swayRate: Float = 0f,
    val stretch: Float = 0f,
    val glow: Float = 0.3f,
) {
    init {
        require(breathAmplitude in 0f..0.3f) { "breathAmplitude must be in 0..0.3, was $breathAmplitude" }
        require(wobbleAmplitude in 0f..0.4f) { "wobbleAmplitude must be in 0..0.4, was $wobbleAmplitude" }
        require(bounceHeight in 0f..1.5f) { "bounceHeight must be in 0..1.5, was $bounceHeight" }
        require(squashStretch in 0f..1f) { "squashStretch must be in 0..1, was $squashStretch" }
        require(swayDegrees in 0f..45f) { "swayDegrees must be in 0..45, was $swayDegrees" }
        require(stretch in -0.4f..0.4f) { "stretch must be in -0.4..0.4, was $stretch" }
        require(glow in 0f..1f) { "glow must be in 0..1, was $glow" }
        require(breathRate >= 0f && wobbleRate >= 0f && bounceRate >= 0f && swayRate >= 0f) {
            "rates must be non-negative"
        }
    }

    public companion object {
        /** Linear interpolation between two profiles; [fraction] is clamped to `0..1`. */
        public fun lerp(start: MotionProfile, stop: MotionProfile, fraction: Float): MotionProfile {
            val t = fraction.coerceIn(0f, 1f)
            return MotionProfile(
                breathAmplitude = lerp(start.breathAmplitude, stop.breathAmplitude, t),
                breathRate = lerp(start.breathRate, stop.breathRate, t),
                wobbleAmplitude = lerp(start.wobbleAmplitude, stop.wobbleAmplitude, t),
                wobbleRate = lerp(start.wobbleRate, stop.wobbleRate, t),
                bounceHeight = lerp(start.bounceHeight, stop.bounceHeight, t),
                bounceRate = lerp(start.bounceRate, stop.bounceRate, t),
                squashStretch = lerp(start.squashStretch, stop.squashStretch, t),
                swayDegrees = lerp(start.swayDegrees, stop.swayDegrees, t),
                swayRate = lerp(start.swayRate, stop.swayRate, t),
                stretch = lerp(start.stretch, stop.stretch, t),
                glow = lerp(start.glow, stop.glow, t),
            )
        }
    }
}

internal fun lerp(start: Float, stop: Float, fraction: Float): Float = start + (stop - start) * fraction
