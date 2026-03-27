package com.example.train

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.train.ui.theme.*

@Composable
fun Progress(
    reps: Int,
    accuracy: Int,
    timeSpent: String,
    onExit: () -> Unit,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(color = Color.White)
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Top content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(90.dp))

            Text(
                text = "Workout Summary",
                color = StoneGrey,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            WorkoutStats(
                reps = reps,
                accuracy = accuracy,
                timeSpent = timeSpent
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Accuracy Indicator
            AccuracyIndicator(accuracy = accuracy)

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onExit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StoneGrey, // background color
                        contentColor = Color.White // icon text color
                    )
                ) {
                    Text(text = "Go to Home")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StoneGrey, // background color
                        contentColor = Color.White // icon text color
                    )
                ) {
                    Text(text = "Restart Workout")
                }
            }
        }
    }
}

@Composable
fun WorkoutStats(
    reps: Int,
    accuracy: Int,
    timeSpent: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatCard("Reps Completed", reps.toString())
        Spacer(modifier = Modifier.height(16.dp))

        StatCard("Accuracy", "$accuracy%")
        Spacer(modifier = Modifier.height(16.dp))

        StatCard("Time Spent", timeSpent)
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(0.5.dp, color = StoneGrey),
        colors = CardDefaults.cardColors(containerColor = SoftBlueTint)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = StoneGrey)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = StoneGrey
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressPreview() {
    TrAInAppTheme {
        Progress(
            reps = 30,
            accuracy = 55,
            timeSpent = "08:14",
            onExit = {},
            onRestart = {}
        )
    }
}
@Composable
fun AccuracyIndicator(accuracy: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Background Circle (Track)
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(120.dp),
                color = Color.LightGray.copy(alpha = 0.3f),
                strokeWidth = 8.dp,
            )
            // Actual Progress Circle
            CircularProgressIndicator(
                progress = { accuracy / 100f },
                modifier = Modifier.size(120.dp),
                color = StoneGrey, // Matching your theme's dark brown
                strokeWidth = 8.dp,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$accuracy%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = StoneGrey
                )
                Text(
                    text = "Accuracy",
                    style = MaterialTheme.typography.labelSmall,
                    color = StoneGrey.copy(alpha = 0.7f)
                )
            }
        }
    }
}