package com.android.example.camx

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for [PushUpCounter].
 *
 * Tests verify:
 * - The state machine transitions correctly through WAITING_FOR_DOWN → DOWN → UP
 * - Rep count increments at the right moment
 * - Form feedback returns the correct message for each condition
 * - [reset] restores all state to initial values
 *
 * Landmarks are mocked with hardcoded x/y values that produce known elbow
 * angles and body alignment deviations, calculated by hand to match the
 * thresholds defined in [PushUpCounter.Companion].
 */
@DisplayName("PushUpCounter")
class PushUpCounterTest {

    private lateinit var counter: PushUpCounter

    @BeforeEach
    fun setup() {
        counter = PushUpCounter()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun lm(x: Float, y: Float): NormalizedLandmark =
        NormalizedLandmark.create(x, y, 0f)

    /**
     * Builds a 33-landmark list simulating the top of a push-up.
     *
     * Elbow angle ~170° (nearly straight arms).
     * Body is horizontal and aligned (no sag or pike).
     *
     * Layout (side view, person facing right):
     * - Shoulders at x=0.3, Elbows at x=0.5, Wrists at x=0.7 (straight arms)
     * - All at y=0.5 (horizontal body)
     */
    private fun topPosition(): List<NormalizedLandmark> {
        val neutral = lm(0.5f, 0.5f)
        val list = MutableList(33) { neutral }
        // Straight arms → high elbow angle (~170°)
        list[11] = lm(0.3f, 0.5f) // left shoulder
        list[12] = lm(0.7f, 0.5f) // right shoulder
        list[13] = lm(0.4f, 0.5f) // left elbow
        list[14] = lm(0.6f, 0.5f) // right elbow
        list[15] = lm(0.5f, 0.5f) // left wrist
        list[16] = lm(0.5f, 0.5f) // right wrist
        // Hips at midpoint between shoulders and ankles → no sag/pike
        list[23] = lm(0.3f, 0.5f) // left hip
        list[24] = lm(0.7f, 0.5f) // right hip
        list[27] = lm(0.3f, 0.5f) // left ankle
        list[28] = lm(0.7f, 0.5f) // right ankle
        return list
    }

    /**
     * Builds a 33-landmark list simulating the bottom of a push-up.
     *
     * Elbow angle ~80° (arms bent well below 90°).
     * Body remains horizontal and aligned.
     *
     * Layout:
     * - Shoulders far from wrists, elbows bent sharply inward
     */
    private fun bottomPosition(): List<NormalizedLandmark> {
        val neutral = lm(0.5f, 0.5f)
        val list = MutableList(33) { neutral }
        // Bent arms → low elbow angle (~80°)
        list[11] = lm(0.2f, 0.5f) // left shoulder
        list[12] = lm(0.8f, 0.5f) // right shoulder
        list[13] = lm(0.3f, 0.6f) // left elbow (bent down)
        list[14] = lm(0.7f, 0.6f) // right elbow (bent down)
        list[15] = lm(0.2f, 0.7f) // left wrist
        list[16] = lm(0.8f, 0.7f) // right wrist
        // Hips aligned
        list[23] = lm(0.3f, 0.5f)
        list[24] = lm(0.7f, 0.5f)
        list[27] = lm(0.3f, 0.5f)
        list[28] = lm(0.7f, 0.5f)
        return list
    }

    /**
     * Builds a 33-landmark list with sagging hips.
     *
     * Shoulders at y=0.2, ankles at y=0.8 → expected hip Y = 0.5.
     * Actual hip Y = 0.7 → deviation = +0.2 (well above SAG_THRESHOLD of 0.05).
     */
    private fun sagPosition(): List<NormalizedLandmark> {
        val list = topPosition().toMutableList()
        list[11] = lm(0.3f, 0.2f) // shoulders high
        list[12] = lm(0.7f, 0.2f)
        list[23] = lm(0.3f, 0.7f) // hips low (sagging)
        list[24] = lm(0.7f, 0.7f)
        list[27] = lm(0.3f, 0.8f) // ankles
        list[28] = lm(0.7f, 0.8f)
        return list
    }

    /**
     * Builds a 33-landmark list with piked hips.
     *
     * Shoulders at y=0.6, ankles at y=0.8 → expected hip Y = 0.7.
     * Actual hip Y = 0.3 → deviation = -0.4 (well below PIKE_THRESHOLD of -0.05).
     */
    private fun pikePosition(): List<NormalizedLandmark> {
        val list = topPosition().toMutableList()
        list[11] = lm(0.3f, 0.6f) // shoulders
        list[12] = lm(0.7f, 0.6f)
        list[23] = lm(0.3f, 0.3f) // hips high (piking)
        list[24] = lm(0.7f, 0.3f)
        list[27] = lm(0.3f, 0.8f) // ankles
        list[28] = lm(0.7f, 0.8f)
        return list
    }

    // ── Initial State ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Initial state")
    inner class InitialState {

        @Test
        @DisplayName("starts with 0 reps")
        fun zeroReps() {
            assertEquals(0, counter.repCount)
        }

        @Test
        @DisplayName("starts in WAITING_FOR_DOWN phase")
        fun waitingForDownPhase() {
            assertEquals(PushUpCounter.Phase.WAITING_FOR_DOWN, counter.currentPhase)
        }

        @Test
        @DisplayName("starts with empty form feedback")
        fun emptyFeedback() {
            assertEquals("", counter.formFeedback)
        }
    }

    // ── State Machine ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("State machine transitions")
    inner class StateMachine {

        @Test
        @DisplayName("transitions to DOWN when elbow angle drops below threshold")
        fun transitionsToDown() {
            counter.update(bottomPosition())
            assertEquals(PushUpCounter.Phase.DOWN, counter.currentPhase)
        }

        @Test
        @DisplayName("transitions to UP after going DOWN then straightening arms")
        fun transitionsToUp() {
            counter.update(bottomPosition()) // → DOWN
            counter.update(topPosition())    // → UP
            assertEquals(PushUpCounter.Phase.UP, counter.currentPhase)
        }

        @Test
        @DisplayName("does not count rep from top position alone")
        fun noRepFromTopOnly() {
            repeat(5) { counter.update(topPosition()) }
            assertEquals(0, counter.repCount)
            assertEquals(PushUpCounter.Phase.WAITING_FOR_DOWN, counter.currentPhase)
        }

        @Test
        @DisplayName("does not count rep from bottom position alone")
        fun noRepFromBottomOnly() {
            repeat(5) { counter.update(bottomPosition()) }
            assertEquals(0, counter.repCount)
            assertEquals(PushUpCounter.Phase.DOWN, counter.currentPhase)
        }

        @Test
        @DisplayName("counts 1 rep after full DOWN then UP cycle")
        fun countsOneRep() {
            counter.update(bottomPosition()) // → DOWN
            counter.update(topPosition())    // → UP, rep counted
            assertEquals(1, counter.repCount)
        }

        @Test
        @DisplayName("counts 3 reps after 3 full DOWN → UP cycles")
        fun countsThreeReps() {
            repeat(3) {
                counter.update(bottomPosition())
                counter.update(topPosition())
            }
            assertEquals(3, counter.repCount)
        }

        @Test
        @DisplayName("resets to WAITING_FOR_DOWN after UP when arms start bending again")
        fun resetsAfterUp() {
            counter.update(bottomPosition()) // → DOWN
            counter.update(topPosition())    // → UP
            counter.update(bottomPosition()) // → WAITING_FOR_DOWN → DOWN
            assertEquals(PushUpCounter.Phase.DOWN, counter.currentPhase)
        }
    }

    // ── Form Feedback ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Form feedback")
    inner class FormFeedback {

        @Test
        @DisplayName("warns about hip sag when hips are below the shoulder-ankle line")
        fun hipSagWarning() {
            val result = counter.update(sagPosition())
            assertTrue(result.feedback.contains("sag", ignoreCase = true),
                "Expected sag warning but got: ${result.feedback}")
        }

        @Test
        @DisplayName("warns about hip pike when hips are above the shoulder-ankle line")
        fun hipPikeWarning() {
            val result = counter.update(pikePosition())
            assertTrue(result.feedback.contains("high", ignoreCase = true)
                    || result.feedback.contains("pike", ignoreCase = true),
                "Expected pike warning but got: ${result.feedback}")
        }

        @Test
        @DisplayName("gives positive feedback at the bottom with good form")
        fun positiveFeedbackAtBottom() {
            val result = counter.update(bottomPosition())
            assertTrue(result.feedback.contains("Good", ignoreCase = true)
                    || result.feedback.contains("push", ignoreCase = true),
                "Expected positive feedback at bottom but got: ${result.feedback}")
        }

        @Test
        @DisplayName("gives positive feedback at the top after a good rep")
        fun positiveFeedbackAtTop() {
            counter.update(bottomPosition()) // → DOWN
            val result = counter.update(topPosition()) // → UP
            assertTrue(result.feedback.contains("Good", ignoreCase = true)
                    || result.feedback.contains("rep", ignoreCase = true),
                "Expected positive rep feedback but got: ${result.feedback}")
        }
    }

    // ── CoachResult ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("CoachResult")
    inner class CoachResultTests {

        @Test
        @DisplayName("result repCount matches counter repCount")
        fun repCountInResult() {
            counter.update(bottomPosition())
            val result = counter.update(topPosition())
            assertEquals(counter.repCount, result.repCount)
        }

        @Test
        @DisplayName("result phaseLabel is non-empty")
        fun phaseLabelNonEmpty() {
            val result = counter.update(topPosition())
            assertTrue(result.phaseLabel.isNotBlank(),
                "phaseLabel should never be blank")
        }

        @Test
        @DisplayName("result primaryAngle is a valid angle in [0, 180]")
        fun primaryAngleInRange() {
            val result = counter.update(topPosition())
            assertTrue(result.primaryAngle in 0.0..180.0,
                "primaryAngle ${result.primaryAngle} is out of [0, 180] range")
        }

        @Test
        @DisplayName("returns safe default result when landmark list is too short")
        fun safeDefaultForShortLandmarks() {
            val result = counter.update(emptyList())
            assertEquals(0, result.repCount)
            assertTrue(result.feedback.isNotBlank())
        }
    }

    // ── Reset ─────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("reset()")
    inner class Reset {

        @Test
        @DisplayName("resets repCount to 0")
        fun resetsRepCount() {
            counter.update(bottomPosition())
            counter.update(topPosition())
            assertEquals(1, counter.repCount)
            counter.reset()
            assertEquals(0, counter.repCount)
        }

        @Test
        @DisplayName("resets phase to WAITING_FOR_DOWN")
        fun resetsPhase() {
            counter.update(bottomPosition())
            assertEquals(PushUpCounter.Phase.DOWN, counter.currentPhase)
            counter.reset()
            assertEquals(PushUpCounter.Phase.WAITING_FOR_DOWN, counter.currentPhase)
        }

        @Test
        @DisplayName("resets formFeedback to empty string")
        fun resetsFeedback() {
            counter.update(bottomPosition())
            counter.reset()
            assertEquals("", counter.formFeedback)
        }

        @Test
        @DisplayName("can count reps again after reset")
        fun canCountAfterReset() {
            counter.update(bottomPosition())
            counter.update(topPosition())
            counter.reset()
            counter.update(bottomPosition())
            counter.update(topPosition())
            assertEquals(1, counter.repCount)
        }
    }
}