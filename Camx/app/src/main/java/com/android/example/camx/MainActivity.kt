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
 * Main entry point for the Camx AI workout coach application.
 *
 * Supports multiple exercises via the [ExerciseCounter] interface. The user
 * can switch between exercises using the exercise toggle button in the UI.
 * Each switch resets the current counter and begins fresh.
 *
 * Current supported exercises:
 * - [PushUpCounter] — tracks elbow flexion angle
 * - [SquatCounter]  — tracks knee flexion angle
 *
 * The full pipeline per frame:
 * 1. CameraX captures the frame
 * 2. Frame is rotated to match display orientation
 * 3. [PoseLandmarkerHelper] runs MediaPipe pose detection asynchronously
 * 4. [activeCounter] analyzes landmarks → rep count + form feedback
 * 5. [PoseOverlay] draws the skeleton + joint angle
 * 6. [R.id.coachText] shows rep count, phase, and feedback
 *
 * @see ExerciseCounter
 * @see PushUpCounter
 * @see SquatCounter
 */
class MainActivity : AppCompatActivity(), PoseLandmarkerHelper.PoseLandmarkerListener {

    /** View binding for [R.layout.activity_main]. */
    private lateinit var binding: ActivityMainBinding

    /** Manages MediaPipe Pose Landmarker initialization and frame submission. */
    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper

    /** Background executor for [ImageAnalysis] — single thread to match backpressure strategy. */
    private lateinit var cameraExecutor: ExecutorService

    /** Whether the front camera is active. Toggled by the camera switch FAB. */
    private var isFrontCamera = false

    // --- Exercise counters ---

    private val pushUpCounter = PushUpCounter()
    private val squatCounter  = SquatCounter()

    /**
     * All available exercise counters in toggle order.
     * Add new exercises here to automatically include them in the cycle.
     */
    private val exercises: List<ExerciseCounter> = listOf(pushUpCounter, squatCounter)

    /** Index into [exercises] for the currently active exercise. */
    private var exerciseIndex = 0

    /**
     * The currently active exercise counter.
     * Switching exercises resets this counter and updates the UI label.
     */
    private val activeCounter: ExerciseCounter
        get() = exercises[exerciseIndex]

    /** Handles the camera permission request result. */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else {
                Log.e("Camera", "Permission denied")
                binding.coachText.text = "Camera permission denied"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        poseLandmarkerHelper = PoseLandmarkerHelper(this, this)

        // Camera switch button — toggle front/back
        binding.cameraSwitchButton.setOnClickListener {
            isFrontCamera = !isFrontCamera
            startCamera()
        }

        // Exercise toggle button — cycle through available exercises
        binding.exerciseToggleButton.setOnClickListener {
            exerciseIndex = (exerciseIndex + 1) % exercises.size
            activeCounter.reset()
            binding.exerciseToggleButton.text = activeCounter.exerciseName
            binding.coachText.text = "Switched to ${activeCounter.exerciseName} — get into position!"
        }

        // Set initial button label
        binding.exerciseToggleButton.text = activeCounter.exerciseName

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * Binds CameraX [Preview] and [ImageAnalysis] to this activity's lifecycle.
     *
     * Each frame is:
     * 1. Converted to [Bitmap] via [ImageProxy.toBitmap]
     * 2. Rotated to match display orientation using [ImageInfo.rotationDegrees]
     * 3. Passed to [PoseLandmarkerHelper.detectLiveStream] for async detection
     */
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

                        // Rotate to correct for sensor orientation (often landscape).
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

    /**
     * Called by [PoseLandmarkerHelper] on every processed frame.
     *
     * Feeds landmarks into [activeCounter] and updates:
     * - [PoseOverlay] with the skeleton and current joint angle
     * - [R.id.coachText] with rep count, phase label, and form feedback
     *
     * Always invoked on a MediaPipe background thread — UI updates
     * dispatched via [runOnUiThread].
     *
     * @param result      Pose detection result containing normalized landmarks.
     * @param imageWidth  Width of the analyzed frame in pixels.
     * @param imageHeight Height of the analyzed frame in pixels.
     */
    override fun onResults(result: PoseLandmarkerResult, imageWidth: Int, imageHeight: Int) {
        runOnUiThread {
            binding.poseOverlay.setResults(result, imageWidth, imageHeight)

            if (result.landmarks().isEmpty()) {
                binding.coachText.text = "No pose detected — step back!"
                return@runOnUiThread
            }

            val coachResult = activeCounter.update(result.landmarks()[0])

            binding.coachText.text =
                "${activeCounter.exerciseName}  |  Reps: ${coachResult.repCount}  |  ${coachResult.phaseLabel}\n${coachResult.feedback}"

            binding.poseOverlay.setCoachData(coachResult.primaryAngle)
        }
    }

    /**
     * Called when MediaPipe encounters an unrecoverable error.
     *
     * @param error Human-readable error description.
     */
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