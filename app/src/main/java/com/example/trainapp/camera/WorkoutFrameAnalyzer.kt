package com.example.trainapp.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.trainapp.model.WorkoutType
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

data class LiveWorkoutFeedback(
    val repCount: Int,
    val formScore: Double,
    val postureScore: Double,
    val stabilityScore: Double,
    val rangeOfMotion: Double,
    val correctRepPercentage: Double,
    val mistakes: List<String>,
    val feedback: String,
    val poseResult: PoseLandmarkerResult?,
)

class WorkoutFrameAnalyzer(
    context: Context,
    private val workoutType: WorkoutType,
    private val onFeedback: (LiveWorkoutFeedback) -> Unit,
) : ImageAnalysis.Analyzer {

    private val poseLandmarker: PoseLandmarker
    private var repCount = 0
    private var movedIntoDepth = false
    private var minAngleDuringRep = 200.0
    private var completedReps = 0
    private var goodReps = 0
    private var lastFeedback = baselineFeedback()

    private var frameCounter = 0
    private val frameSkipRate = 2 // Only analyze every 2nd frame (roughly 15 FPS)

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("pose_landmarker_lite.task")
            .build()
        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setNumPoses(1)
            .setMinPoseDetectionConfidence(0.4f)
            .setMinPosePresenceConfidence(0.4f)
            .setMinTrackingConfidence(0.4f)
            .build()
        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    override fun analyze(image: ImageProxy) {
        frameCounter++
        if (frameCounter % frameSkipRate != 0) {
            image.close()
            return
        }

        try {
            val bitmap = rotateBitmap(image.toBitmap(), image.imageInfo.rotationDegrees)
            val result = poseLandmarker.detect(BitmapImageBuilder(bitmap).build())
            if (result.landmarks().isEmpty()) {
                onFeedback(
                    LiveWorkoutFeedback(
                        repCount = repCount,
                        formScore = 0.0,
                        postureScore = 0.0,
                        stabilityScore = 0.0,
                        rangeOfMotion = 0.0,
                        correctRepPercentage = correctRepPercentage(),
                        mistakes = listOf("No full body detected"),
                        feedback = "Step back so your full body is visible to the camera.",
                        poseResult = null,
                    ),
                )
                return
            }
            processPose(result)
        } finally {
            image.close()
        }
    }

    fun close() {
        poseLandmarker.close()
    }

    private fun processPose(result: PoseLandmarkerResult) {
        val pose = result.landmarks().first()
        val elbowAngle = averageAngle(pose[11], pose[13], pose[15], pose[12], pose[14], pose[16])
        val kneeAngle = averageAngle(pose[23], pose[25], pose[27], pose[24], pose[26], pose[28])
        val bodyLine = averageAngle(pose[11], pose[23], pose[27], pose[12], pose[24], pose[28])
        val torsoAngle = averageAngle(pose[11], pose[23], pose[25], pose[12], pose[24], pose[26])
        val stability = stabilityScore(pose)

        val mistakes = mutableListOf<String>()
        val motionAngle = when (workoutType) {
            WorkoutType.PUSH_UP -> elbowAngle
            WorkoutType.SQUAT -> kneeAngle
            WorkoutType.LUNGE -> min(leftAngle(pose[23], pose[25], pose[27]), leftAngle(pose[24], pose[26], pose[28]))
        }

        val depthThreshold = when (workoutType) {
            WorkoutType.PUSH_UP -> 100.0
            WorkoutType.SQUAT -> 110.0
            WorkoutType.LUNGE -> 115.0
        }
        val extensionThreshold = when (workoutType) {
            WorkoutType.PUSH_UP -> 150.0
            WorkoutType.SQUAT -> 150.0
            WorkoutType.LUNGE -> 145.0
        }

        if (motionAngle <= depthThreshold) {
            movedIntoDepth = true
            minAngleDuringRep = min(minAngleDuringRep, motionAngle)
        }

        if (movedIntoDepth && motionAngle >= extensionThreshold) {
            repCount += 1
            completedReps += 1
            if (isGoodRep(bodyLine, torsoAngle, stability, minAngleDuringRep)) {
                goodReps += 1
            }
            movedIntoDepth = false
            minAngleDuringRep = 200.0
        }

        if (workoutType == WorkoutType.PUSH_UP && bodyLine < 155.0) {
            mistakes += "Keep your back flatter"
        }
        if (workoutType != WorkoutType.PUSH_UP && torsoAngle < 55.0) {
            mistakes += "Keep your chest up"
        }
        if (workoutType == WorkoutType.SQUAT && motionAngle > 120.0) {
            mistakes += "Go slightly deeper"
        }
        if (workoutType == WorkoutType.LUNGE && motionAngle > 115.0) {
            mistakes += "Lower until both knees get closer to 90 degrees"
        }
        if (stability < 70.0) {
            mistakes += "Slow down and stabilize"
        }

        val postureScore = when (workoutType) {
            WorkoutType.PUSH_UP -> bodyLine.scoreFromIdeal(175.0)
            WorkoutType.SQUAT, WorkoutType.LUNGE -> torsoAngle.scoreFromIdeal(75.0)
        }
        val formScore = ((motionAngle.depthScore(depthThreshold) + postureScore + stability) / 3.0).coerceIn(0.0, 100.0)
        val rangeOfMotion = minAngleDuringRep.depthScore(depthThreshold)
        lastFeedback = when {
            mistakes.isEmpty() -> "Great rep. Keep that same control."
            else -> mistakes.first()
        }

        onFeedback(
            LiveWorkoutFeedback(
                repCount = repCount,
                formScore = formScore,
                postureScore = postureScore,
                stabilityScore = stability,
                rangeOfMotion = rangeOfMotion,
                correctRepPercentage = correctRepPercentage(),
                mistakes = mistakes.distinct(),
                feedback = lastFeedback,
                poseResult = result,
            ),
        )
    }

    private fun isGoodRep(bodyLine: Double, torsoAngle: Double, stability: Double, minAngle: Double): Boolean {
        return when (workoutType) {
            WorkoutType.PUSH_UP -> minAngle <= 100.0 && bodyLine >= 155.0 && stability >= 70.0
            WorkoutType.SQUAT -> minAngle <= 110.0 && torsoAngle >= 55.0 && stability >= 70.0
            WorkoutType.LUNGE -> minAngle <= 115.0 && torsoAngle >= 55.0 && stability >= 70.0
        }
    }

    private fun correctRepPercentage(): Double {
        if (completedReps == 0) return 0.0
        return (goodReps.toDouble() / completedReps.toDouble()) * 100.0
    }

    private fun stabilityScore(pose: List<NormalizedLandmark>): Double {
        val shoulderDelta = abs(pose[11].y() - pose[12].y()) * 100.0
        val hipDelta = abs(pose[23].y() - pose[24].y()) * 100.0
        return (100.0 - (shoulderDelta + hipDelta) * 2.0).coerceIn(0.0, 100.0)
    }

    private fun averageAngle(
        leftA: NormalizedLandmark,
        leftB: NormalizedLandmark,
        leftC: NormalizedLandmark,
        rightA: NormalizedLandmark,
        rightB: NormalizedLandmark,
        rightC: NormalizedLandmark,
    ): Double = (leftAngle(leftA, leftB, leftC) + leftAngle(rightA, rightB, rightC)) / 2.0

    private fun leftAngle(a: NormalizedLandmark, b: NormalizedLandmark, c: NormalizedLandmark): Double {
        val radians = atan2(c.y() - b.y(), c.x() - b.x()) - atan2(a.y() - b.y(), a.x() - b.x())
        var angle = Math.toDegrees(radians.toDouble())
        angle = abs(angle)
        if (angle > 180.0) angle = 360.0 - angle
        return angle
    }

    private fun rotateBitmap(source: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees == 0) return source
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun baselineFeedback(): String = when (workoutType) {
        WorkoutType.PUSH_UP -> "Lower with control, then press back up."
        WorkoutType.SQUAT -> "Sit your hips back and keep your chest tall."
        WorkoutType.LUNGE -> "Lower under control and keep the front knee stacked."
    }

    private fun Double.scoreFromIdeal(ideal: Double): Double {
        return (100.0 - abs(this - ideal) * 1.2).coerceIn(0.0, 100.0)
    }

    private fun Double.depthScore(target: Double): Double {
        return when {
            this <= target -> 100.0
            else -> (100.0 - (this - target) * 1.5).coerceIn(0.0, 100.0)
        }
    }
}
