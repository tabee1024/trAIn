import com.android.example.camx.SimpleFrameAnalyzer
import androidx.camera.core.ImageProxy
import androidx.camera.core.ImageInfo
import android.graphics.Rect
import android.media.Image
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SimpleFrameAnalyzerTest {

    // Fake ImageProxy for unit testing (JVM only)
    private class FakeImageProxy(
        private val w: Int,
        private val h: Int
    ) : ImageProxy {

        var closed = false

        override fun getWidth(): Int = w
        override fun getHeight(): Int = h

        override fun close() {
            closed = true
        }

        override fun getImage(): Image? = null

        override fun getFormat(): Int = 0

        override fun getPlanes(): Array<ImageProxy.PlaneProxy> = emptyArray()

        override fun getCropRect(): Rect = Rect()

        override fun setCropRect(rect: Rect?) {}

        override fun getImageInfo(): ImageInfo {
            return object : ImageInfo {
                override fun getTimestamp(): Long = System.nanoTime()
                override fun getRotationDegrees(): Int = 0

                override fun getTagBundle(): androidx.camera.core.impl.TagBundle {
                    return androidx.camera.core.impl.TagBundle.emptyBundle()
                }

                override fun populateExifData(exifBuilder: androidx.camera.core.impl.utils.ExifData.Builder) {
                    // No EXIF data needed for testing
                }
            }
        }
    }



    // ---------- Test Case 1 ----------
    @Test
    fun `no info emitted when less than 1 second elapsed`() {
        val emittedInfo = mutableListOf<String>()

        val analyzer = SimpleFrameAnalyzer { infoText ->
            emittedInfo.add(infoText)
        }

        val image = FakeImageProxy(640, 480)

        analyzer.analyze(image) // First frame, <1s elapsed

        assertTrue(emittedInfo.isEmpty(), "Expected NO info text before 1 second")
        assertTrue(image.closed, "Image must always be closed")
    }

    // ---------- Test Case 2 ----------
    @Test
    fun `info emitted when at least 1 second elapsed`() {
        val emittedInfo = mutableListOf<String>()

        val analyzer = SimpleFrameAnalyzer { infoText ->
            emittedInfo.add(infoText)
        }

        val image = FakeImageProxy(1280, 720)

        // Call analyze() multiple times, sleeping a bit in between
        // Total sleep time will be > 1 second, so eventually elapsed >= 1000 ms
        repeat(5) {        // 5 times
            analyzer.analyze(image)
            Thread.sleep(300)   // 300 ms * 5 = 1500 ms total
        }

        // Now we expect at least one info string to have been emitted
        assertTrue(emittedInfo.isNotEmpty(), "Expected info after enough time has passed")

        val text = emittedInfo.first()
        assertTrue(text.contains("FPS"))
        assertTrue(text.contains("1280x720"))
        assertTrue(text.contains("Frame time"))
    }


    // ---------- Test Case 3 ----------
    @Test
    fun `image closed in normal execution`() {
        val analyzer = SimpleFrameAnalyzer { /* do nothing */ }
        val image = FakeImageProxy(800, 600)

        analyzer.analyze(image)

        assertTrue(image.closed, "Frame must be closed after normal analyze()")
    }

    // ---------- Test Case 4 ----------
    @Test
    fun `image closed even when onInfo throws`() {
        val analyzer = SimpleFrameAnalyzer {
            throw RuntimeException("mock UI update failed")
        }

        val image = FakeImageProxy(800, 600)

        assertDoesNotThrow { analyzer.analyze(image) }
        assertTrue(image.closed, "Frame must be closed EVEN if callback crashes")
    }
}
