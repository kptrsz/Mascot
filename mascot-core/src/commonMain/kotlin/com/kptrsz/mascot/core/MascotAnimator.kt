package com.kptrsz.mascot.core

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Deterministic simulation that turns an emotion and an [appearance] into [MascotFrame]s.
 *
 * It is driven purely by [step] with an explicit time delta: no clocks, coroutines or
 * platform APIs, so it can be unit-tested frame by frame and reused by any renderer.
 * Phases are integrated (not computed from absolute time), which keeps motion continuous
 * when rates change mid-animation.
 *
 * Not thread-safe: confine it to the thread that renders.
 */
public class MascotAnimator(
    appearance: MascotAppearance = MascotAppearance.Default,
    initialMood: MascotEmotion = MascotEmotion.Neutral,
) {
    public var appearance: MascotAppearance = appearance
        set(value) {
            if (value.shape != field.shape) {
                harmonics = ShapeHarmonics.from(value.shape)
                wobblePhases = FloatArray(harmonics.size)
                mutableFrame.resize(value.shape.resolution)
            }
            field = value
        }

    /** Long-lived emotional state the mascot returns to after reactions. */
    public var mood: MascotEmotion = initialMood
        private set

    private var harmonics = ShapeHarmonics.from(appearance.shape)
    private var wobblePhases = FloatArray(harmonics.size)
    private var breathPhase = 0f
    private var bouncePhase = 0f
    private var swayPhase = 0f

    private var reaction: MascotReaction? = null
    private var reactionElapsedSeconds = 0f

    internal var currentProfile: MotionProfile = appearance.motionProfiles.profileFor(initialMood)
        private set
    // Colors are 8-bit per channel, so incremental smoothing would stall on rounding.
    // Instead blend from a snapshot towards the target with float progress.
    private var currentPalette: MascotPalette = appearance.paletteFor(initialMood)
    private var paletteFrom: MascotPalette = currentPalette
    private var paletteTo: MascotPalette = currentPalette
    private var paletteProgress = 1f

    private val mutableFrame = MutableMascotFrame(appearance.shape.resolution, currentPalette, initialMood)

    /** The most recently computed frame. Updated in place by [step]. */
    public val frame: MascotFrame get() = mutableFrame

    /** Emotion the mascot is currently blending towards. */
    public val activeEmotion: MascotEmotion get() = reaction?.emotion ?: mood

    init {
        writeFrame()
    }

    public fun setMood(emotion: MascotEmotion) {
        mood = emotion
    }

    /** Plays [reaction] on top of the mood, replacing any reaction in progress. */
    public fun play(reaction: MascotReaction) {
        this.reaction = reaction
        reactionElapsedSeconds = 0f
    }

    /**
     * Advances the simulation by [deltaSeconds] and updates [frame].
     * Deltas are clamped to [MAX_STEP_SECONDS] so resuming after a pause doesn't cause a jump.
     */
    public fun step(deltaSeconds: Float) {
        val dt = deltaSeconds.coerceIn(0f, MAX_STEP_SECONDS)
        advanceReaction(dt)
        blendTowardsTarget(dt)
        advancePhases(dt)
        writeFrame()
    }

    private fun advanceReaction(dt: Float) {
        val active = reaction ?: return
        reactionElapsedSeconds += dt
        if (reactionElapsedSeconds * 1000f >= active.durationMillis) reaction = null
    }

    private fun blendTowardsTarget(dt: Float) {
        val emotion = activeEmotion
        val targetProfile = appearance.motionProfiles.profileFor(emotion)
        val targetPalette = appearance.paletteFor(emotion)
        val transition = appearance.transitionSeconds
        // Frame-rate independent exponential smoothing; ~95 % converged after `transitionSeconds`.
        val fraction = if (transition <= 0f) 1f else 1f - exp(-dt * 3f / transition)
        currentProfile = MotionProfile.lerp(currentProfile, targetProfile, fraction)
        if (targetPalette != paletteTo) {
            paletteFrom = currentPalette
            paletteTo = targetPalette
            paletteProgress = 0f
        }
        paletteProgress = lerp(paletteProgress, 1f, fraction).let { if (it > PALETTE_SNAP) 1f else it }
        currentPalette = MascotPalette.lerp(paletteFrom, paletteTo, paletteProgress)
    }

    private fun advancePhases(dt: Float) {
        val profile = currentProfile
        val scaledDt = dt * appearance.speed
        breathPhase = wrap(breathPhase + TWO_PI * profile.breathRate * scaledDt)
        swayPhase = wrap(swayPhase + TWO_PI * profile.swayRate * scaledDt)
        // |sin| has period π, so one hop per π radians.
        bouncePhase = wrap(bouncePhase + HALF_TURN * profile.bounceRate * scaledDt)
        for (i in wobblePhases.indices) {
            wobblePhases[i] = wrap(wobblePhases[i] + TWO_PI * profile.wobbleRate * harmonics.wobbleRateMultipliers[i] * scaledDt)
        }
    }

    private fun writeFrame() {
        val profile = currentProfile
        val shape = appearance.shape
        val frame = mutableFrame

        // Area-preserving aspect ratio.
        val aspectX = sqrt(shape.aspectRatio)
        val aspectY = 1f / aspectX

        val points = frame.pointCount
        val outline = frame.outline
        for (p in 0 until points) {
            val theta = TWO_PI * p / points
            // y grows down, so sin(theta) > 0 is the bottom half.
            var radius = 1f + shape.bottomWeight * 0.5f * sin(theta)
            for (h in 0 until harmonics.size) {
                val order = harmonics.orders[h]
                radius += harmonics.staticAmplitudes[h] * sin(order * theta + harmonics.staticPhases[h])
                radius += profile.wobbleAmplitude * harmonics.wobbleWeights[h] * sin(order * theta + wobblePhases[h])
            }
            outline[2 * p] = radius * cos(theta) * aspectX
            // Shift up by 1 so the ground contact point sits at the origin.
            outline[2 * p + 1] = radius * sin(theta) * aspectY - 1f
        }

        val hop = abs(sin(bouncePhase))
        // Squash is proportional to how much the mascot is actually hopping.
        val squash = profile.squashStretch * (profile.bounceHeight / FULL_SQUASH_BOUNCE).coerceAtMost(1f)
        val landingCompression = (1f - hop).let { it * it * it * it }
        val bounceStretch = 1f + squash * (AIR_STRETCH * hop - LANDING_SQUASH * landingCompression)
        val breath = 1f + profile.breathAmplitude * sin(breathPhase)
        val stretchY = (1f + profile.stretch) * bounceStretch

        frame.scaleY = stretchY * breath
        frame.scaleX = breath / stretchY
        frame.rotationDegrees = profile.swayDegrees * sin(swayPhase)
        frame.lift = profile.bounceHeight * hop
        frame.glow = profile.glow
        frame.palette = currentPalette
        frame.emotion = activeEmotion
    }

    /** Keeps phases small to preserve float precision. 2π is also a multiple of the π hop period. */
    private fun wrap(phase: Float): Float {
        val wrapped = phase % TWO_PI
        return if (wrapped < 0f) wrapped + TWO_PI else wrapped
    }

    public companion object {
        public const val MAX_STEP_SECONDS: Float = 0.1f

        private const val HALF_TURN = TWO_PI / 2f
        private const val PALETTE_SNAP = 0.999f
        private const val FULL_SQUASH_BOUNCE = 0.3f
        private const val AIR_STRETCH = 0.12f
        private const val LANDING_SQUASH = 0.2f
    }
}
