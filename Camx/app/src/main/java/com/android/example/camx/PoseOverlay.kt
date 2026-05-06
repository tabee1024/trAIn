package com.android.example.camx

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.*

/**
 * Transparent [View] drawing the live skeleton and an animated silhouette preview.
 *
 * The silhouette is a solid fully-opaque unified human shape built from thick
 * overlapping capsule paths merged with [Path.Op.UNION], ensuring no visible
 * seams between body segments.
 *
 * Key fix vs previous version: limb thickness is now a minimum of 20% of the
 * preview box width, ensuring all segments overlap enough to merge cleanly.
 */
class PoseOverlay(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    // ── State ─────────────────────────────────────────────────────────────────

    private var results: PoseLandmarkerResult? = null
    private var imageWidth = 1
    private var imageHeight = 1
    private var primaryAngle: Double? = null
    private var currentExerciseName: String = "Push-Ups"
    private var animProgress: Float = 0f

    // ── Animation ─────────────────────────────────────────────────────────────

    private val ANIMATION_DURATION_MS = 2000L

    private val repAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration     = ANIMATION_DURATION_MS
        repeatCount  = ValueAnimator.INFINITE
        repeatMode   = ValueAnimator.RESTART
        interpolator = LinearInterpolator()
        addUpdateListener { anim ->
            animProgress = anim.animatedValue as Float
            invalidate()
        }
    }

    // ── Preview dimensions ────────────────────────────────────────────────────

    private val PREVIEW_W      = 200f
    private val PREVIEW_H      = 260f
    private val PREVIEW_MARGIN = 16f
    private val PREVIEW_CORNER = 16f

    // ── Live skeleton paints ──────────────────────────────────────────────────

    private val pointPaint = Paint().apply {
        color = Color.YELLOW; strokeWidth = 12f
        style = Paint.Style.FILL; isAntiAlias = true
    }
    private val linePaint = Paint().apply {
        color = Color.WHITE; strokeWidth = 6f
        style = Paint.Style.STROKE; isAntiAlias = true
    }
    private val anglePaint = Paint().apply {
        color = Color.CYAN; textSize = 48f
        isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD
    }

    // ── Silhouette paints ─────────────────────────────────────────────────────

    private val silhouettePaint = Paint().apply {
        style = Paint.Style.FILL; isAntiAlias = true
    }
    private val silhouetteOutlinePaint = Paint().apply {
        color = Color.argb(120, 255, 255, 255)
        style = Paint.Style.STROKE; strokeWidth = 2f; isAntiAlias = true
    }

    // ── Preview UI paints ─────────────────────────────────────────────────────

    private val previewBgPaint = Paint().apply {
        color = Color.argb(220, 10, 10, 20)
        style = Paint.Style.FILL; isAntiAlias = true
    }
    private val previewLabelPaint = Paint().apply {
        color = Color.WHITE; textSize = 24f
        isAntiAlias = true; typeface = Typeface.DEFAULT_BOLD
    }
    private val previewSubPaint = Paint().apply {
        color = Color.argb(160, 180, 210, 255); textSize = 19f; isAntiAlias = true
    }

    // ── Pose connections ──────────────────────────────────────────────────────

    private val POSE_CONNECTIONS = listOf(
        Pair(11, 12), Pair(11, 13), Pair(13, 15),
        Pair(12, 14), Pair(14, 16),
        Pair(11, 23), Pair(12, 24), Pair(23, 24),
        Pair(23, 25), Pair(24, 26),
        Pair(25, 27), Pair(26, 28),
        Pair(27, 29), Pair(28, 30),
        Pair(29, 31), Pair(30, 32)
    )
    private val MIN_VISIBILITY = 0.5f

    // ── Exercise colors ───────────────────────────────────────────────────────

    private fun silhouetteColor(name: String): Int = when (name) {
        "Push-Ups" -> Color.rgb(30,  140, 255) // blue
        "Squats"   -> Color.rgb(160, 60,  255) // purple
        "Lunges"   -> Color.rgb(30,  210, 110) // green
        else       -> Color.rgb(30,  140, 255)
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onAttachedToWindow() { super.onAttachedToWindow(); repAnimator.start() }
    override fun onDetachedFromWindow() { super.onDetachedFromWindow(); repAnimator.cancel() }

    // ── Public API ────────────────────────────────────────────────────────────

    fun setResults(result: PoseLandmarkerResult, imgWidth: Int, imgHeight: Int) {
        results = result; imageWidth = imgWidth; imageHeight = imgHeight; invalidate()
    }

    fun setCoachData(angle: Double, current: GhostModel.GhostPose?, next: GhostModel.GhostPose?) {
        primaryAngle = angle; invalidate()
    }

    fun setExercise(exerciseName: String) {
        if (currentExerciseName != exerciseName) {
            currentExerciseName = exerciseName
            repAnimator.cancel(); animProgress = 0f; repAnimator.start(); invalidate()
        }
    }

    fun clear() {
        results = null; primaryAngle = null
        repAnimator.cancel(); animProgress = 0f; repAnimator.start(); invalidate()
    }

    // ── Coordinate helpers ────────────────────────────────────────────────────

    private fun getImageRect(): RectF {
        val vW = width.toFloat(); val vH = height.toFloat()
        val ia = imageWidth.toFloat() / imageHeight.toFloat()
        val va = vW / vH
        return if (ia > va) {
            val sh = vW / ia; val oy = (vH - sh) / 2f
            RectF(0f, oy, vW, oy + sh)
        } else {
            val sw = vH * ia; val ox = (vW - sw) / 2f
            RectF(ox, 0f, ox + sw, vH)
        }
    }

    /**
     * Maps ghost normalized landmarks into [targetRect].
     *
     * Finds the bounding box of the pose landmarks and scales them to fill
     * the target rect with [padding] on all sides. This ensures the silhouette
     * always fills the preview box regardless of the exercise orientation.
     */
    private fun mapToRect(
        landmarks: Map<Int, Pair<Float, Float>>,
        targetRect: RectF,
        padding: Float = 0.08f
    ): Map<Int, Pair<Float, Float>> {
        val xs = landmarks.values.map { it.first }
        val ys = landmarks.values.map { it.second }
        val minX = xs.min(); val maxX = xs.max()
        val minY = ys.min(); val maxY = ys.max()
        val rx = (maxX - minX).coerceAtLeast(0.01f)
        val ry = (maxY - minY).coerceAtLeast(0.01f)
        return landmarks.mapValues { (_, p) ->
            val nx = (p.first  - minX) / rx
            val ny = (p.second - minY) / ry
            Pair(
                targetRect.left + targetRect.width()  * (padding + nx * (1f - 2 * padding)),
                targetRect.top  + targetRect.height() * (padding + ny * (1f - 2 * padding))
            )
        }
    }

    // ── Silhouette paths ──────────────────────────────────────────────────────

    /**
     * Creates a rounded capsule [Path] from point [a] to point [b] with [radius].
     * The radius is deliberately large (≥ boxW * 0.12f) so adjacent capsules
     * overlap significantly and merge cleanly when combined with [Path.Op.UNION].
     */
    private fun capsulePath(a: Pair<Float, Float>, b: Pair<Float, Float>, radius: Float): Path {
        val dx = b.first - a.first; val dy = b.second - a.second
        val len = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        val path = Path()
        val rect = RectF(0f, -radius, len, radius)
        path.addRoundRect(rect, radius, radius, Path.Direction.CW)
        val m = Matrix().apply { postRotate(angle); postTranslate(a.first, a.second) }
        path.transform(m)
        return path
    }

    private fun circlePath(cx: Float, cy: Float, r: Float): Path =
        Path().apply { addCircle(cx, cy, r, Path.Direction.CW) }

    private fun torsoPath(
        lS: Pair<Float, Float>, rS: Pair<Float, Float>,
        lH: Pair<Float, Float>, rH: Pair<Float, Float>,
        extraRadius: Float = 0f
    ): Path {
        // Expand the torso slightly outward so it merges with arm capsules
        val midSX = (lS.first + rS.first) / 2f
        val midHX = (lH.first + rH.first) / 2f
        return Path().apply {
            moveTo(lS.first - extraRadius, lS.second)
            lineTo(rS.first + extraRadius, rS.second)
            lineTo(rH.first + extraRadius, rH.second)
            lineTo(lH.first - extraRadius, lH.second)
            close()
        }
    }

    /**
     * Draws a unified solid human silhouette by merging all body segment
     * paths with [Path.Op.UNION].
     *
     * **Critical for clean merging:** limb radius is set to at least
     * `boxWidth * 0.13f` so adjacent segments overlap enough to produce
     * a smooth continuous outline rather than separate blobs.
     *
     * @param canvas  Canvas to draw on.
     * @param mapped  Landmark index → screen pixel positions.
     * @param color   Solid fill color.
     * @param boxW    Width of the preview box — used to scale limb thickness.
     */
    private fun drawSilhouette(
        canvas: Canvas,
        mapped: Map<Int, Pair<Float, Float>>,
        color: Int,
        boxW: Float
    ) {
        val lS = mapped[11]; val rS = mapped[12]
        val lE = mapped[13]; val rE = mapped[14]
        val lW = mapped[15]; val rW = mapped[16]
        val lH = mapped[23]; val rH = mapped[24]
        val lK = mapped[25]; val rK = mapped[26]
        val lA = mapped[27]; val rA = mapped[28]

        // Thick limbs — minimum size ensures overlap and clean UNION merge
        val minR  = boxW * 0.13f
        val sw    = if (lS != null && rS != null) dist(lS, rS) else boxW * 0.5f
        val ut    = (sw * 0.32f).coerceAtLeast(minR) // upper arm
        val ft    = (sw * 0.26f).coerceAtLeast(minR) // forearm
        val tt    = (sw * 0.38f).coerceAtLeast(minR) // thigh
        val st    = (sw * 0.30f).coerceAtLeast(minR) // shin
        val nk    = (sw * 0.20f).coerceAtLeast(minR * 0.8f) // neck
        val torsoExtra = ut * 0.4f // expand torso to meet arm capsules

        val body = Path()

        // Torso (expanded so arms merge into it)
        if (lS != null && rS != null && lH != null && rH != null)
            body.op(torsoPath(lS, rS, lH, rH, torsoExtra), Path.Op.UNION)

        // Upper arms
        if (lS != null && lE != null) body.op(capsulePath(lS, lE, ut), Path.Op.UNION)
        if (rS != null && rE != null) body.op(capsulePath(rS, rE, ut), Path.Op.UNION)

        // Forearms
        if (lE != null && lW != null) body.op(capsulePath(lE, lW, ft), Path.Op.UNION)
        if (rE != null && rW != null) body.op(capsulePath(rE, rW, ft), Path.Op.UNION)

        // Thighs
        if (lH != null && lK != null) body.op(capsulePath(lH, lK, tt), Path.Op.UNION)
        if (rH != null && rK != null) body.op(capsulePath(rH, rK, tt), Path.Op.UNION)

        // Shins
        if (lK != null && lA != null) body.op(capsulePath(lK, lA, st), Path.Op.UNION)
        if (rK != null && rA != null) body.op(capsulePath(rK, rA, st), Path.Op.UNION)

        // Head + neck
        if (lS != null && rS != null) {
            val headR = (sw * 0.34f).coerceAtLeast(minR)
            val neckL = headR * 1.1f
            val mx = (lS.first + rS.first) / 2f
            val my = (lS.second + rS.second) / 2f
            body.op(capsulePath(Pair(mx, my), Pair(mx, my - neckL), nk), Path.Op.UNION)
            body.op(circlePath(mx, my - neckL - headR, headR), Path.Op.UNION)
        }

        silhouettePaint.color = color
        canvas.drawPath(body, silhouettePaint)
        canvas.drawPath(body, silhouetteOutlinePaint)
    }

    private fun dist(a: Pair<Float, Float>, b: Pair<Float, Float>): Float {
        val dx = a.first - b.first; val dy = a.second - b.second
        return sqrt(dx * dx + dy * dy)
    }

    // ── Main draw ─────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rect = getImageRect()

        // ── Live skeleton ─────────────────────────────────────────────────────
        val result = results
        if (result != null && result.landmarks().isNotEmpty()) {
            val lms = result.landmarks()[0]
            fun lmX(lm: NormalizedLandmark) = rect.left + lm.x() * rect.width()
            fun lmY(lm: NormalizedLandmark) = rect.top  + lm.y() * rect.height()

            for ((s, e) in POSE_CONNECTIONS) {
                if (s < lms.size && e < lms.size) {
                    val sl = lms[s]; val el = lms[e]
                    if (sl.visibility().orElse(0f) < MIN_VISIBILITY ||
                        el.visibility().orElse(0f) < MIN_VISIBILITY) continue
                    canvas.drawLine(lmX(sl), lmY(sl), lmX(el), lmY(el), linePaint)
                }
            }
            for (lm in lms) {
                if (lm.visibility().orElse(0f) < MIN_VISIBILITY) continue
                canvas.drawCircle(lmX(lm), lmY(lm), 8f, pointPaint)
            }

            val angle = primaryAngle
            if (angle != null && lms.size > 14) {
                val le = lms[13]; val re = lms[14]
                if (le.visibility().orElse(0f) >= MIN_VISIBILITY ||
                    re.visibility().orElse(0f) >= MIN_VISIBILITY) {
                    val mx = (lmX(le) + lmX(re)) / 2f
                    val my = (lmY(le) + lmY(re)) / 2f
                    canvas.drawText("${angle.toInt()}°", mx, my - 20f, anglePaint)
                }
            }
        }

        // ── Animated silhouette preview ───────────────────────────────────────
        drawAnimatedPreview(canvas)
    }

    // ── Animated preview ──────────────────────────────────────────────────────

    private fun drawAnimatedPreview(canvas: Canvas) {
        val poses = GhostModel.getAnimationPoses(currentExerciseName) ?: return
        val ghostPose = GhostModel.interpolate(poses.first, poses.second, animProgress)

        // ✅ Centered horizontally at the top
        val left   = (width / 2f) - (PREVIEW_W / 2f)
        val top    = PREVIEW_MARGIN
        val right  = left + PREVIEW_W
        val bottom = top  + PREVIEW_H

        // Background
        canvas.drawRoundRect(RectF(left, top, right, bottom), PREVIEW_CORNER, PREVIEW_CORNER, previewBgPaint)

        // Silhouette drawing area
        val skRect = RectF(left + 10f, top + 14f, right - 10f, bottom - 50f)

        // Map and draw solid silhouette
        val mapped = mapToRect(ghostPose.landmarks, skRect)
        drawSilhouette(canvas, mapped, silhouetteColor(currentExerciseName), PREVIEW_W)

        // Divider
        val div = Paint().apply { color = Color.argb(50, 255, 255, 255); strokeWidth = 1f }
        canvas.drawLine(left + 10f, bottom - 42f, right - 10f, bottom - 42f, div)

        // Labels
        val label = currentExerciseName
        canvas.drawText(label, left + (PREVIEW_W - previewLabelPaint.measureText(label)) / 2f, bottom - 22f, previewLabelPaint)
        val sub = "Demonstrating..."
        canvas.drawText(sub, left + (PREVIEW_W - previewSubPaint.measureText(sub)) / 2f, bottom - 6f, previewSubPaint)
    }
}