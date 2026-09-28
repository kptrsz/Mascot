package com.kptrsz.mascot.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MascotAnimatorTest {
    private val frameDelta = 1f / 60f

    private fun MascotAnimator.run(seconds: Float) {
        repeat((seconds / frameDelta).toInt()) { step(frameDelta) }
    }

    private fun MascotFrame.xExtent(): Float = (0 until pointCount).maxOf { outline[2 * it] } - (0 until pointCount).minOf { outline[2 * it] }
    private fun MascotFrame.yExtent(): Float = (0 until pointCount).maxOf { outline[2 * it + 1] } - (0 until pointCount).minOf { outline[2 * it + 1] }

    @Test
    fun isDeterministicForIdenticalInputs() {
        val first = MascotAnimator(initialMood = MascotEmotion.Excited)
        val second = MascotAnimator(initialMood = MascotEmotion.Excited)
        first.run(2.5f)
        second.run(2.5f)
        assertContentEquals(first.frame.outline, second.frame.outline)
        assertEquals(first.frame.scaleY, second.frame.scaleY)
        assertEquals(first.frame.lift, second.frame.lift)
    }

    @Test
    fun outlineMatchesShapeResolution() {
        val animator = MascotAnimator(MascotAppearance(shape = MascotShape(resolution = 32)))
        assertEquals(32, animator.frame.pointCount)
        assertEquals(64, animator.frame.outline.size)
    }

    @Test
    fun changingShapeResizesOutline() {
        val animator = MascotAnimator()
        animator.appearance = animator.appearance.copy(shape = MascotShape(resolution = 100))
        animator.step(frameDelta)
        assertEquals(100, animator.frame.pointCount)
        assertEquals(200, animator.frame.outline.size)
    }

    @Test
    fun startsAtInitialMoodWithoutTransition() {
        val animator = MascotAnimator(initialMood = MascotEmotion.Sad)
        assertEquals(MotionProfiles.Default.profileFor(MascotEmotion.Sad), animator.currentProfile)
    }

    @Test
    fun blendsToNewMoodWithinTransitionTime() {
        val appearance = MascotAppearance(transitionSeconds = 0.5f)
        val animator = MascotAnimator(appearance, MascotEmotion.Calm)
        animator.setMood(MascotEmotion.Excited)
        animator.step(frameDelta)
        val target = appearance.motionProfiles.profileFor(MascotEmotion.Excited)
        assertTrue(animator.currentProfile.bounceHeight < target.bounceHeight * 0.5f, "should not jump instantly")

        animator.run(1.5f)
        assertEquals(target.bounceHeight, animator.currentProfile.bounceHeight, 1e-3f)
        assertEquals(target.glow, animator.currentProfile.glow, 1e-3f)
    }

    @Test
    fun zeroTransitionSnapsImmediately() {
        val animator = MascotAnimator(MascotAppearance(transitionSeconds = 0f), MascotEmotion.Calm)
        animator.setMood(MascotEmotion.Happy)
        animator.step(frameDelta)
        assertEquals(MotionProfiles.Default.profileFor(MascotEmotion.Happy), animator.currentProfile)
    }

    @Test
    fun reactionPlaysThenReturnsToMood() {
        val animator = MascotAnimator(initialMood = MascotEmotion.Calm)
        animator.play(MascotReaction(MascotEmotion.Excited, durationMillis = 500))
        animator.step(frameDelta)
        assertEquals(MascotEmotion.Excited, animator.activeEmotion)
        assertEquals(MascotEmotion.Excited, animator.frame.emotion)

        animator.run(0.6f)
        assertEquals(MascotEmotion.Calm, animator.activeEmotion)
        assertEquals(MascotEmotion.Calm, animator.mood)
    }

    @Test
    fun newReactionReplacesRunningOne() {
        val animator = MascotAnimator()
        animator.play(MascotReaction(MascotEmotion.Happy, durationMillis = 1000))
        animator.run(0.5f)
        animator.play(MascotReaction(MascotEmotion.Surprised, durationMillis = 1000))
        animator.run(0.8f)
        assertEquals(MascotEmotion.Surprised, animator.activeEmotion)
    }

    @Test
    fun zeroSpeedFreezesMotion() {
        val animator = MascotAnimator(MascotAppearance(speed = 0f), MascotEmotion.Excited)
        animator.step(frameDelta)
        val before = animator.frame.outline.copyOf()
        animator.run(1f)
        assertContentEquals(before, animator.frame.outline)
        assertEquals(0f, animator.frame.lift)
    }

    @Test
    fun movesWhenSpeedIsPositive() {
        val animator = MascotAnimator(initialMood = MascotEmotion.Excited)
        val before = animator.frame.outline.copyOf()
        animator.run(0.3f)
        assertFalse(before.contentEquals(animator.frame.outline))
        assertTrue(animator.frame.lift > 0f)
    }

    @Test
    fun calmMoodNeverLeavesTheGround() {
        val animator = MascotAnimator(initialMood = MascotEmotion.Calm)
        repeat(600) {
            animator.step(frameDelta)
            assertEquals(0f, animator.frame.lift)
        }
    }

    @Test
    fun aspectRatioWidensOutline() {
        val round = MascotAnimator(MascotAppearance(shape = MascotShape.Orb))
        val wide = MascotAnimator(MascotAppearance(shape = MascotShape.Orb.copy(aspectRatio = 1.6f)))
        val roundRatio = round.frame.xExtent() / round.frame.yExtent()
        val wideRatio = wide.frame.xExtent() / wide.frame.yExtent()
        assertEquals(1f, roundRatio, 0.08f)
        assertEquals(1.6f, wideRatio, 0.15f)
    }

    @Test
    fun outlineRestsOnGround() {
        val animator = MascotAnimator(MascotAppearance(shape = MascotShape.Orb))
        val bottom = (0 until animator.frame.pointCount).maxOf { animator.frame.outline[2 * it + 1] }
        assertEquals(0f, bottom, 0.1f)
    }

    @Test
    fun stretchPreservesArea() {
        val animator = MascotAnimator(MascotAppearance(speed = 0f), MascotEmotion.Sleepy)
        assertEquals(1f, animator.frame.scaleX * animator.frame.scaleY, 1e-4f)
        assertTrue(animator.frame.scaleY < 1f, "sleepy should be flattened")
    }

    @Test
    fun clampsLargeDeltas() {
        val clamped = MascotAnimator(initialMood = MascotEmotion.Happy)
        val reference = MascotAnimator(initialMood = MascotEmotion.Happy)
        clamped.step(10f)
        reference.step(MascotAnimator.MAX_STEP_SECONDS)
        assertContentEquals(reference.frame.outline, clamped.frame.outline)
    }

    @Test
    fun paletteBlendsToEmotionPalette() {
        val sad = MascotAppearance.EmotionalPalettes.getValue(MascotEmotion.Sad)
        val animator = MascotAnimator(MascotAppearance.Default, MascotEmotion.Neutral)
        animator.setMood(MascotEmotion.Sad)
        animator.run(3f)
        assertEquals(sad, animator.frame.palette)
    }
}
