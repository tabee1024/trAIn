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
 * Main entry point for the Camx pose detection application.
 *
 * This activity manages the full lifecycle of the camera and pose detection pipeline:
 * - Requesting and handling camera permissions at runtime
 * - Setting up the CameraX [Preview] and [ImageAnalysis] use cases
 * - Rotating each camera frame to match the device's display orientation
 * - Supporting front/back camera switching via a floating action button
 * - Forwarding processed frames to [PoseLandmarkerHelper] for pose detection
 * - Receiving detection results and passing them to [PoseOverlay] for rendering
 *
 * The activity implements [PoseLandmarkerHelper.PoseLandmarkerListener] to receive
 * asynchronous pose results and errors from the MediaPipe pipeline.
 *
 * @see PoseLandmarkerHelper
 * @see PoseOverlay
 */
class MainActivity : AppCompatActivity(), PoseLandmarkerHelper.PoseLandmarkerListener {

    /** View binding instance providing access to all views in [R.layout.activity_main]. */
    private lateinit var binding: ActivityMainBinding

    /**
     * Wrapper around the MediaPipe Pose Landmarker.
     * Initialized after the layout is set and destroyed in [onDestroy].
     */
    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper

    /**
     * Single-threaded executor used exclusively for [ImageAnalysis] frame processing.
     *
     * Keeping analysis off the main thread prevents UI jank. A single thread
     * is sufficient because [ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST] ensures
     * only one frame is queued at a time.
     */
    private lateinit var cameraExecutor: ExecutorService

    /**
     * Tracks which camera is currently active.
     *
     * `false` = back camera (default), `true` = front camera.
     * Toggled by [R.id.cameraSwitchButton] and used both to select the
     * [CameraSelector] and to mirror front-camera frames before processing.
     */
    private var isFrontCamera = false

    /**
     * Launcher for the [Manifest.permission.CAMERA] runtime permission request.
     *
     * On grant → calls [startCamera].
     * On denial → logs the event and updates the status text to inform the user.
     */
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startCamera()
            else {
                Log.e("Camera", "Permission denied")
                binding.coachText.text = "Camera permission denied"
            }
        }

    /**
     * Initializes the activity, sets up view binding, creates the camera executor,
     * initializes [PoseLandmarkerHelper], wires the camera switch button, and
     * starts the camera permission flow.
     *
     * @param savedInstanceState Previously saved instance state (unused).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        poseLandmarkerHelper = PoseLandmarkerHelper(this, this)

        // Toggle front/back camera and restart the camera pipeline on each press.
        binding.cameraSwitchButton.setOnClickListener {
            isFrontCamera = !isFrontCamera
            startCamera()
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * Binds the CameraX [Preview] and [ImageAnalysis] use cases to this activity's lifecycle.
     *
     * The camera selected is determined by [isFrontCamera]. Each frame delivered by
     * [ImageAnalysis] is:
     * 1. Converted to a [Bitmap] via [ImageProxy.toBitmap].
     * 2. Rotated by [ImageInfo.rotationDegrees] to correct for sensor orientation.
     * 3. Passed to [PoseLandmarkerHelper.detectLiveStream] for async pose detection.
     *
     * Uses [ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST] so that slow frames are
     * dropped rather than queued, keeping detection latency low.
     *
     * Uses [ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888] to enable the fast-path
     * conversion in [ImageProxy.toBitmap].
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

                        // Rotate the bitmap to match the device's display orientation.
                        // CameraX delivers frames in the sensor's native orientation
                        // (often landscape), so without this rotation MediaPipe would
                        // receive a sideways image and produce incorrect landmark positions.
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        val rotatedBitmap = if (rotationDegrees != 0) {
                            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                        } else {
                            bitmap
                        }

                        poseLandmarkerHelper.detectLiveStream(rotatedBitmap, isFrontCamera)
                        imageProxy.close()
                    }
                }

            val cameraSelector = if (isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                binding.coachText.text = "Pose detection running..."
            } catch (e: Exception) {
                Log.e("MainActivity", "Camera binding failed: ${e.message}")
                binding.coachText.text = "Camera error: ${e.message}"
            }

        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Called by [PoseLandmarkerHelper] when a frame has been processed and
     * pose landmarks are available (or absent).
     *
     * Always invoked on a background thread, so UI updates are dispatched
     * to the main thread via [runOnUiThread].
     *
     * @param result      The detected pose landmarks for the current frame.
     *                    May contain zero poses if no person is visible.
     * @param imageWidth  Width of the processed image in pixels.
     * @param imageHeight Height of the processed image in pixels.
     */
    override fun onResults(result: PoseLandmarkerResult, imageWidth: Int, imageHeight: Int) {
        runOnUiThread {
            binding.poseOverlay.setResults(result, imageWidth, imageHeight)
            binding.coachText.text = if (result.landmarks().isEmpty()) {
                "No pose detected"
            } else {
                "Pose detected ✓"
            }
        }
    }

    /**
     * Called by [PoseLandmarkerHelper] when an unrecoverable error occurs
     * during initialization or frame processing.
     *
     * The error message is logged and displayed in the status text view.
     *
     * @param error Human-readable description of the failure.
     */
    override fun onError(error: String) {
        Log.e("PoseLandmarker", error)
        runOnUiThread {
            binding.coachText.text = "Error: $error"
        }
    }

    /**
     * Releases resources when the activity is destroyed.
     *
     * Closes the [PoseLandmarkerHelper] to free MediaPipe native memory and
     * shuts down [cameraExecutor] to terminate the background analysis thread.
     * Failure to call these would cause memory leaks.
     */
    override fun onDestroy() {
        super.onDestroy()
        poseLandmarkerHelper.close()
        cameraExecutor.shutdown()
    }
}