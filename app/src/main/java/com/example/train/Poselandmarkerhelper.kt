package com.example.train

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * Manages initialization, frame processing, and cleanup of the MediaPipe
 * [PoseLandmarker] for use with a CameraX live stream.
 *
 * This class wraps the MediaPipe Tasks Vision API and exposes a simple interface
 * for submitting camera frames and receiving pose landmark results asynchronously.
 * It is configured to run in [RunningMode.LIVE_STREAM] mode, which means:
 * - [detectAsync] returns immediately without blocking the calling thread.
 * - Results are delivered via [PoseLandmarkerListener.onResults] on an internal
 *   MediaPipe thread — callers must marshal back to the main thread for UI updates.
 *
 * The model used is `pose_landmarker_full.task` which detects 33 body landmarks.
 * This file must be placed in the app's `assets/` directory.
 *
 * Typical usage:
 * ```kotlin
 * val helper = PoseLandmarkerHelper(context, listener)
 * // Per frame (from ImageAnalysis analyzer):
 * helper.detectLiveStream(bitmap, isFrontCamera = false)
 * // On activity destroy:
 * helper.close()
 * ```
 *
 * @param context  Android context used to load the model asset from `assets/`.
 * @param listener Callback interface for receiving results or errors.
 *
 * @see PoseLandmarkerListener
 * @see PoseLandmarker
 */
class PoseLandmarkerHelper(
    private val context: Context,
    private val listener: PoseLandmarkerListener
) {

    /**
     * The underlying MediaPipe [PoseLandmarker] instance.
     *
     * Nullable to allow safe cleanup and re-initialization. Access is always
     * guarded via null-safe calls to prevent crashes after [close] is called.
     */
    private var poseLandmarker: PoseLandmarker? = null

    /**
     * Callback interface for receiving pose detection results and errors.
     *
     * Both methods are invoked on a MediaPipe internal thread.
     * Implementations must use `runOnUiThread {}` for any UI updates.
     */
    interface PoseLandmarkerListener {

        /**
         * Called when pose landmarks have been detected in a frame.
         *
         * Invoked for every processed frame, even if no pose is found
         * (in which case [result].landmarks() will be empty).
         *
         * @param result      Contains normalized landmark coordinates for each
         *                    detected pose. Access via `result.landmarks()[poseIndex][landmarkIndex]`.
         * @param imageWidth  Width of the source image in pixels. Used by [PoseOverlay]
         *                    to correctly scale landmark coordinates.
         * @param imageHeight Height of the source image in pixels.
         */
        fun onResults(result: PoseLandmarkerResult, imageWidth: Int, imageHeight: Int)

        /**
         * Called when an error occurs during initialization or frame processing.
         *
         * @param error Human-readable error message describing the failure.
         */
        fun onError(error: String)
    }

    init {
        setupPoseLandmarker()
    }

    /**
     * Initializes (or re-initializes) the [PoseLandmarker] with default settings.
     *
     * Safe to call multiple times — any existing instance is closed before
     * a new one is created. This allows recovery from initialization failures.
     *
     * Configuration:
     * - Model: `pose_landmarker_full.task` (33 landmarks, best accuracy)
     * - Mode: [RunningMode.LIVE_STREAM] (non-blocking, async results)
     * - Max poses: 1 (optimized for single-person use cases)
     * - Detection confidence: 0.5
     * - Presence confidence: 0.5
     * - Tracking confidence: 0.5
     *
     * On failure, [PoseLandmarkerListener.onError] is called with the exception message.
     */
    fun setupPoseLandmarker() {
        // Close any existing instance before creating a new one to prevent leaks.
        poseLandmarker?.close()
        poseLandmarker = null

        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("pose_landmarker_full.task")
                .build()

            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumPoses(1)
                .setMinPoseDetectionConfidence(0.5f)
                .setMinPosePresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setResultListener { result, image ->
                    listener.onResults(result, image.width, image.height)
                }
                .setErrorListener { error ->
                    listener.onError(error.message ?: "Unknown error")
                }
                .build()

            poseLandmarker = PoseLandmarker.createFromOptions(context, options)

        } catch (e: Exception) {
            listener.onError("Failed to initialize: ${e.message}")
        }
    }

    /**
     * Submits a camera frame for asynchronous pose landmark detection.
     *
     * This method returns immediately. The detection result will be delivered
     * to [PoseLandmarkerListener.onResults] once processing is complete.
     *
     * If [isFrontCamera] is `true`, the bitmap is horizontally flipped before
     * processing. This corrects the mirroring inherent in front-facing camera
     * output so that left/right body landmarks are correctly assigned.
     *
     * The bitmap should already be rotated to match the display orientation
     * (this is handled in [MainActivity.startCamera] using [ImageInfo.rotationDegrees]).
     *
     * If [poseLandmarker] is null (e.g. after [close] was called), this method
     * returns silently to avoid a crash.
     *
     * @param bitmap        The camera frame as an [Bitmap], already display-rotated.
     * @param isFrontCamera Whether the frame originated from the front-facing camera.
     */
    fun detectLiveStream(bitmap: Bitmap, isFrontCamera: Boolean) {
        // Guard against calling detectAsync after close().
        val landmarker = poseLandmarker ?: return

        // Flip the image horizontally for front camera to correct left/right mirroring.
        // Without this, landmarks 11 (left shoulder) and 12 (right shoulder) would
        // appear swapped relative to the person's actual body.
        val processedBitmap = if (isFrontCamera) {
            val matrix = Matrix().apply { postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }

        val frameTime = SystemClock.uptimeMillis()
        val mpImage = BitmapImageBuilder(processedBitmap).build()
        landmarker.detectAsync(mpImage, frameTime)
    }

    /**
     * Releases the underlying [PoseLandmarker] and frees all associated native memory.
     *
     * Must be called in [MainActivity.onDestroy] to prevent memory leaks.
     * After calling this method, [detectLiveStream] will silently no-op until
     * [setupPoseLandmarker] is called again.
     */
    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }
}