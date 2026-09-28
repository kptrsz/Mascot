package com.kptrsz.mascot.core

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MascotControllerTest {
    @Test
    fun setMoodUpdatesState() {
        val controller = MascotController(MascotEmotion.Calm)
        assertEquals(MascotEmotion.Calm, controller.state.value.mood)
        controller.setMood(MascotEmotion.Happy)
        assertEquals(MascotEmotion.Happy, controller.state.value.mood)
    }

    @Test
    fun reactionsReachActiveCollectors() = runTest {
        val controller = MascotController()
        val received = mutableListOf<MascotReaction>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { controller.reactions.collect { received += it } }

        controller.react(MascotEmotion.Excited, durationMillis = 300)
        controller.react(MascotReaction(MascotEmotion.Surprised))

        assertEquals(
            listOf(MascotReaction(MascotEmotion.Excited, 300), MascotReaction(MascotEmotion.Surprised)),
            received,
        )
        job.cancel()
    }

    @Test
    fun reactionsAreNotReplayedToLateCollectors() = runTest {
        val controller = MascotController()
        controller.react(MascotEmotion.Excited)

        val received = mutableListOf<MascotReaction>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) { controller.reactions.collect { received += it } }
        controller.react(MascotEmotion.Happy)

        assertEquals(listOf(MascotReaction(MascotEmotion.Happy)), received)
        job.cancel()
    }

    @Test
    fun moodIsReplayedToLateCollectors() = runTest {
        val controller = MascotController()
        controller.setMood(MascotEmotion.Sleepy)
        assertEquals(MascotEmotion.Sleepy, controller.state.first().mood)
    }

    @Test
    fun rejectsNonPositiveReactionDuration() {
        assertFailsWith<IllegalArgumentException> { MascotReaction(MascotEmotion.Happy, durationMillis = 0) }
    }
}
