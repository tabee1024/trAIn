package com.android.example.camx

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker.PoseLandmarkerOptions
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class SimpleFrameAnalyzer(
    private val context: Context,
    private val onResult: (PoseLandmarkerResult) -> Unit,
    private val onInfo: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val poseLandmarker: PoseLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_lite.task")
            .build()

        val options = PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setNumPoses(1)
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    override fun analyze(image: ImageProxy) {
        try {
            // 1) Convert camera frame to Bitmap (use your existing extension)
            val bitmap = image.toBitmap()   // you already had this working

            // 2) Rotate so person is upright (fixes sideways skeleton)
            val rotationDegrees = image.imageInfo.rotationDegrees
            val rotated = rotateBitmap(bitmap, rotationDegrees)

            // 3) Wrap into MPImage and run MediaPipe
            val mpImage = BitmapImageBuilder(rotated).build()
            val result = poseLandmarker.detect(mpImage)

            if (result.landmarks().isEmpty()) {
                onInfo("No pose detected")
            } else {
                onInfo("Pose detected (${result.landmarks()[0].size} landmarks)")
            }

            // 4) Send result to overlay
            onResult(result)
        } catch (e: Exception) {
            onInfo("Analyzer error: ${e.message}")
        } finally {
            image.close()
        }
    }

    private fun rotateBitmap(src: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return src
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(
            src,
            0,
            0,
            src.width,
            src.height,
            matrix,
            true
        )
    }

    fun close() {
        poseLandmarker.close()
    }
}
