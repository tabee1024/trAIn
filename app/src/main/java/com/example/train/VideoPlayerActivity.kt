package com.example.train

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import java.io.File

class VideoPlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val videoPath = intent.getStringExtra("video_path")
        
        if (videoPath == null) {
            Toast.makeText(this, "Video path missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val videoFile = File(videoPath)
        if (!videoFile.exists()) {
            Toast.makeText(this, "Video file not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val videoView = VideoView(this)
        setContentView(videoView)

        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        try {
            // Use FileProvider to get a content URI that the VideoView can access
            val contentUri: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                videoFile
            )
            
            videoView.setVideoURI(contentUri)
            videoView.setOnPreparedListener { it.start() }
            videoView.setOnErrorListener { _, _, _ ->
                Toast.makeText(this, "Error playing video", Toast.LENGTH_SHORT).show()
                finish()
                true
            }
            videoView.setOnCompletionListener { finish() }
        } catch (e: Exception) {
            Toast.makeText(this, "Permission error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
