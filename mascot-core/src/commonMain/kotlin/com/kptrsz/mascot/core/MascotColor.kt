package com.kptrsz.mascot.core

import kotlin.jvm.JvmInline
import kotlin.math.roundToInt

/**
 * Platform-independent ARGB color, so the core module stays free of UI toolkit types.
 * Renderers convert it to their native color type.
 */
@JvmInline
public value class MascotColor(public val argb: Int) {
    public val alpha: Int get() = (argb ushr 24) and 0xFF
    public val red: Int get() = (argb ushr 16) and 0xFF
    public val green: Int get() = (argb ushr 8) and 0xFF
    public val blue: Int get() = argb and 0xFF

    public companion object {
        public fun argb(alpha: Int, red: Int, green: Int, blue: Int): MascotColor {
            require(alpha in 0..255 && red in 0..255 && green in 0..255 && blue in 0..255) {
                "components must be in 0..255"
            }
            return MascotColor((alpha shl 24) or (red shl 16) or (green shl 8) or blue)
        }

        /** Component-wise interpolation in sRGB space; [fraction] is clamped to `0..1`. */
        public fun lerp(start: MascotColor, stop: MascotColor, fraction: Float): MascotColor {
            val t = fraction.coerceIn(0f, 1f)
            fun channel(a: Int, b: Int) = (a + (b - a) * t).roundToInt()
            return argb(
                channel(start.alpha, stop.alpha),
                channel(start.red, stop.red),
                channel(start.green, stop.green),
                channel(start.blue, stop.blue),
            )
        }
    }
}

/** Creates a color from a `0xAARRGGBB` literal, e.g. `MascotColor(0xFF7C5CFF)`. */
public fun MascotColor(argb: Long): MascotColor = MascotColor(argb.toInt())
