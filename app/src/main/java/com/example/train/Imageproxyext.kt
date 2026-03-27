package com.example.train

import android.graphics.*
import android.media.Image
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

/**
 * Converts this [ImageProxy] frame into a [Bitmap] suitable for use with MediaPipe.
 *
 * @OptIn(ExperimentalGetImage::class) is required because accessing the
 * underlying android.media.Image from an ImageProxy is an experimental
 * feature in CameraX.
 */
@OptIn(ExperimentalGetImage::class)
fun ImageProxy.toBitmap(): Bitmap {

    // Fast path: RGBA_8888
    // This path is used when ImageAnalysis is configured with OUTPUT_IMAGE_FORMAT_RGBA_8888.
    if (format == PixelFormat.RGBA_8888) {
        // We use 'this.width' and 'this.height' to explicitly reference the ImageProxy
        // properties and avoid "Unresolved reference" errors.
        val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)

        // Rewind the buffer to ensure we read from the beginning (index 0).
        planes[0].buffer.rewind()
        bitmap.copyPixelsFromBuffer(planes[0].buffer)
        return bitmap
    }

    // Fallback path: YUV_420_888
    // Used for compatibility if the device doesn't support direct RGBA output.
    val image = this.image ?: throw IllegalStateException("Underlying Image is null")

    if (format != ImageFormat.YUV_420_888) {
        throw IllegalArgumentException("Unsupported image format: $format")
    }

    // 1. Convert YUV to NV21 byte array
    val nv21 = yuv420ToNv21(image)

    // 2. Wrap in YuvImage to enable JPEG compression
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)

    // 3. Compress to JPEG and decode into a Bitmap
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, this.width, this.height), 50, out)
    val imageBytes = out.toByteArray()

    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

/**
 * Helper to convert YUV_420_888 to NV21.
 * NV21 is the format required by the YuvImage helper class.
 */
private fun yuv420ToNv21(image: Image): ByteArray {
    val yPlane = image.planes[0]
    val uPlane = image.planes[1]
    val vPlane = image.planes[2]

    val yBuffer = yPlane.buffer.also { it.rewind() }
    val uBuffer = uPlane.buffer.also { it.rewind() }
    val vBuffer = vPlane.buffer.also { it.rewind() }

    val ySize = yBuffer.remaining()
    val width = image.width
    val height = image.height

    // NV21 is 1.5 bytes per pixel (Y + interleaved VU)
    val nv21 = ByteArray(ySize + width * height / 2)

    // Copy Y plane (Luminance)
    yBuffer.get(nv21, 0, ySize)

    val vPixelStride = vPlane.pixelStride
    val uPixelStride = uPlane.pixelStride
    val chromaSize = width * height / 4
    var pos = ySize

    if (vPixelStride == 2 && uPixelStride == 2) {
        // Fast path: V and U are already interleaved (pixelStride 2)
        val vBytes = ByteArray(vBuffer.remaining())
        vBuffer.get(vBytes)
        System.arraycopy(vBytes, 0, nv21, ySize, width * height / 2)
    } else {
        // Slow path: V and U are separate (pixelStride 1), manually interleave them
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