package com.example.train

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.train.ui.theme.*

@Composable
fun MotivationPopup(
    reps: Int,
    accuracy: Int,
    timeSpent: String,
    onDismiss: () -> Unit
) {
    val survey = UserProfileStore.latestSurvey
    val goal = survey?.primaryGoal ?: PrimaryGoal.GENERAL_FITNESS

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AccentGold, // Using your #B48D55
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = "Workout Insights",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DeepNavy, // Using your #101723
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MiniStat("Accuracy", "$accuracy%")
                    MiniStat("Time", timeSpent)
                    MiniStat("Reps", reps.toString())
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = SoftBlueTint)

                // Motivational Message based on Goal
                Text(
                    text = getMotivationalPhrase(goal, accuracy, reps),
                    style = MaterialTheme.typography.bodyLarge,
                    color = DeepNavy,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Coaching Tip Box
                Surface(
                    color = SoftBlueTint, // Using your #D9E0ED
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = DeepCoffee)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = getCoachingTip(goal, accuracy),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DeepCoffee // Using your #704E43
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Keep Grinding", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = SteelBlue, fontSize = 18.sp)
        Text(label, style = MaterialTheme.typography.labelSmall, color = StoneGrey)
    }
}

private fun getMotivationalPhrase(goal: PrimaryGoal, accuracy: Int, reps: Int): String {
    return when {
        accuracy > 90 -> "Incredible precision! Your form is perfect for ${goal.label.lowercase()}."
        reps > 20 -> "High volume today! You're building serious consistency."
        else -> when (goal) {
            PrimaryGoal.WEIGHT_LOSS -> "Every rep is a step toward a leaner you. Keep moving!"
            PrimaryGoal.MUSCLE_GAIN -> "Focus on the tension. That's where the growth happens."
            PrimaryGoal.IMPROVE_ENDURANCE -> "Consistency over speed. You're outlasting yesterday's self."
            else -> "Great session! Your future self is thanking you for this effort."
        }
    }
}

private fun getCoachingTip(goal: PrimaryGoal, accuracy: Int): String {
    if (accuracy < 70) {
        return "Form Check: Try slowing down your movements. Better accuracy leads to faster ${goal.label.lowercase()} results."
    }
    return when (goal) {
        PrimaryGoal.MUSCLE_GAIN -> "Tip: Focus on the 'negative' part of the movement to maximize muscle fiber engagement."
        PrimaryGoal.WEIGHT_LOSS -> "Tip: Try to keep your rest periods under 45 seconds to keep your heart rate in the burn zone."
        PrimaryGoal.IMPROVE_ENDURANCE -> "Tip: Take deep, rhythmic breaths through your nose to maintain oxygen flow."
        else -> "Tip: Hydration is key! Drink 8oz of water within the next 20 minutes to aid recovery."
    }
}