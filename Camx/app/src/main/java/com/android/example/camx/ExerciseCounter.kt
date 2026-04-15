package com.android.example.camx

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

/**
 * Common interface for all rule-based exercise rep counters.
 *
 * Each exercise (push-ups, squats, lunges, etc.) implements this interface
 * so [MainActivity] can swap between them without caring about the specific
 * exercise logic. This follows the Strategy pattern — the algorithm (exercise
 * counting logic) is encapsulated and interchangeable at runtime.
 *
 * ## Implementing a new exercise
 * 1. Create a class implementing [ExerciseCounter]
 * 2. Define your state machine phases in an inner [enum class]
 * 3. Implement [update] to process landmarks and return a [CoachResult]
 * 4. Add your counter to [MainActivity] and the exercise toggle button
 *
 * @see PushUpCounter
 * @see SquatCounter
 */
interface ExerciseCounter {

    /** Display name of the exercise shown in the UI. */
    val exerciseName: String

    /** Number of completed repetitions in the current session. */
    val repCount: Int

    /** Most recent form feedback message for display. */
    val formFeedback: String

    /**
     * Processes a single frame's landmarks and returns the updated coach state.
     *
     * Should be called once per frame from the MediaPipe result callback.
     * Implementations must be lightweight — no blocking operations, no I/O.
     *
     * @param landmarks The 33 normalized pose landmarks from MediaPipe for this frame.
     * @return A [CoachResult] snapshot representing the current state.
     */
    fun update(landmarks: List<NormalizedLandmark>): CoachResult

    /**
     * Resets the rep counter and state machine back to initial values.
     * Call at the start of each new set or session.
     */
    fun reset()

    /**
     * Immutable snapshot of the exercise coach state at a single frame.
     *
     * Returned by [update] each frame so the UI can render the latest state
     * without holding a direct reference to the counter implementation.
     *
     * @param repCount    Number of completed reps so far.
     * @param phaseLabel  Human-readable label for the current movement phase
     *                    (e.g. "⬇ Go down", "⬆ Push up", "✓ Top").
     * @param primaryAngle The main joint angle being tracked (degrees), shown
     *                    on the [PoseOverlay] as a real-time label.
     * @param feedback    Coaching feedback string for display.
     */
    data class CoachResult(
        val repCount: Int,
        val phaseLabel: String,
        val primaryAngle: Double,
        val feedback: String
    )
}