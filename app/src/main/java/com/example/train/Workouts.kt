package com.example.train

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.train.ui.theme.StoneGrey
import com.example.train.ui.theme.AccentGold

@Composable
fun Workouts(onWorkoutSelected: (String) -> Unit) {
    // State to toggle between the Start Button and the Workout List
    var isStarted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (!isStarted) {
            // 1. THE LARGE CENTERED START BUTTON
            Button(
                onClick = { isStarted = true },
                modifier = Modifier
                    .width(280.dp)
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text(
                    text = "Start Workouts",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        } else {
            // 2. THE WORKOUT LIST (Visible after clicking start)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Workouts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = StoneGrey
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable list of full-bleed workout cards
                WorkoutList(onWorkoutSelected = onWorkoutSelected)
            }
        }
    }
}

@Composable
fun WorkoutList(onWorkoutSelected: (String) -> Unit) {
    val workouts = listOf(
        Workout(1, "Push up", "20 min", "Intermediate", R.drawable.push_up, Screens.PushUp.screen),
        Workout(2, "Squats", "35 min", "Beginner", R.drawable.squats, Screens.Squats.screen),
        Workout(3, "Lunges", "15 min", "Beginner", R.drawable.lunges, Screens.Lunges.screen)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        items(workouts) { workout ->
            WorkoutItem(workout, onWorkoutSelected)
        }
    }
}

data class Workout(
    val id: Int,
    val title: String,
    val duration: String,
    val difficulty: String,
    val imageRes: Int, // Using Int for direct R.drawable resource
    val screen: String
)

@Composable
fun WorkoutItem(workout: Workout, onWorkoutSelected: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onWorkoutSelected(workout.screen) },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image - Set to Crop to fill the card
            Image(
                painter = painterResource(id = workout.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark Overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )

            // Text Content positioned at the bottom left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = workout.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Duration: ${workout.duration}",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = workout.difficulty,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // "PUSH YOUR LIMITS" Watermark
            Text(
                text = "PUSH YOUR\nLIMITS",
                color = Color.White.copy(alpha = 0.15f),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 22.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
            )
        }
    }
}