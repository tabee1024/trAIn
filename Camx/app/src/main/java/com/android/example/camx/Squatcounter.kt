package com.android.example.camx

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

/**
 * Rule-based squat repetition counter and real-time form coach.
 *
 * Implements [ExerciseCounter] using knee flexion angle and additional form
 * checks (knee cave, forward lean) to count squat reps and give coaching cues.
 *
 * ## How rep counting works
 *
 * A squat rep is counted using this state machine:
 * ```
 * STANDING → DOWN (knee angle drops below threshold) → STANDING (rep counted)
 * ```
 * - Knee angle drops below [KNEE_ANGLE_DOWN] → enter [Phase.DOWN]
 * - Knee angle rises above [KNEE_ANGLE_UP]   → enter [Phase.STANDING] → increment [repCount]
 *
 * ## Landmark indices used
 *
 * | Body part  | Left | Right |
 * |------------|------|-------|
 * | Hip        | 23   | 24    |
 * | Knee       | 25   | 26    |
 * | Ankle      | 27   | 28    |
 * | Shoulder   | 11   | 12    |
 * | Foot index | 31   | 32    |
 *
 * ## Camera position
 *
 * For best results, position the camera at hip height, 6–8 feet away,
 * pointing at the person's side profile. This gives the clearest view
 * of the knee flexion angle.
 */
class SquatCounter : ExerciseCounter {

    override val exerciseName = "Squats"

    companion object {

        /**
         * Knee angle threshold (degrees) to detect the bottom of a squat.
         *
         * A parallel squat (thighs parallel to floor) produces a knee angle
         * of approximately 90°. A threshold of 100° accepts both parallel
         * and slightly-above-parallel depths without being too strict.
         */
        const val KNEE_ANGLE_DOWN = 100.0

        /**
         * Knee angle threshold (degrees) to detect the standing position.
         *
         * At full standing, the knee is nearly straight (~170°+). A threshold
         * of 160° ensures the person is close to fully upright before the
         * rep is counted, preventing partial-rep counting.
         */
        const val KNEE_ANGLE_UP = 160.0

        /**
         * Maximum allowed horizontal distance between knee and foot index
         * (in normalized coordinates) before flagging knee cave.
         *
         * When the knee tracks inward (valgus collapse), its x-position
         * moves closer to center relative to the foot. A threshold of 0.05
         * (~5% of image width) catches significant cave without false positives
         * from natural knee travel.
         */
        const val KNEE_CAVE_THRESHOLD = 0.05f

        /**
         * Maximum allowed horizontal distance between shoulder and knee
         * (in normalized coordinates) before flagging excessive forward lean.
         *
         * In a good squat the torso stays relatively upright — shoulders should
         * be roughly above the knees or slightly in front. If the shoulder
         * is significantly ahead of the knee the spine is dangerously loaded.
         */
        const val FORWARD_LEAN_THRESHOLD = 0.15f
    }

    override var repCount: Int = 0
        private set

    override var formFeedback: String = ""
        private set

    /**
     * Current movement phase of the squat state machine.
     */
    var currentPhase: Phase = Phase.STANDING
        private set

    /** Most recently computed average knee angle in degrees. */
    var currentKneeAngle: Double = 180.0
        private set

    /**
     * Represents the phase of a single squat repetition.
     */
    enum class Phase {
        /** Person is upright — waiting for them to begin descending. */
        STANDING,

        /** Knees are bent below [KNEE_ANGLE_DOWN] — person is at the bottom. */
        DOWN
    }

    /**
     * Processes a new set of pose landmarks to update the squat rep count,
     * current phase, and form feedback.
     *
     * Called once per frame. Returns early with a safe default [ExerciseCounter.CoachResult]
     * if [landmarks] has fewer than 33 entries (incomplete pose detection).
     *
     * @param landmarks The 33 normalized pose landmarks from MediaPipe.
     * @return A [ExerciseCounter.CoachResult] snapshot of the current coach state.
     */
    override fun update(landmarks: List<NormalizedLandmark>): ExerciseCounter.CoachResult {
        if (landmarks.size < 33) {
            return ExerciseCounter.CoachResult(repCount, "No pose", 0.0, "Step back so full body is visible")
        }

        // Average knee angle across both legs for stability
        currentKneeAngle = PoseAnalyzer.getAverageAngle(
            leftA  = landmarks[23], leftB  = landmarks[25], leftC  = landmarks[27],
            rightA = landmarks[24], rightB = landmarks[26], rightC = landmarks[28]
        )

        // --- Rep state machine ---
        when (currentPhase) {
            Phase.STANDING -> {
                if (currentKneeAngle < KNEE_ANGLE_DOWN) {
                    currentPhase = Phase.DOWN
                }
            }
            Phase.DOWN -> {
                if (currentKneeAngle > KNEE_ANGLE_UP) {
                    currentPhase = Phase.STANDING
                    repCount++
                }
            }
        }

        // --- Form checks ---
        formFeedback = checkForm(landmarks)

        val phaseLabel = when (currentPhase) {
            Phase.STANDING -> if (repCount == 0) "⬇ Squat down" else "⬇ Next rep"
            Phase.DOWN     -> "⬆ Drive up!"
        }

        return ExerciseCounter.CoachResult(
            repCount     = repCount,
            phaseLabel   = phaseLabel,
            primaryAngle = currentKneeAngle,
            feedback     = formFeedback
        )
    }

    /**
     * Evaluates the current pose for common squat form errors.
     *
     * Checks performed (in priority order — only the first issue is returned):
     *
     * 1. **Knee cave (valgus)** — knees tracking inward toward each other,
     *    detected by comparing knee x-position to foot x-position.
     * 2. **Excessive forward lean** — shoulders drifting too far in front of
     *    the knees, indicating the torso is not staying upright.
     * 3. **Insufficient depth** — knee angle above [KNEE_ANGLE_DOWN] + buffer
     *    while in [Phase.DOWN], meaning the squat is too shallow.
     * 4. **Positive feedback** — given when form looks good at each phase.
     *
     * @param landmarks The 33 normalized pose landmarks for the current frame.
     * @return A single coaching message addressing the highest-priority issue.
     */
    private fun checkForm(landmarks: List<NormalizedLandmark>): String {
        val leftKneeX  = landmarks[25].x()
        val rightKneeX = landmarks[26].x()
        val leftFootX  = landmarks[31].x()
        val rightFootX = landmarks[32].x()
        val leftShoulderX  = landmarks[11].x()
        val rightShoulderX = landmarks[12].x()

        // Knee cave: left knee should not be to the right of left foot (and vice versa)
        val leftKneeCave  = leftKneeX  - leftFootX   // positive = caving inward (to the right)
        val rightKneeCave = rightFootX - rightKneeX   // positive = caving inward (to the left)
        val kneeIsCollapsing = leftKneeCave > KNEE_CAVE_THRESHOLD ||
                rightKneeCave > KNEE_CAVE_THRESHOLD

        // Forward lean: shoulder midpoint vs knee midpoint horizontal distance
        val shoulderMidX = (leftShoulderX + rightShoulderX) / 2f
        val kneeMidX     = (leftKneeX + rightKneeX) / 2f
        val forwardLean  = Math.abs(shoulderMidX - kneeMidX)

        return when {
            kneeIsCollapsing ->
                "⚠️ Knees caving in — push knees out!"

            currentPhase == Phase.DOWN && forwardLean > FORWARD_LEAN_THRESHOLD ->
                "⚠️ Too much forward lean — chest up!"

            currentPhase == Phase.DOWN && currentKneeAngle > KNEE_ANGLE_DOWN + 15 ->
                "⬇️ Go deeper — aim for parallel!"

            currentPhase == Phase.DOWN ->
                "✓ Good depth — drive through your heels!"

            currentPhase == Phase.STANDING && repCount > 0 ->
                "✓ Good rep — stay controlled!"

            else ->
                "Feet shoulder-width apart, toes slightly out"
        }
    }

    /**
     * Resets the rep counter, phase, and angle back to initial values.
     * Call at the start of each new set.
     */
    override fun reset() {
        repCount = 0
        currentPhase = Phase.STANDING
        currentKneeAngle = 180.0
        formFeedback = ""
    }
}
