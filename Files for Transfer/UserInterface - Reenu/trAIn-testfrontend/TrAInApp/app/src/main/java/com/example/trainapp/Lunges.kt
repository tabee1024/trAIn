package com.example.trainapp

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.core.net.toUri

@Composable
fun Lunges(
        videoUrl: String = "https://www.youtube.com/watch?v=wrwwXE_x-pQ"
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
            Image(painter = painterResource(id = R.drawable.lunges),
                contentDescription = "Lunges",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            //Black shade overlay
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.10f))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Workout details
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Lunges",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = DBrown
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "A lower‑body move that strengthens legs and glutes.",
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
            WorkoutInfoRow(label = "Target Muscles:", value = "Quads, Glutes, Hamstrings, Calves")
            WorkoutInfoRow(label = "Recommended Sets:", value = "3–4 sets")
            WorkoutInfoRow(label = "Reps per Set:", value = "10–15 reps per leg")
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, videoUrl.toUri())
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

            StepItem("Stand tall with your feet hip‑width apart.")
            StepItem("Step forward with one leg and lower your hips.")
            StepItem("Bend both knees to about 90 degrees.")
            StepItem("Keep your torso upright and core tight.")
            StepItem("Push through your front heel to return to standing.")

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LungesPreview() {
    TrAInAppTheme {
        Lunges()
    }
}
