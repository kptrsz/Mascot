package com.kptrsz.mascot.core

/**
 * Everything the host app can tune about how the mascot looks and moves.
 *
 * @property shape Resting silhouette.
 * @property palette Colors used when no emotion-specific palette exists.
 * @property emotionPalettes Optional per-emotion colors; missing emotions fall back to [palette].
 * @property speed Global tempo multiplier applied to every motion rate. `0` freezes motion, `1` is the tuned default.
 * @property motionProfiles Emotion → motion mapping.
 * @property transitionSeconds Approximate time to blend fully between two emotions.
 */
public data class MascotAppearance(
    val shape: MascotShape = MascotShape.Blob,
    val palette: MascotPalette = DefaultPalette,
    val emotionPalettes: Map<MascotEmotion, MascotPalette> = emptyMap(),
    val speed: Float = 1f,
    val motionProfiles: MotionProfiles = MotionProfiles.Default,
    val transitionSeconds: Float = 0.6f,
) {
    init {
        require(speed in 0f..5f) { "speed must be in 0..5, was $speed" }
        require(transitionSeconds >= 0f) { "transitionSeconds must be non-negative, was $transitionSeconds" }
    }

    public fun paletteFor(emotion: MascotEmotion): MascotPalette = emotionPalettes[emotion] ?: palette

    public companion object {
        public val DefaultPalette: MascotPalette = MascotPalette(
            body = MascotColor(0xFF6C5CE7),
            highlight = MascotColor(0xFFA29BFE),
            glow = MascotColor(0xFFB8B0FF),
        )

        /** Opinionated per-emotion tints layered on [DefaultPalette]. */
        public val EmotionalPalettes: Map<MascotEmotion, MascotPalette> = mapOf(
            MascotEmotion.Happy to MascotPalette(MascotColor(0xFF7B61FF), MascotColor(0xFFC3B5FF), MascotColor(0xFFFFD7A8)),
            MascotEmotion.Excited to MascotPalette(MascotColor(0xFF8E5CFF), MascotColor(0xFFD9C2FF), MascotColor(0xFFFFC46B)),
            MascotEmotion.Sad to MascotPalette(MascotColor(0xFF5468B8), MascotColor(0xFF8E9BD1), MascotColor(0xFF8FA3D9)),
            MascotEmotion.Sleepy to MascotPalette(MascotColor(0xFF6A649A), MascotColor(0xFF9A95C2), MascotColor(0xFF9A95C2)),
        )

        public val Default: MascotAppearance = MascotAppearance(emotionPalettes = EmotionalPalettes)
    }
}
