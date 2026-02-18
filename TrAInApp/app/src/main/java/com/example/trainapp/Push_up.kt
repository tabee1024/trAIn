package com.example.trainapp

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainapp.ui.theme.Beige
import com.example.trainapp.ui.theme.Brown
import com.example.trainapp.ui.theme.DBrown
import com.example.trainapp.ui.theme.Emperor
import com.example.trainapp.ui.theme.Swirl
//import androidx.navigation.compose.rememberNavController
import com.example.trainapp.ui.theme.TrAInAppTheme

@Composable
fun Push_up( ){
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .background(color = Beige)
    ) {
        // Top Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        ) {
            Image(painter = painterResource(id = R.drawable.push_up),
                contentDescription = "Push Up",
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
                text = "Push Up",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = DBrown
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "A classic upper‑body exercise that strengthens your chest, shoulders, triceps, and core.",
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

            WorkoutInfoRow(label = "Difficulty:", value = "Intermediate")
            WorkoutInfoRow(label = "Target Muscles:", value = "Chest, Shoulders, Triceps, Core")
            WorkoutInfoRow(label = "Recommended Sets:", value = "3–4 sets")
            WorkoutInfoRow(label = "Reps per Set:", value = "10–20 reps")
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "How to Perform",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DBrown
            )
            Spacer(modifier = Modifier.height(8.dp))

            StepItem("Start in a high plank position with your hands shoulder‑width apart.")
            StepItem("Lower your body until your chest nearly touches the floor.")
            StepItem("Keep your elbows at about a 45° angle.")
            StepItem("Push back up to the starting position.")
            StepItem("Maintain a straight line from head to heels throughout the movement.")

            Spacer(modifier = Modifier.height(40.dp))

        }
    }
}

@Composable
fun WorkoutInfoRow(label: String, value: String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = Brown
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Brown
        )
    }
}

@Composable
fun StepItem(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp) ) {
        Box( modifier = Modifier
            .size(8.dp)
            .background(Brown, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Brown,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PushUpPreview() {
    TrAInAppTheme {
        Push_up()
    }
}
