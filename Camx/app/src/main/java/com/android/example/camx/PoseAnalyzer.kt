package com.android.example.camx

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.*

/**
 * Utility object for geometric calculations on MediaPipe pose landmarks.
 *
 * All methods operate on [NormalizedLandmark] objects whose x/y coordinates
 * are normalized to the range 0.0–1.0 relative to the image dimensions.
 * Since we only need angles (not absolute distances), normalization does not
 * affect correctness as long as the image aspect ratio is consistent.
 *
 * MediaPipe landmark index reference:
 * https://ai.google.dev/edge/mediapipe/solutions/vision/pose_landmarker
 */
object PoseAnalyzer {

    /**
     * Computes the interior angle (in degrees) at joint [b], formed by the
     * vector b→a and the vector b→c.
     *
     * This is the standard "joint angle" used in biomechanics — for example,
     * passing shoulder/elbow/wrist returns the elbow flexion angle.
     *
     * Uses `atan2` on the 2D (x, y) plane. Z-depth from MediaPipe world
     * landmarks is intentionally ignored here to keep calculations stable
     * across varying camera distances.
     *
     * @param a First landmark (e.g. shoulder).
     * @param b Middle landmark — the joint whose angle is measured (e.g. elbow).
     * @param c Third landmark (e.g. wrist).
     * @return Angle in degrees, in the range [0.0, 180.0].
     */
    fun getAngle(
        a: NormalizedLandmark,
        b: NormalizedLandmark,
        c: NormalizedLandmark
    ): Double {
        val radians = atan2(
            (c.y() - b.y()).toDouble(), (c.x() - b.x()).toDouble()
        ) - atan2(
            (a.y() - b.y()).toDouble(), (a.x() - b.x()).toDouble()
        )
        var angle = abs(Math.toDegrees(radians))
        if (angle > 180.0) angle = 360.0 - angle
        return angle
    }

    /**
     * Returns the average of the left-side and right-side versions of a joint angle.
     *
     * Because the person may not be perfectly aligned with the camera, using
     * the average of both sides reduces noise from slight rotations.
     *
     * @param leftA  Left-side first landmark (e.g. left shoulder, index 11).
     * @param leftB  Left-side middle landmark (e.g. left elbow, index 13).
     * @param leftC  Left-side third landmark (e.g. left wrist, index 15).
     * @param rightA Right-side first landmark (e.g. right shoulder, index 12).
     * @param rightB Right-side middle landmark (e.g. right elbow, index 14).
     * @param rightC Right-side third landmark (e.g. right wrist, index 16).
     * @return Average angle in degrees across both sides.
     */
    fun getAverageAngle(
        leftA: NormalizedLandmark, leftB: NormalizedLandmark, leftC: NormalizedLandmark,
        rightA: NormalizedLandmark, rightB: NormalizedLandmark, rightC: NormalizedLandmark
    ): Double {
        val leftAngle  = getAngle(leftA, leftB, leftC)
        val rightAngle = getAngle(rightA, rightB, rightC)
        return (leftAngle + rightAngle) / 2.0
    }

    /**
     * Calculates how well the body forms a straight line from shoulders to ankles.
     *
     * A good push-up requires a rigid plank — hips should not sag below or
     * pike above the shoulder-to-ankle line. This is measured as the vertical
     * deviation of the hip midpoint from the midpoint of shoulders and ankles.
     *
     * Positive values mean hips are above the line (piking).
     * Negative values mean hips are below the line (sagging).
     *
     * The value is in normalized image coordinates (0.0–1.0), so thresholds
     * of ~0.05 represent roughly 5% of the image height.
     *
     * @param landmarks Full list of 33 MediaPipe pose landmarks.
     * @return Signed vertical deviation of hips from the shoulder-ankle line.
     *         Positive = hips high (pike), negative = hips low (sag).
     */
    fun getBodyAlignmentDeviation(landmarks: List<NormalizedLandmark>): Float {
        val shoulderMidY = (landmarks[11].y() + landmarks[12].y()) / 2f
        val hipMidY      = (landmarks[23].y() + landmarks[24].y()) / 2f
        val ankleMidY    = (landmarks[27].y() + landmarks[28].y()) / 2f

        // Expected hip Y if body is perfectly straight
        val expectedHipY = (shoulderMidY + ankleMidY) / 2f

        // Negative = hips are higher than expected (pike)
        // Positive = hips are lower than expected (sag)
        return hipMidY - expectedHipY
    }
}