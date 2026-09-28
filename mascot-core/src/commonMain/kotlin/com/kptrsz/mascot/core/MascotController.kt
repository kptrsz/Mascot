package com.kptrsz.mascot.core

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Persistent mascot state. A data class so fields can be added without breaking consumers. */
public data class MascotState(
    val mood: MascotEmotion = MascotEmotion.Neutral,
)

/**
 * A short-lived emotional response played on top of the mood, e.g. [MascotEmotion.Excited]
 * after the user completes a goal. The mascot returns to its mood afterwards.
 */
public data class MascotReaction(
    val emotion: MascotEmotion,
    val durationMillis: Long = DEFAULT_DURATION_MILLIS,
) {
    init {
        require(durationMillis > 0) { "durationMillis must be positive, was $durationMillis" }
    }

    public companion object {
        public const val DEFAULT_DURATION_MILLIS: Long = 1_800L
    }
}

/**
 * UI-toolkit-agnostic entry point for driving the mascot, safe to hold in a ViewModel
 * or any other presentation-layer state holder.
 *
 * - [state] is persistent: the mood survives recomposition and re-subscription.
 * - [reactions] are one-off effects: they are not replayed, so a mascot that appears
 *   later (or after a configuration change) won't re-play old reactions.
 */
public class MascotController(initialMood: MascotEmotion = MascotEmotion.Neutral) {
    private val _state = MutableStateFlow(MascotState(mood = initialMood))
    public val state: StateFlow<MascotState> = _state.asStateFlow()

    private val _reactions = MutableSharedFlow<MascotReaction>(
        extraBufferCapacity = REACTION_BUFFER,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    public val reactions: SharedFlow<MascotReaction> = _reactions.asSharedFlow()

    public fun setMood(emotion: MascotEmotion) {
        _state.update { it.copy(mood = emotion) }
    }

    /** Plays a transient reaction. Dropped if nothing is currently rendering the mascot. */
    public fun react(reaction: MascotReaction) {
        _reactions.tryEmit(reaction)
    }

    public fun react(emotion: MascotEmotion, durationMillis: Long = MascotReaction.DEFAULT_DURATION_MILLIS) {
        react(MascotReaction(emotion, durationMillis))
    }

    private companion object {
        const val REACTION_BUFFER = 8
    }
}
