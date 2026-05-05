package com.example.trainapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.trainapp.ui.theme.TrAInAppTheme
import com.example.trainapp.ui.theme.Beige

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Log to Logcat so we can see it starting
        android.util.Log.d("TrAInApp", "MainActivity onCreate started")
        
        setContent {
            TrAInAppTheme(dynamicColor = false) {
                // Using a simple Surface here to ensure we see a background color immediately
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Beige
                ) {
                    TrainApp()
                }
            }
        }
    }
}
