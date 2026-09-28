package com.kptrsz.mascot.core

/**
 * Emotional states the mascot can express.
 *
 * The mascot is faceless, so every emotion is conveyed purely through motion
 * (breathing, wobble, hops, sway), posture (stretch) and color. The mapping from
 * emotion to motion lives in [MotionProfiles] and can be replaced by the host app.
 */
public enum class MascotEmotion {
    Neutral,
    Calm,
    Happy,
    Excited,
    Curious,
    Surprised,
    Sad,
    Sleepy,
}
