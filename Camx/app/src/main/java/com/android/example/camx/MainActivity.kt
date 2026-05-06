package com.android.example.camx

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.android.example.camx.databinding.ActivityMainBinding
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Main entry point for the Camx AI workout coach.
 *
 * Supported exercises (toggle order): Push-Ups → Squats → Lunges → Push-Ups
 *
 * The mini preview panel in [PoseOverlay] continuously animates the current
 * exercise using [GhostModel.interpolate], demonstrating the movement with a
 * glowing human-shaped ghost figure.
 *
 * Exercise color themes:
 * - Push-Ups → blue ghost
 * - Squats   → purple ghost
 * - Lunges   → green ghost
 */
class MainActivity : AppCompatActivity(), PoseLandmarkerHelper.PoseLandmarkerListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper
    private lateinit var cameraExecutor: ExecutorService
    private var isFrontCamera = false

    // ── Exercises ─────────────────────────────────────────────────────────────

    private val pushUpCounter = PushUpCounter()
    private val squatCounter  = SquatCounter()
    private val lungeCounter  = LungeCounter()

    private val exercises: List<ExerciseCounter> = listOf(
        pushUpCounter,
        squatCounter,
        lungeCounter
    )

    private var exerciseIndex = 0

    private val activeCounter: ExerciseCounter
        get() = exercises[exerciseIndex]

    private val nextExerciseName: String
        get() = exercises[(exerciseIndex + 1) % exercises.size].exerciseName

    private var lastCoachResult: ExerciseCounter.CoachResult? = null

    // ── Permission ────────────────────────────────────────────────────────────

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else {
                Log.e("Camera", "Permission denied")
                binding.coachText.text = "Camera permission denied"
            }
        }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        poseLandmarkerHelper = PoseLandmarkerHelper(this, this)

        // Set initial exercise on the overlay so ghost animates immediately
        binding.poseOverlay.setExercise(activeCounter.exerciseName)

        binding.cameraSwitchButton.setOnClickListener {
            isFrontCamera = !isFrontCamera
            startCamera()
        }

        binding.exerciseToggleButton.setOnClickListener {
            exerciseIndex = (exerciseIndex + 1) % exercises.size
            activeCounter.reset()
            lastCoachResult = null
            binding.exerciseToggleButton.text = activeCounter.exerciseName
            binding.coachText.text = "Switched to ${activeCounter.exerciseName}!"

            // ✅ Tell overlay which exercise is now active so ghost updates
            binding.poseOverlay.setExercise(activeCounter.exerciseName)
            binding.poseOverlay.clear()
        }

        binding.exerciseToggleButton.text = activeCounter.exerciseName

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        val bitmap = imageProxy.toBitmap()
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        val rotatedBitmap = if (rotationDegrees != 0) {
                            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                        } else bitmap
                        poseLandmarkerHelper.detectLiveStream(rotatedBitmap, isFrontCamera)
                        imageProxy.close()
                    }
                }

            val cameraSelector = if (isFrontCamera)
                CameraSelector.DEFAULT_FRONT_CAMERA
            else
                CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                binding.coachText.text = "Get into ${activeCounter.exerciseName} position!"
            } catch (e: Exception) {
                Log.e("MainActivity", "Camera binding failed: ${e.message}")
                binding.coachText.text = "Camera error: ${e.message}"
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onResults(result: PoseLandmarkerResult, imageWidth: Int, imageHeight: Int) {
        runOnUiThread {
            binding.poseOverlay.setResults(result, imageWidth, imageHeight)

            if (result.landmarks().isEmpty()) {
                binding.coachText.text = "No pose detected — step back!"
                val prev = lastCoachResult
                if (prev != null) {
                    val cur  = GhostModel.getGhost(activeCounter.exerciseName, prev.phaseLabel)
                    val next = GhostModel.getNextGhost(activeCounter.exerciseName, prev.phaseLabel, nextExerciseName)
                    binding.poseOverlay.setCoachData(prev.primaryAngle, cur, next)
                }
                return@runOnUiThread
            }

            val coachResult = activeCounter.update(result.landmarks()[0])
            lastCoachResult = coachResult

            val currentGhost = GhostModel.getGhost(activeCounter.exerciseName, coachResult.phaseLabel)
            val nextGhost    = GhostModel.getNextGhost(activeCounter.exerciseName, coachResult.phaseLabel, nextExerciseName)
            binding.poseOverlay.setCoachData(coachResult.primaryAngle, currentGhost, nextGhost)

            // Show L/R breakdown for lunges
            val repText = if (activeCounter is LungeCounter) {
                val lc = activeCounter as LungeCounter
                "L: ${lc.leftRepCount}  R: ${lc.rightRepCount}"
            } else {
                "Reps: ${coachResult.repCount}"
            }

            binding.coachText.text =
                "${activeCounter.exerciseName}  |  $repText  |  ${coachResult.phaseLabel}\n${coachResult.feedback}"
        }
    }

    override fun onError(error: String) {
        Log.e("PoseLandmarker", error)
        runOnUiThread { binding.coachText.text = "Error: $error" }
    }

    override fun onDestroy() {
        super.onDestroy()
        poseLandmarkerHelper.close()
        cameraExecutor.shutdown()
    }
}