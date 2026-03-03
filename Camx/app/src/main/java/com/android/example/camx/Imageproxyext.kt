package com.android.example.camx

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * Converts this [ImageProxy] frame into a [Bitmap] suitable for use with MediaPipe.
 *
 * Two conversion paths are supported depending on the image format:
 *
 * **Fast path — [PixelFormat.RGBA_8888]:**
 * A direct buffer copy into a [Bitmap.Config.ARGB_8888] bitmap. No color space
 * conversion or compression is needed. This is the preferred path and is activated
 * by setting [androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888]
 * on the [androidx.camera.core.ImageAnalysis] builder.
 *
 * **Fallback path — [ImageFormat.YUV_420_888]:**
 * Converts the YUV frame to NV21 format, compresses it to JPEG at 50% quality,
 * then decodes back to a [Bitmap]. This is slower due to compression overhead
 * but is kept for compatibility with devices or configurations that do not
 * support RGBA_8888 output.
 *
 * The buffer is rewound before reading to ensure the position is at index 0,
 * preventing blank or corrupted bitmaps that can occur if the buffer position
 * was left non-zero by a previous read.
 *
 * @receiver The [ImageProxy] from the CameraX [androidx.camera.core.ImageAnalysis] analyzer.
 * @return A [Bitmap] in [Bitmap.Config.ARGB_8888] format.
 * @throws IllegalStateException If the underlying [Image] is null in YUV path.
 * @throws IllegalArgumentException If the image format is not RGBA_8888 or YUV_420_888.
 */
fun ImageProxy.toBitmap(): Bitmap {
    // Fast path: RGBA_8888 (set via OUTPUT_IMAGE_FORMAT_RGBA_8888 in ImageAnalysis).
    // A single buffer copy — no YUV math, no JPEG encode/decode.
    if (format == PixelFormat.RGBA_8888) {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // Rewind before copy — avoids blank bitmap if buffer position is non-zero.
        planes[0].buffer.rewind()
        bitmap.copyPixelsFromBuffer(planes[0].buffer)
        return bitmap
    }

    // Fallback: YUV_420_888 (slower JPEG path, kept for compatibility).
    val image = this.image ?: throw IllegalStateException("Image is null")
    if (format != ImageFormat.YUV_420_888) {
        throw IllegalArgumentException("Unsupported image format: $format")
    }
    val nv21 = yuv420ToNv21(image)
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 50, out)
    return BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
}

/**
 * Converts an [ImageFormat.YUV_420_888] [Image] into an NV21 byte array.
 *
 * NV21 is a YUV format where the Y plane comes first, followed by interleaved
 * V and U chroma samples. It is the input format required by [YuvImage] for
 * JPEG compression.
 *
 * YUV_420_888 is a flexible Android format where the U and V planes may be
 * stored with varying `pixelStride` values depending on the device:
 *
 * - **pixelStride == 2 (fast path):** U and V are already interleaved in memory.
 *   The V buffer is copied directly as the chroma plane (NV21 = Y + interleaved VU).
 *
 * - **pixelStride == 1 (slow path):** U and V are stored as separate planar arrays.
 *   V and U bytes are manually interleaved one-by-one to construct the NV21 chroma plane.
 *
 * All plane buffers are rewound before reading to reset their position to index 0.
 *
 * @param image The [Image] from the YUV_420_888 [ImageProxy]. Must have exactly 3 planes.
 * @return A byte array in NV21 format of size `width * height * 3 / 2`.
 */
private fun yuv420ToNv21(image: Image): ByteArray {
    val yPlane = image.planes[0]
    val uPlane = image.planes[1]
    val vPlane = image.planes[2]

    // Rewind all buffers to ensure we read from the start.
    val yBuffer = yPlane.buffer.also { it.rewind() }
    val uBuffer = uPlane.buffer.also { it.rewind() }
    val vBuffer = vPlane.buffer.also { it.rewind() }

    val ySize = yBuffer.remaining()
    val width = image.width
    val height = image.height
    val nv21 = ByteArray(ySize + width * height / 2)

    // Copy Y plane as-is.
    yBuffer.get(nv21, 0, ySize)

    val vPixelStride = vPlane.pixelStride
    val uPixelStride = uPlane.pixelStride
    val chromaSize = width * height / 4
    var pos = ySize

    if (vPixelStride == 2 && uPixelStride == 2) {
        // Fast path: V buffer is already laid out as interleaved VU pairs in memory.
        // Copy it directly as the NV21 chroma plane.
        // This is the common case on most Android devices using camera2 or CameraX.
        val vBytes = ByteArray(vBuffer.remaining())
        vBuffer.get(vBytes)
        System.arraycopy(vBytes, 0, nv21, ySize, width * height / 2)
    } else {
        // Slow path: V and U planes are stored separately (pixelStride == 1).
        // Manually interleave them in V, U, V, U... order to form NV21 chroma.
        val vBytes = ByteArray(vBuffer.remaining())
        val uBytes = ByteArray(uBuffer.remaining())
        vBuffer.get(vBytes)
        uBuffer.get(uBytes)
        for (i in 0 until chromaSize) {
            nv21[pos++] = vBytes[i]
            nv21[pos++] = uBytes[i]
        }
    }

    return nv21
}