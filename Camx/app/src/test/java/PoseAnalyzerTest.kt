package com.android.example.camx

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for [PoseAnalyzer].
 *
 * All tests use manually constructed [NormalizedLandmark] objects with known
 * x/y coordinates so that expected angles and deviations can be calculated
 * by hand and verified against the implementation.
 *
 * No Android device or MediaPipe runtime is required — [PoseAnalyzer] is
 * pure Kotlin math with no Android dependencies.
 */
@DisplayName("PoseAnalyzer")
class PoseAnalyzerTest {

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Creates a [NormalizedLandmark] with the given coordinates.
     * z and visibility are left at defaults (0f / empty) since
     * [PoseAnalyzer] only uses x and y.
     */
    private fun lm(x: Float, y: Float): NormalizedLandmark =
        NormalizedLandmark.create(x, y, 0f)

    /** Asserts two doubles are equal within a tolerance of [delta] degrees. */
    private fun assertAngle(expected: Double, actual: Double, delta: Double = 1.0) {
        assertEquals(expected, actual, delta,
            "Expected angle ~$expected° but got $actual°")
    }

    // ── getAngle() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAngle()")
    inner class GetAngle {

        @Test
        @DisplayName("returns 90° for a perfect right angle")
        fun rightAngle() {
            // a is directly above b, c is directly to the right of b
            // Forms a perfect L-shape → 90°
            val a = lm(0f, 0f) // top
            val b = lm(0f, 1f) // corner (joint)
            val c = lm(1f, 1f) // right
            assertAngle(90.0, PoseAnalyzer.getAngle(a, b, c))
        }

        @Test
        @DisplayName("returns 180° for a perfectly straight line")
        fun straightLine() {
            // a, b, c all on the same horizontal line → fully extended joint
            val a = lm(0f, 0.5f)
            val b = lm(0.5f, 0.5f)
            val c = lm(1f, 0.5f)
            assertAngle(180.0, PoseAnalyzer.getAngle(a, b, c))
        }

        @Test
        @DisplayName("returns 0° when all three points are at the same location")
        fun zeroAngle() {
            val p = lm(0.5f, 0.5f)
            // All at same point — degenerate case, angle should be 0
            assertAngle(0.0, PoseAnalyzer.getAngle(p, p, p), delta = 2.0)
        }

        @Test
        @DisplayName("returns ~45° for a 45-degree angle")
        fun fortyFiveDegrees() {
            // b is at origin, a is directly above, c is diagonal
            val a = lm(0f, 0f)   // straight up from b
            val b = lm(0f, 1f)   // joint
            val c = lm(1f, 0f)   // diagonal up-right from b
            // Angle at b between vectors b→a and b→c
            assertAngle(45.0, PoseAnalyzer.getAngle(a, b, c), delta = 2.0)
        }

        @Test
        @DisplayName("returns same angle regardless of arm order (symmetric)")
        fun symmetric() {
            val a = lm(0f, 0f)
            val b = lm(0f, 1f)
            val c = lm(1f, 1f)
            val angle1 = PoseAnalyzer.getAngle(a, b, c)
            val angle2 = PoseAnalyzer.getAngle(c, b, a)
            assertEquals(angle1, angle2, 0.001,
                "getAngle should be symmetric — order of a and c should not matter")
        }

        @Test
        @DisplayName("result is always in range [0, 180]")
        fun alwaysInRange() {
            // Try several arbitrary landmark configurations
            val cases = listOf(
                Triple(lm(0.1f, 0.9f), lm(0.5f, 0.5f), lm(0.9f, 0.1f)),
                Triple(lm(0.0f, 0.0f), lm(0.3f, 0.7f), lm(1.0f, 0.2f)),
                Triple(lm(0.8f, 0.2f), lm(0.2f, 0.8f), lm(0.5f, 0.0f)),
            )
            for ((a, b, c) in cases) {
                val angle = PoseAnalyzer.getAngle(a, b, c)
                assertTrue(angle in 0.0..180.0,
                    "Angle $angle is outside [0, 180] range")
            }
        }
    }

    // ── getAverageAngle() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAverageAngle()")
    inner class GetAverageAngle {

        @Test
        @DisplayName("returns average of two equal angles")
        fun averageOfEqualAngles() {
            // Both sides form 90° — average should be 90°
            val a = lm(0f, 0f)
            val b = lm(0f, 1f)
            val c = lm(1f, 1f)
            val avg = PoseAnalyzer.getAverageAngle(a, b, c, a, b, c)
            assertAngle(90.0, avg)
        }

        @Test
        @DisplayName("returns average of two different angles")
        fun averageOfDifferentAngles() {
            // Left side: 90° right angle
            val leftA = lm(0f, 0f)
            val leftB = lm(0f, 1f)
            val leftC = lm(1f, 1f)

            // Right side: straight line 180°
            val rightA = lm(0f, 0.5f)
            val rightB = lm(0.5f, 0.5f)
            val rightC = lm(1f, 0.5f)

            // Expected: (90 + 180) / 2 = 135°
            assertAngle(135.0, PoseAnalyzer.getAverageAngle(
                leftA, leftB, leftC, rightA, rightB, rightC))
        }
    }

    // ── getBodyAlignmentDeviation() ───────────────────────────────────────────

    @Nested
    @DisplayName("getBodyAlignmentDeviation()")
    inner class GetBodyAlignmentDeviation {

        /**
         * Builds a minimal 33-landmark list with only the indices used by
         * [PoseAnalyzer.getBodyAlignmentDeviation]: 11, 12, 23, 24, 27, 28.
         * All other indices are filled with a neutral landmark at (0.5, 0.5).
         */
        private fun buildLandmarks(
            shoulderY: Float,
            hipY: Float,
            ankleY: Float
        ): List<NormalizedLandmark> {
            val neutral = lm(0.5f, 0.5f)
            val list = MutableList(33) { neutral }
            list[11] = lm(0.3f, shoulderY) // left shoulder
            list[12] = lm(0.7f, shoulderY) // right shoulder
            list[23] = lm(0.3f, hipY)      // left hip
            list[24] = lm(0.7f, hipY)      // right hip
            list[27] = lm(0.3f, ankleY)    // left ankle
            list[28] = lm(0.7f, ankleY)    // right ankle
            return list
        }

        @Test
        @DisplayName("returns ~0 when body is perfectly straight")
        fun perfectAlignment() {
            // Shoulders at 0.1, hips at 0.5 (midpoint), ankles at 0.9
            // Hip midpoint == expected midpoint → deviation ≈ 0
            val landmarks = buildLandmarks(shoulderY = 0.1f, hipY = 0.5f, ankleY = 0.9f)
            val deviation = PoseAnalyzer.getBodyAlignmentDeviation(landmarks)
            assertEquals(0f, deviation, 0.01f,
                "Perfectly straight body should have ~0 deviation")
        }

        @Test
        @DisplayName("returns positive value when hips are sagging (below the line)")
        fun hipSag() {
            // Hips lower than the midpoint between shoulders and ankles
            // shoulderY=0.1, ankleY=0.9 → expected hip Y = 0.5
            // actual hip Y = 0.65 → deviation = 0.65 - 0.5 = +0.15 (sagging)
            val landmarks = buildLandmarks(shoulderY = 0.1f, hipY = 0.65f, ankleY = 0.9f)
            val deviation = PoseAnalyzer.getBodyAlignmentDeviation(landmarks)
            assertTrue(deviation > 0f,
                "Sagging hips should produce a positive deviation, got $deviation")
            assertEquals(0.15f, deviation, 0.01f)
        }

        @Test
        @DisplayName("returns negative value when hips are piked (above the line)")
        fun hipPike() {
            // Hips higher than the midpoint between shoulders and ankles
            // shoulderY=0.1, ankleY=0.9 → expected hip Y = 0.5
            // actual hip Y = 0.35 → deviation = 0.35 - 0.5 = -0.15 (piking)
            val landmarks = buildLandmarks(shoulderY = 0.1f, hipY = 0.35f, ankleY = 0.9f)
            val deviation = PoseAnalyzer.getBodyAlignmentDeviation(landmarks)
            assertTrue(deviation < 0f,
                "Piked hips should produce a negative deviation, got $deviation")
            assertEquals(-0.15f, deviation, 0.01f)
        }

        @Test
        @DisplayName("deviation magnitude increases the more hips deviate from the line")
        fun deviationScales() {
            val slightSag = buildLandmarks(shoulderY = 0.1f, hipY = 0.55f, ankleY = 0.9f)
            val severeSag = buildLandmarks(shoulderY = 0.1f, hipY = 0.75f, ankleY = 0.9f)

            val slightDev = PoseAnalyzer.getBodyAlignmentDeviation(slightSag)
            val severeDev = PoseAnalyzer.getBodyAlignmentDeviation(severeSag)

            assertTrue(severeDev > slightDev,
                "Larger hip deviation should produce larger positive value")
        }
    }
}