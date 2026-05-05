package com.example.trainapp.camera

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class PoseOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var poseResult: PoseLandmarkerResult? = null
    private var isMirrored: Boolean = true

    fun setMirrored(mirrored: Boolean) {
        isMirrored = mirrored
        invalidate()
    }

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

    fun setPoseResult(result: PoseLandmarkerResult?) {
        poseResult = result
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pose = poseResult?.landmarks()?.firstOrNull() ?: return
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        // Mirror if requested (typical for front camera)
        if (isMirrored) {
            canvas.scale(-1f, 1f, width / 2f, height / 2f)
        }

        pose.forEach { landmark ->
            canvas.drawCircle(landmark.x() * width, landmark.y() * height, 8f, pointPaint)
        }

        val connections = listOf(
            11 to 13, 13 to 15,
            12 to 14, 14 to 16,
            11 to 12,
            11 to 23, 12 to 24,
            23 to 24,
            23 to 25, 25 to 27,
            24 to 26, 26 to 28,
        )

        connections.forEach { (start, end) ->
            if (start < pose.size && end < pose.size) {
                val first = pose[start]
                val second = pose[end]
                canvas.drawLine(
                    first.x() * width,
                    first.y() * height,
                    second.x() * width,
                    second.y() * height,
                    linePaint,
                )
            }
        }
    }
}
