package com.kptrsz.mascot.core

/**
 * Static silhouette of the mascot. The same shape always produces the same
 * silhouette (it is seeded), which keeps the companion recognizable across sessions.
 *
 * @property aspectRatio Width / height. Area is preserved when changing it.
 * @property irregularity How far the resting outline deviates from a circle, `0..0.35`.
 * @property lobes Dominant number of bulges around the outline, `2..8`.
 * @property bottomWeight Shifts volume towards the bottom for a grounded, droplet-like look, `0..0.5`.
 * @property seed Seed for the organic variation; change it to get a different individual.
 * @property resolution Outline sample count. Higher is smoother but costs more per frame.
 */
public data class MascotShape(
    val aspectRatio: Float = 1f,
    val irregularity: Float = 0.08f,
    val lobes: Int = 3,
    val bottomWeight: Float = 0.12f,
    val seed: Int = 0,
    val resolution: Int = 64,
) {
    init {
        require(aspectRatio in 0.5f..2f) { "aspectRatio must be in 0.5..2, was $aspectRatio" }
        require(irregularity in 0f..0.35f) { "irregularity must be in 0..0.35, was $irregularity" }
        require(lobes in 2..8) { "lobes must be in 2..8, was $lobes" }
        require(bottomWeight in 0f..0.5f) { "bottomWeight must be in 0..0.5, was $bottomWeight" }
        require(resolution in 16..256) { "resolution must be in 16..256, was $resolution" }
    }

    public companion object {
        /** Almost a perfect circle; the calmest, most neutral silhouette. */
        public val Orb: MascotShape = MascotShape(irregularity = 0.015f, bottomWeight = 0.05f)

        /** Soft organic blob. The default. */
        public val Blob: MascotShape = MascotShape()

        /** Heavier bottom and a slightly narrower top, like a water drop. */
        public val Droplet: MascotShape = MascotShape(aspectRatio = 0.9f, irregularity = 0.04f, lobes = 2, bottomWeight = 0.3f)

        /** Wide and puffy with many small bulges. */
        public val Cloud: MascotShape = MascotShape(aspectRatio = 1.3f, irregularity = 0.1f, lobes = 6, bottomWeight = 0.05f)
    }
}
