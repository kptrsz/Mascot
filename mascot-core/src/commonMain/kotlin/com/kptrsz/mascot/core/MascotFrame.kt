package com.kptrsz.mascot.core

/**
 * Renderer-agnostic snapshot of the mascot for a single frame.
 *
 * Coordinates are in *unit space*: the ground contact point is the origin, x grows right,
 * y grows down, and the resting body has a radius of roughly `1`. Renderers scale by
 * their own radius and apply [scaleX]/[scaleY]/[rotationDegrees] around the origin, then
 * translate up by [lift].
 *
 * Instances are reused by [MascotAnimator] between steps: read it during the frame, never retain it.
 */
public interface MascotFrame {
    /** Number of outline points. */
    public val pointCount: Int

    /** Interleaved `x0, y0, x1, y1, …` closed outline, `2 * pointCount` long. Do not mutate. */
    public val outline: FloatArray

    public val scaleX: Float
    public val scaleY: Float
    public val rotationDegrees: Float

    /** Height above ground in unit space (≥ 0). */
    public val lift: Float

    /** Glow intensity, `0..1`. */
    public val glow: Float

    public val palette: MascotPalette

    /** Emotion currently being expressed (a reaction if one is playing, otherwise the mood). */
    public val emotion: MascotEmotion
}

internal class MutableMascotFrame(pointCount: Int, palette: MascotPalette, emotion: MascotEmotion) : MascotFrame {
    override var pointCount: Int = pointCount
        private set
    override var outline: FloatArray = FloatArray(pointCount * 2)
        private set
    override var scaleX: Float = 1f
    override var scaleY: Float = 1f
    override var rotationDegrees: Float = 0f
    override var lift: Float = 0f
    override var glow: Float = 0f
    override var palette: MascotPalette = palette
    override var emotion: MascotEmotion = emotion

    fun resize(pointCount: Int) {
        if (pointCount == this.pointCount) return
        this.pointCount = pointCount
        outline = FloatArray(pointCount * 2)
    }
}
