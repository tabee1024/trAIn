package com.example.train

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.train.ui.theme.*
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun Lunges(
    videoUrl: String = "https://www.youtube.com/watch?v=wrwwXE_x-pQ"
) {
    // FIX: Initialize context here to resolve the "Unresolved reference" error
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(color = OffWhite) // Using your brand Beige background
    ) {
        // Top Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            // Ensure you have a file named lunges.png/jpg in res/drawable
            Image(
                painter = painterResource(id = R.drawable.lunges),
                contentDescription = "Lunges",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            // Subtle overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.15f))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Workout details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Lunges",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = CharcoalBlue
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "A lower-body move that strengthens legs and glutes.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontStyle = Italic
                ),
                fontSize = 14.sp,
                color = CharcoalBlue.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Start workout logic here */ },
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Start Workout", color = Color.White)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.PlayArrow, contentDescription = null)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CharcoalBlue
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 1.dp, color = CharcoalBlue.copy(alpha = 0.1f))

            WorkoutInfoRow(label = "Difficulty", value = "Beginner")
            WorkoutInfoRow(label = "Target", value = "Quads & Glutes")
            WorkoutInfoRow(label = "Sets", value = "3–4 sets")
            WorkoutInfoRow(label = "Reps", value = "12 reps per leg")

            Spacer(modifier = Modifier.height(24.dp))

            // Video Tutorial Button
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, videoUrl.toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, CharcoalBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Watch Video Tutorial", color = CharcoalBlue)
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CharcoalBlue)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "How to Perform",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CharcoalBlue
            )
            Spacer(modifier = Modifier.height(8.dp))

            StepItem("1", "Stand tall with your feet hip-width apart.")
            StepItem("2", "Step forward with one leg and lower your hips.")
            StepItem("3", "Bend both knees to about 90 degrees.")
            StepItem("4", "Keep your torso upright and core tight.")
            StepItem("5", "Push through your front heel to return to standing.")

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// --- Helper Composables to fix compilation ---

@Composable
fun LungeInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.SemiBold, color = CharcoalBlue)
        Text(text = value, color = CharcoalBlue.copy(alpha = 0.7f))
    }
}

@Composable
fun StepItem(number: String, instruction: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Surface(
            shape = CircleShape,
            color = SteelBlue,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(number, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = instruction, color = CharcoalBlue, fontSize = 14.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun LungesPreview() {
    TrAInAppTheme {
        Lunges()
    }
}
