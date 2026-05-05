package com.example.trainapp.camera

import android.app.AlertDialog
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Size
import android.view.Surface
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.AspectRatio
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.UseCaseGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import android.widget.Toast
import com.example.trainapp.R
import com.example.trainapp.data.TrainDatabaseHelper
import com.example.trainapp.model.WorkoutCatalog
import com.example.trainapp.model.WorkoutSummary
import com.example.trainapp.model.WorkoutType
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class CameraWorkoutActivity : ComponentActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var overlayView: PoseOverlayView
    private lateinit var repsText: TextView
    private lateinit var scoreText: TextView
    private lateinit var feedbackText: TextView
    private lateinit var finishButton: Button
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var analyzer: WorkoutFrameAnalyzer

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var videoFile: File? = null

    private var latestFeedback: LiveWorkoutFeedback? = null
    private var startTimeMillis: Long = 0L
    private var workoutId: Int = 1
    private var isFinishing = false

    private var isCameraStarted = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            startCamera()
        } else {
            feedbackText.text = "Camera permission is required for workout tracking."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_workout)

        previewView = findViewById(R.id.previewView)
        overlayView = findViewById(R.id.poseOverlay)
        repsText = findViewById(R.id.repsText)
        scoreText = findViewById(R.id.scoreText)
        feedbackText = findViewById(R.id.feedbackText)
        finishButton = findViewById(R.id.finishWorkoutButton)
        cameraExecutor = Executors.newSingleThreadExecutor()
        startTimeMillis = System.currentTimeMillis()
        workoutId = intent.getIntExtra(EXTRA_WORKOUT_ID, 1)

        finishButton.setOnClickListener { finishWorkout() }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    override fun onDestroy() {
        if (::analyzer.isInitialized) {
            analyzer.close()
        }
        cameraExecutor.shutdown()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            if (!isCameraStarted) {
                previewView.post { startCamera() }
            }
        } else {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                
                analyzer = WorkoutFrameAnalyzer(
                    context = this,
                    workoutType = WorkoutType.fromWorkoutId(workoutId),
                ) { feedback ->
                    latestFeedback = feedback
                    runOnUiThread {
                        overlayView.setPoseResult(feedback.poseResult)
                        repsText.text = "Reps: ${feedback.repCount}"
                        scoreText.text = "Form: ${feedback.formScore.roundToInt()}%"
                        feedbackText.text = feedback.feedback
                    }
                }

                val rotation = try { previewView.display.rotation } catch (e: Exception) { Surface.ROTATION_0 }

                val resolutionSelector = ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(Size(640, 480), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
                    )
                    .build()

                val preview = Preview.Builder()
                    .setTargetRotation(rotation)
                    .setResolutionSelector(resolutionSelector)
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetRotation(rotation)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .setResolutionSelector(resolutionSelector)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, analyzer)
                    }

                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.fromOrderedList(
                        listOf(Quality.SD, Quality.LOWEST),
                        FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                    ))
                    .build()
                videoCapture = VideoCapture.withOutput(recorder)

                val frontCameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()
                val backCameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                val useCaseGroup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageAnalysis)
                    .addUseCase(videoCapture!!)
                    .build()

                cameraProvider.unbindAll()
                val camera = try {
                    cameraProvider.bindToLifecycle(
                        this,
                        frontCameraSelector,
                        useCaseGroup
                    )
                } catch (frontCameraError: IllegalArgumentException) {
                    cameraProvider.bindToLifecycle(
                        this,
                        backCameraSelector,
                        useCaseGroup
                    )
                }
                
                isCameraStarted = true
                
                // Update overlay mirroring based on the actual camera being used
                val isFrontCamera = camera.cameraInfo.lensFacing == CameraSelector.LENS_FACING_FRONT
                overlayView.setMirrored(isFrontCamera)
                
                startRecording()
            } catch (error: Exception) {
                feedbackText.text = "Camera Error: ${error.localizedMessage}"
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startRecording() {
        val dir = getExternalFilesDir(null)
        if (dir == null) {
            Toast.makeText(this, "Storage not available", Toast.LENGTH_SHORT).show()
            return
        }
        val file = File(dir, "trAIN_${System.currentTimeMillis()}.mp4")
        videoFile = file
        
        val outputOptions = FileOutputOptions.Builder(file).build()
        val pendingRecording = videoCapture?.output?.prepareRecording(this, outputOptions)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            pendingRecording?.withAudioEnabled()
        }

        recording = pendingRecording?.start(ContextCompat.getMainExecutor(this)) { event ->
            if (event is VideoRecordEvent.Finalize) {
                // Remove the hasError() deletion logic here. 
                // We will handle the file preservation/deletion in the dialog instead.
                if (isFinishing) {
                    showSaveVideoDialog()
                }
            }
        }
    }

    private fun showSaveVideoDialog() {
        // Run on UI thread just in case
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Save Workout Video?")
                .setMessage("Would you like to save the video recording to your history for form review?")
                .setPositiveButton("Save Video") { _, _ ->
                    saveAndExit()
                }
                .setNegativeButton("Delete Video") { _, _ ->
                    videoFile?.delete()
                    videoFile = null
                    saveAndExit()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun finishWorkout() {
        if (isFinishing) return
        isFinishing = true
        
        finishButton.isEnabled = false
        finishButton.text = "Processing..."
        
        if (recording != null) {
            recording?.stop()
            recording = null
        } else {
            saveAndExit()
        }
    }

    private fun saveAndExit() {
        val workout = WorkoutCatalog.byId(workoutId)
        val durationMinutes = (((System.currentTimeMillis() - startTimeMillis) / 1000L) / 60L).toInt().coerceAtLeast(1)
        val feedback = latestFeedback
        val summary = WorkoutSummary(
            repetitions = feedback?.repCount ?: 0,
            durationMinutes = durationMinutes,
            caloriesBurned = maxOf(20, (durationMinutes * 6) + ((feedback?.repCount ?: 0) * 2)),
            formScore = feedback?.formScore ?: 0.0,
            postureScore = feedback?.postureScore ?: 0.0,
            stabilityScore = feedback?.stabilityScore ?: 0.0,
            rangeOfMotion = feedback?.rangeOfMotion ?: 0.0,
            correctRepPercentage = feedback?.correctRepPercentage ?: 0.0,
            mistakesDetected = feedback?.mistakes?.joinToString() ?: "No mistakes recorded",
            feedback = feedback?.feedback ?: "Workout completed.",
        )

        val user = FirebaseAuth.getInstance().currentUser
        TrainDatabaseHelper(this).saveWorkoutSession(
            authUid = user?.uid,
            fallbackName = user?.displayName ?: user?.email?.substringBefore("@").orEmpty().ifBlank { "trAIN athlete" },
            fallbackEmail = user?.email ?: "local@train.app",
            workout = workout,
            summary = summary,
            videoUri = videoFile?.absolutePath
        )

        setResult(
            RESULT_OK,
            intent.apply {
                putExtra("repetitions", summary.repetitions)
                putExtra("formScore", summary.formScore)
            },
        )
        finish()
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        const val EXTRA_WORKOUT_ID = "workout_id"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}
