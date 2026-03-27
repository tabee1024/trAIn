package com.example.trainapp

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainapp.ui.theme.Brown
import com.example.trainapp.ui.theme.DBrown
import com.example.trainapp.ui.theme.Emperor
import com.example.trainapp.ui.theme.Swirl
import com.example.trainapp.ui.theme.TrAInAppTheme

@Composable
fun Squats(
    videoUrl: String = "https://www.youtube.com/watch?v=YaXPRqUwItQ&t=10s"
){
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .background(color = Color.White)
    ) {
        // Top Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        ) {
            Image(painter = painterResource(id = R.drawable.squats),
                contentDescription = "Squats",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            //Black shade overlay
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.40f))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Workout details
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Squats",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = DBrown
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "A foundational exercise that builds strength in the legs and core.",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontStyle = Italic),
                fontSize = 14.sp,
                color = Emperor
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = DBrown)
            ) {
                Text(text = "Start Workout", color = Swirl, style = MaterialTheme.typography.bodyLarge)
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Arrow"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DBrown
            )
            Spacer(modifier = Modifier.height(8.dp))

            WorkoutInfoRow(label = "Difficulty:", value = "Beginner - Intermediate")
            WorkoutInfoRow(label = "Target Muscles:", value = "Quads, Glutes, Hamstrings, Core")
            WorkoutInfoRow(label = "Recommended Sets:", value = "3–4 sets")
            WorkoutInfoRow(label = "Reps per Set:", value = "12–20 reps")
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                    context.startActivity(intent)
                },
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = BorderStroke(width = 1.dp, color = Brown)
            ) {
                Text(text = "Watch Video Tutorial",
                    color = DBrown,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "External link",
                    tint = DBrown
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "How to Perform",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = DBrown )

            Spacer(modifier = Modifier.height(8.dp))

            StepItem("Stand with your feet shoulder‑width apart.")
            StepItem("Keep your chest up and core engaged.")
            StepItem("Push your hips back as if sitting into a chair.")
            StepItem("Lower until your thighs are parallel to the floor.")
            StepItem("Drive through your heels to return to standing.")

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SquatsPreview() {
    TrAInAppTheme {
        Squats()
    }
}
