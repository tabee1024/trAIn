package com.android.example.camx

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var landmarks: PoseLandmarkerResult? = null

    // Paint for dots and lines
    private val pointPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        strokeWidth = 10f
        isAntiAlias = true
    }

    private val linePaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    /**
     * MAIN FUNCTION NEEDED BY MainActivity
     */
    fun setLandmarks(result: PoseLandmarkerResult) {
        landmarks = result
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val result = landmarks ?: return
        if (result.landmarks().isEmpty()) return

        val pose = result.landmarks()[0]   // first detected person

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        for (lm in pose) {
            val x = lm.x() * width
            val y = lm.y() * height
            canvas.drawCircle(x, y, 8f, pointPaint)
        }

        // Connect lines (same structure as MediaPipe)
        val connections = listOf(
            Pair(11, 13), Pair(13, 15),       // Left arm
            Pair(12, 14), Pair(14, 16),       // Right arm
            Pair(11, 12),                     // Shoulders
            Pair(23, 24),                     // Hips
            Pair(11, 23), Pair(12, 24),       // Torso
            Pair(23, 25), Pair(25, 27),       // Left leg
            Pair(24, 26), Pair(26, 28)        // Right leg
        )

        for ((startIdx, endIdx) in connections) {
            if (startIdx < pose.size && endIdx < pose.size) {
                val s = pose[startIdx]
                val e = pose[endIdx]

                canvas.drawLine(
                    s.x() * width,
                    s.y() * height,
                    e.x() * width,
                    e.y() * height,
                    linePaint
                )
            }
        }
    }
}
