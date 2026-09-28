package com.kptrsz.mascot.compose

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import com.kptrsz.mascot.core.MascotAnimator
import com.kptrsz.mascot.core.MascotAppearance
import com.kptrsz.mascot.core.MascotController
import com.kptrsz.mascot.core.MascotEmotion
import kotlinx.coroutines.launch

/**
 * Renders the mascot driven by a [MascotController], typically owned by a ViewModel.
 *
 * @param appearance Shape, colors, speed and body language. Changes apply live without restarting motion.
 * @param contentDescription Accessibility label. `null` treats the mascot as decorative.
 * @param showGroundShadow Draws a contact shadow that shrinks while hopping.
 */
@Composable
public fun Mascot(
    controller: MascotController,
    modifier: Modifier = Modifier,
    appearance: MascotAppearance = MascotAppearance.Default,
    contentDescription: String? = null,
    showGroundShadow: Boolean = true,
) {
    val animator = remember(controller) { MascotAnimator(appearance, controller.state.value.mood) }
    LaunchedEffect(controller, animator) {
        launch { controller.state.collect { animator.setMood(it.mood) } }
        controller.reactions.collect { animator.play(it) }
    }
    MascotCanvas(animator, appearance, modifier, contentDescription, showGroundShadow)
}

/**
 * Stateless convenience overload: renders the mascot in [mood] (e.g. straight from a UI state).
 * Use the [MascotController] overload when you also need transient reactions.
 */
@Composable
public fun Mascot(
    mood: MascotEmotion,
    modifier: Modifier = Modifier,
    appearance: MascotAppearance = MascotAppearance.Default,
    contentDescription: String? = null,
    showGroundShadow: Boolean = true,
) {
    val animator = remember { MascotAnimator(appearance, mood) }
    SideEffect { animator.setMood(mood) }
    MascotCanvas(animator, appearance, modifier, contentDescription, showGroundShadow)
}

/** Creates a [MascotController] scoped to the composition. Prefer a ViewModel-owned one for screen state. */
@Composable
public fun rememberMascotController(initialMood: MascotEmotion = MascotEmotion.Neutral): MascotController =
    remember { MascotController(initialMood) }

@Composable
private fun MascotCanvas(
    animator: MascotAnimator,
    appearance: MascotAppearance,
    modifier: Modifier,
    contentDescription: String?,
    showGroundShadow: Boolean,
) {
    SideEffect { animator.appearance = appearance }

    // Written every frame but only read inside the draw lambda, so it invalidates drawing, not composition.
    var frameTimeNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(animator) {
        var previous = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                animator.step((now - previous) / NANOS_PER_SECOND)
                previous = now
                frameTimeNanos = now
            }
        }
    }

    val renderer = remember { MascotRenderer() }
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            role = Role.Image
        }
    } else {
        Modifier
    }

    Canvas(modifier.then(semanticsModifier)) {
        frameTimeNanos // Subscribe to frame ticks.
        with(renderer) { draw(animator.frame, showGroundShadow) }
    }
}

private const val NANOS_PER_SECOND = 1_000_000_000f
