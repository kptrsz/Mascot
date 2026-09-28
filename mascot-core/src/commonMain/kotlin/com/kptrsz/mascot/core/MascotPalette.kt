package com.kptrsz.mascot.core

/**
 * Colors used to paint the mascot.
 *
 * @property body Main body color (bottom of the body gradient).
 * @property highlight Top of the body gradient and the specular sheen.
 * @property glow Ambient halo behind the body; its strength is driven by [MotionProfile.glow].
 */
public data class MascotPalette(
    val body: MascotColor,
    val highlight: MascotColor,
    val glow: MascotColor,
) {
    public companion object {
        public fun lerp(start: MascotPalette, stop: MascotPalette, fraction: Float): MascotPalette =
            MascotPalette(
                body = MascotColor.lerp(start.body, stop.body, fraction),
                highlight = MascotColor.lerp(start.highlight, stop.highlight, fraction),
                glow = MascotColor.lerp(start.glow, stop.glow, fraction),
            )
    }
}
