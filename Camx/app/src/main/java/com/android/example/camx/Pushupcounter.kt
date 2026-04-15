package com.android.example.camx

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

/**
 * Rule-based push-up repetition counter and real-time form coach.
 *
 * Implements [ExerciseCounter] using elbow flexion angle and body alignment
 * deviation (computed by [PoseAnalyzer]) to count reps and validate form.
 *
 * ## How rep counting works
 *
 * A push-up rep is counted using this state machine:
 * ```
 * WAITING_FOR_DOWN → DOWN (elbow angle < threshold) → UP (rep counted) → WAITING_FOR_DOWN
 * ```
 *
 * ## Landmark indices used
 *
 * | Body part | Left | Right |
 * |-----------|------|-------|
 * | Shoulder  | 11   | 12    |
 * | Elbow     | 13   | 14    |
 * | Wrist     | 15   | 16    |
 * | Hip       | 23   | 24    |
 * | Ankle     | 27   | 28    |
 */
class PushUpCounter : ExerciseCounter {

    override val exerciseName = "Push-Ups"

    companion object {
        /** Elbow angle (degrees) threshold for the bottom of a push-up (~90°). */
        const val ELBOW_ANGLE_DOWN = 100.0

        /** Elbow angle (degrees) threshold for the top of a push-up (~160°+). */
        const val ELBOW_ANGLE_UP = 160.0

        /** Hip sag threshold in normalized coordinates (~5% of image height). */
        const val SAG_THRESHOLD = 0.05f

        /** Hip pike threshold in normalized coordinates. */
        const val PIKE_THRESHOLD = -0.05f
    }

    override var repCount: Int = 0
        private set

    override var formFeedback: String = ""
        private set

    /** Current phase of the push-up movement. */
    var currentPhase: Phase = Phase.WAITING_FOR_DOWN
        private set

    /** Most recently computed average elbow angle in degrees. */
    var currentElbowAngle: Double = 180.0
        private set

    /**
     * Represents the phase of a single push-up repetition.
     */
    enum class Phase {
        /** Waiting for elbows to bend into the down position. */
        WAITING_FOR_DOWN,

        /** Elbows are bent — person is at the bottom of the push-up. */
        DOWN,

        /** Elbows are extended — person has pushed back to the top (rep complete). */
        UP
    }

    /**
     * Processes a new set of pose landmarks to update rep count, phase, and feedback.
     *
     * @param landmarks The 33 normalized pose landmarks from MediaPipe.
     * @return A [ExerciseCounter.CoachResult] snapshot of the current state.
     */
    override fun update(landmarks: List<NormalizedLandmark>): ExerciseCounter.CoachResult {
        if (landmarks.size < 29) {
            return ExerciseCounter.CoachResult(repCount, "No pose", 0.0, "No pose detected")
        }

        // Average elbow angle across both arms
        currentElbowAngle = PoseAnalyzer.getAverageAngle(
            leftA  = landmarks[11], leftB  = landmarks[13], leftC  = landmarks[15],
            rightA = landmarks[12], rightB = landmarks[14], rightC = landmarks[16]
        )

        // --- Rep state machine ---
        when (currentPhase) {
            Phase.WAITING_FOR_DOWN -> {
                if (currentElbowAngle < ELBOW_ANGLE_DOWN) currentPhase = Phase.DOWN
            }
            Phase.DOWN -> {
                if (currentElbowAngle > ELBOW_ANGLE_UP) {
                    currentPhase = Phase.UP
                    repCount++
                }
            }
            Phase.UP -> {
                if (currentElbowAngle < ELBOW_ANGLE_UP) currentPhase = Phase.WAITING_FOR_DOWN
            }
        }

        formFeedback = checkForm(landmarks)

        val phaseLabel = when (currentPhase) {
            Phase.WAITING_FOR_DOWN -> "⬇ Go down"
            Phase.DOWN             -> "⬆ Push up"
            Phase.UP               -> "✓ Top"
        }

        return ExerciseCounter.CoachResult(
            repCount     = repCount,
            phaseLabel   = phaseLabel,
            primaryAngle = currentElbowAngle,
            feedback     = formFeedback
        )
    }

    /**
     * Evaluates pose landmarks for common push-up form errors.
     *
     * Checks (in priority order): hip sag, hip pike, insufficient depth,
     * incomplete extension. Returns a positive message when form is good.
     *
     * @param landmarks The 33 normalized pose landmarks for the current frame.
     * @return A single coaching message for the highest-priority issue found.
     */
    private fun checkForm(landmarks: List<NormalizedLandmark>): String {
        val alignment = PoseAnalyzer.getBodyAlignmentDeviation(landmarks)

        return when {
            alignment > SAG_THRESHOLD ->
                "⚠️ Hips sagging — tighten your core!"

            alignment < PIKE_THRESHOLD ->
                "⚠️ Hips too high — lower your hips!"

            currentPhase == Phase.DOWN && currentElbowAngle > ELBOW_ANGLE_DOWN + 20 ->
                "⬇️ Go lower — aim for 90° at the elbows"

            currentPhase == Phase.UP && currentElbowAngle < ELBOW_ANGLE_UP ->
                "⬆️ Fully extend your arms at the top"

            currentPhase == Phase.DOWN ->
                "✓ Good depth — now push up!"

            currentPhase == Phase.UP ->
                "✓ Good rep — keep going!"

            else ->
                "Ready — lower yourself down"
        }
    }

    /**
     * Resets rep count, phase, and angle back to initial values.
     * Call at the start of each new set.
     */
    override fun reset() {
        repCount = 0
        currentPhase = Phase.WAITING_FOR_DOWN
        currentElbowAngle = 180.0
        formFeedback = ""
    }
}