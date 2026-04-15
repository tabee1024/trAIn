package com.android.example.camx

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * A transparent [View] that draws the MediaPipe pose skeleton and real-time
 * coaching data on top of the camera preview.
 *
 * Renders two layers:
 * 1. **Skeleton** — yellow joint circles and white connection lines for the
 *    detected pose landmarks.
 * 2. **Coach overlay** — the current elbow angle displayed near the elbow
 *    landmarks, helping the user see their depth in real time.
 *
 * Landmark coordinates from MediaPipe are normalized (0.0–1.0). This view
 * maps them into actual screen pixel positions using [getImageRect], which
 * accounts for the letterbox/pillarbox black bars introduced by the
 * [androidx.camera.view.PreviewView] `fitCenter` scale type.
 *
 * Landmarks with [NormalizedLandmark.visibility] below [MIN_VISIBILITY] are
 * skipped to avoid drawing phantom joints for occluded body parts.
 *
 * @see PushUpCounter
 * @see PoseAnalyzer
 */
class PoseOverlay(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var results: PoseLandmarkerResult? = null
    private var imageWidth = 1
    private var imageHeight = 1

    /**
     * The current elbow angle (degrees) to display near the elbow landmarks.
     * Set via [setCoachData]. Null means no angle label is drawn.
     */
    private var elbowAngle: Double? = null

    /** Yellow fill paint for landmark joint circles. Anti-aliased. */
    private val pointPaint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 12f
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    /** White stroke paint for skeleton connection lines. Anti-aliased. */
    private val linePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    /**
     * Paint for the elbow angle text label drawn near the elbow joints.
     *
     * Cyan color provides high contrast against both the white skeleton lines
     * and the dark camera background.
     */
    private val anglePaint = Paint().apply {
        color = Color.CYAN
        textSize = 48f
        isAntiAlias = true
        typeface = Typeface.DEFAULT_BOLD
    }

    /**
     * Pairs of landmark indices defining the body skeleton connections.
     *
     * Covers the torso, arms, and legs. Head/face landmarks (0–10) are omitted
     * for a cleaner body-only overlay. For push-ups the arm connections
     * (11–16) are the most important for feedback.
     *
     * Index reference:
     * - 11/12 = shoulders, 13/14 = elbows, 15/16 = wrists
     * - 23/24 = hips, 25/26 = knees, 27/28 = ankles
     * - 29/30 = heels, 31/32 = foot index
     */
    private val POSE_CONNECTIONS = listOf(
        Pair(11, 12), Pair(11, 13), Pair(13, 15),
        Pair(12, 14), Pair(14, 16),
        Pair(11, 23), Pair(12, 24), Pair(23, 24),
        Pair(23, 25), Pair(24, 26),
        Pair(25, 27), Pair(26, 28),
        Pair(27, 29), Pair(28, 30),
        Pair(29, 31), Pair(30, 32)
    )

    /**
     * Minimum visibility confidence (0.0–1.0) required to render a landmark
     * or connection. Filters out MediaPipe detections with low confidence,
     * such as body parts outside the frame or occluded by the floor.
     */
    private val MIN_VISIBILITY = 0.5f

    /**
     * Updates the pose landmarks to render and triggers a redraw.
     *
     * Must be called on the main thread.
     *
     * @param result    The [PoseLandmarkerResult] from the current frame.
     * @param imgWidth  Width of the analyzed image in pixels.
     * @param imgHeight Height of the analyzed image in pixels.
     */
    fun setResults(result: PoseLandmarkerResult, imgWidth: Int, imgHeight: Int) {
        results = result
        imageWidth = imgWidth
        imageHeight = imgHeight
        invalidate()
    }

    /**
     * Updates the coaching data overlay and triggers a redraw.
     *
     * Called each frame by [MainActivity.onResults] with the latest elbow
     * angle from [PushUpCounter]. The angle is drawn as a text label near
     * the midpoint of the elbow landmarks on screen.
     *
     * @param angle The current average elbow angle in degrees to display.
     */
    fun setCoachData(angle: Double) {
        elbowAngle = angle
        invalidate()
    }

    /**
     * Clears all results and coach data, removing everything from the screen.
     * Call when pose detection is stopped or the session resets.
     */
    fun clear() {
        results = null
        elbowAngle = null
        invalidate()
    }

    /**
     * Computes the [RectF] of the actual camera image area within this view,
     * matching the `fitCenter` scale type of [androidx.camera.view.PreviewView].
     *
     * `fitCenter` scales the image to fit entirely within the view while
     * preserving aspect ratio, leaving black bars where the aspect ratios
     * differ. Landmarks must be mapped into this rect (not the full view)
     * to align correctly with the visible preview.
     *
     * @return A [RectF] in view-pixel coordinates for the rendered image area.
     */
    private fun getImageRect(): RectF {
        val viewW = width.toFloat()
        val viewH = height.toFloat()
        val imageAspect = imageWidth.toFloat() / imageHeight.toFloat()
        val viewAspect = viewW / viewH

        return if (imageAspect > viewAspect) {
            // Letterbox — black bars top and bottom
            val scaledH = viewW / imageAspect
            val offsetY = (viewH - scaledH) / 2f
            RectF(0f, offsetY, viewW, offsetY + scaledH)
        } else {
            // Pillarbox — black bars left and right
            val scaledW = viewH * imageAspect
            val offsetX = (viewW - scaledW) / 2f
            RectF(offsetX, 0f, offsetX + scaledW, viewH)
        }
    }

    /**
     * Renders the skeleton and elbow angle label onto the canvas.
     *
     * Draw order:
     * 1. White skeleton connection lines (drawn first, behind joints)
     * 2. Yellow landmark joint circles (drawn on top of lines)
     * 3. Cyan elbow angle text label (drawn last, always on top)
     *
     * The elbow angle label is positioned at the midpoint between the left
     * and right elbow landmarks (indices 13 and 14).
     *
     * @param canvas The [Canvas] provided by the Android View system.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val result = results ?: return
        if (result.landmarks().isEmpty()) return

        val landmarks = result.landmarks()[0]
        val rect = getImageRect()

        fun lmX(lm: NormalizedLandmark) = rect.left + lm.x() * rect.width()
        fun lmY(lm: NormalizedLandmark) = rect.top  + lm.y() * rect.height()

        // Pass 1: Skeleton connection lines
        for ((start, end) in POSE_CONNECTIONS) {
            if (start < landmarks.size && end < landmarks.size) {
                val s = landmarks[start]
                val e = landmarks[end]
                if (s.visibility().orElse(0f) < MIN_VISIBILITY ||
                    e.visibility().orElse(0f) < MIN_VISIBILITY) continue
                canvas.drawLine(lmX(s), lmY(s), lmX(e), lmY(e), linePaint)
            }
        }

        // Pass 2: Joint circles
        for (landmark in landmarks) {
            if (landmark.visibility().orElse(0f) < MIN_VISIBILITY) continue
            canvas.drawCircle(lmX(landmark), lmY(landmark), 8f, pointPaint)
        }

        // Pass 3: Elbow angle label
        // Draw the angle at the midpoint between both elbows so it's visible
        // regardless of which side the person faces the camera.
        val angle = elbowAngle ?: return
        if (landmarks.size > 14) {
            val leftElbow  = landmarks[13]
            val rightElbow = landmarks[14]
            if (leftElbow.visibility().orElse(0f) >= MIN_VISIBILITY ||
                rightElbow.visibility().orElse(0f) >= MIN_VISIBILITY) {

                val midX = (lmX(leftElbow) + lmX(rightElbow)) / 2f
                val midY = (lmY(leftElbow) + lmY(rightElbow)) / 2f
                canvas.drawText("${angle.toInt()}°", midX, midY - 20f, anglePaint)
            }
        }
    }
}