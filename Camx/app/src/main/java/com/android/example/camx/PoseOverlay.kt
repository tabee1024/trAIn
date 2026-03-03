package com.android.example.camx

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * A transparent [View] that draws the MediaPipe pose skeleton on top of the camera preview.
 *
 * This view is placed directly over the [androidx.camera.view.PreviewView] in the layout,
 * with a transparent background so the camera feed shows through. It translates normalized
 * MediaPipe landmark coordinates (in the range 0.0–1.0) into pixel coordinates on screen,
 * accounting for the letterbox or pillarbox black bars introduced by the PreviewView's
 * `fitCenter` scale type.
 *
 * The skeleton consists of:
 * - **Yellow circles** at each detected landmark joint
 * - **White lines** connecting joints according to the MediaPipe 33-point body model
 *
 * Landmarks with a [NormalizedLandmark.visibility] score below [MIN_VISIBILITY]
 * are skipped, preventing jittery or incorrect points being drawn for body parts
 * that are occluded or outside the frame.
 *
 * MediaPipe 33 landmark indices reference:
 * https://ai.google.dev/edge/mediapipe/solutions/vision/pose_landmarker
 *
 * Usage:
 * ```kotlin
 * // From PoseLandmarkerListener.onResults (must be on main thread):
 * poseOverlay.setResults(result, imageWidth, imageHeight)
 *
 * // When no pose is present:
 * poseOverlay.clear()
 * ```
 *
 * @see PoseLandmarkerHelper
 * @see MainActivity
 */
class PoseOverlay(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    /**
     * The most recent pose detection result to render.
     * `null` when no result has been set or after [clear] is called.
     */
    private var results: PoseLandmarkerResult? = null

    /**
     * Width of the image that produced the current [results], in pixels.
     * Used together with [imageHeight] to compute [getImageRect].
     */
    private var imageWidth = 1

    /**
     * Height of the image that produced the current [results], in pixels.
     */
    private var imageHeight = 1

    /**
     * Paint used to draw landmark joint circles.
     *
     * Yellow fill, 8px radius, anti-aliased for smooth rendering.
     */
    private val pointPaint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 12f
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    /**
     * Paint used to draw skeleton connection lines between joints.
     *
     * White stroke, 6px width, anti-aliased for smooth rendering.
     */
    private val linePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    /**
     * Pairs of landmark indices defining the skeleton connections to draw.
     *
     * Each pair `(a, b)` draws a line from landmark `a` to landmark `b`.
     * Indices correspond to the MediaPipe 33-point body model:
     *
     * - 11/12 = left/right shoulder
     * - 13/14 = left/right elbow
     * - 15/16 = left/right wrist
     * - 23/24 = left/right hip
     * - 25/26 = left/right knee
     * - 27/28 = left/right ankle
     * - 29/30 = left/right heel
     * - 31/32 = left/right foot index
     *
     * Note: Head, face, and finger landmarks (0–10) are intentionally excluded
     * for a cleaner body-only skeleton overlay.
     */
    private val POSE_CONNECTIONS = listOf(
        Pair(11, 12), // shoulder to shoulder
        Pair(11, 13), // left shoulder to left elbow
        Pair(13, 15), // left elbow to left wrist
        Pair(12, 14), // right shoulder to right elbow
        Pair(14, 16), // right elbow to right wrist
        Pair(11, 23), // left shoulder to left hip
        Pair(12, 24), // right shoulder to right hip
        Pair(23, 24), // hip to hip
        Pair(23, 25), // left hip to left knee
        Pair(24, 26), // right hip to right knee
        Pair(25, 27), // left knee to left ankle
        Pair(26, 28), // right knee to right ankle
        Pair(27, 29), // left ankle to left heel
        Pair(28, 30), // right ankle to right heel
        Pair(29, 31), // left heel to left foot index
        Pair(30, 32)  // right heel to right foot index
    )

    /**
     * Minimum visibility score (0.0–1.0) required to draw a landmark or connection.
     *
     * MediaPipe assigns each landmark a visibility score estimating whether
     * the point is within the frame and unoccluded. Points below this threshold
     * are skipped to avoid drawing phantom joints for body parts that are not
     * clearly visible. 0.5 means at least 50% confidence the point is visible.
     */
    private val MIN_VISIBILITY = 0.5f

    /**
     * Updates the overlay with new pose detection results and triggers a redraw.
     *
     * Must be called on the main thread. Typically invoked from
     * [MainActivity.onResults] after marshalling from the MediaPipe thread.
     *
     * @param result    The pose detection result from MediaPipe containing
     *                  normalized landmark positions.
     * @param imgWidth  Width of the image that was analyzed, in pixels.
     *                  Must match the bitmap passed to [PoseLandmarkerHelper.detectLiveStream].
     * @param imgHeight Height of the analyzed image in pixels.
     */
    fun setResults(result: PoseLandmarkerResult, imgWidth: Int, imgHeight: Int) {
        results = result
        imageWidth = imgWidth
        imageHeight = imgHeight
        invalidate()
    }

    /**
     * Clears the current results and removes the skeleton from the screen.
     *
     * Call this when pose detection stops or no pose is present to avoid
     * leaving a stale skeleton frozen on screen.
     */
    fun clear() {
        results = null
        invalidate()
    }

    /**
     * Computes the [RectF] representing the area within this view that the
     * camera preview image actually occupies, accounting for `fitCenter` scaling.
     *
     * The [androidx.camera.view.PreviewView] uses `fitCenter`, which scales the
     * camera image to fit entirely within the view while preserving aspect ratio.
     * This means black bars may appear on the sides (pillarbox) or top/bottom
     * (letterbox) when the image and view aspect ratios differ.
     *
     * Landmark coordinates are normalized to the image's own dimensions (0.0–1.0),
     * so they must be mapped into this rect — not the full view dimensions — to
     * correctly overlay the visible preview image.
     *
     * @return A [RectF] in view-pixel coordinates describing the image's drawn area.
     */
    private fun getImageRect(): RectF {
        val viewW = width.toFloat()
        val viewH = height.toFloat()
        val imageAspect = imageWidth.toFloat() / imageHeight.toFloat()
        val viewAspect = viewW / viewH

        return if (imageAspect > viewAspect) {
            // Image is wider than the view → letterbox (black bars top & bottom)
            val scaledHeight = viewW / imageAspect
            val offsetY = (viewH - scaledHeight) / 2f
            RectF(0f, offsetY, viewW, offsetY + scaledHeight)
        } else {
            // Image is taller than the view → pillarbox (black bars left & right)
            val scaledWidth = viewH * imageAspect
            val offsetX = (viewW - scaledWidth) / 2f
            RectF(offsetX, 0f, offsetX + scaledWidth, viewH)
        }
    }

    /**
     * Renders the pose skeleton onto the canvas.
     *
     * Drawing order:
     * 1. Skeleton connection lines (drawn first so joints appear on top)
     * 2. Landmark joint circles
     *
     * Both steps skip any landmark whose visibility score is below [MIN_VISIBILITY].
     * A connection line is only drawn if **both** of its endpoint landmarks
     * meet the visibility threshold.
     *
     * Landmark coordinates are mapped from normalized image space (0.0–1.0) into
     * screen pixel space using [getImageRect], ensuring the skeleton aligns
     * precisely with the camera preview regardless of letterboxing.
     *
     * @param canvas The canvas to draw the skeleton on.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val result = results ?: return
        if (result.landmarks().isEmpty()) return

        val landmarks = result.landmarks()[0]
        val rect = getImageRect()

        // Helper lambdas to convert normalized landmark coords to screen pixels.
        fun lmX(lm: NormalizedLandmark) = rect.left + lm.x() * rect.width()
        fun lmY(lm: NormalizedLandmark) = rect.top + lm.y() * rect.height()

        // Pass 1: Draw skeleton connection lines.
        for ((start, end) in POSE_CONNECTIONS) {
            if (start < landmarks.size && end < landmarks.size) {
                val startLm = landmarks[start]
                val endLm = landmarks[end]

                // Skip connection if either endpoint is not confidently visible.
                if (startLm.visibility().orElse(0f) < MIN_VISIBILITY ||
                    endLm.visibility().orElse(0f) < MIN_VISIBILITY) continue

                canvas.drawLine(lmX(startLm), lmY(startLm), lmX(endLm), lmY(endLm), linePaint)
            }
        }

        // Pass 2: Draw joint circles on top of connection lines.
        for (landmark in landmarks) {
            if (landmark.visibility().orElse(0f) < MIN_VISIBILITY) continue
            canvas.drawCircle(lmX(landmark), lmY(landmark), 8f, pointPaint)
        }
    }
}