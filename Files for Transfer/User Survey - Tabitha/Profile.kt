package com.example.train

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainapp.ui.theme.Beige
import com.example.trainapp.ui.theme.Brown
import com.example.trainapp.ui.theme.DBrown
import com.example.trainapp.ui.theme.Swirl
import com.example.trainapp.ui.theme.TrAInAppTheme

@Composable
fun Profile() {

    val survey = UserProfileStore.latestSurvey

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Beige)
            .padding(16.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Transparent)
                .padding(8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleSmall,
                fontSize = 30.sp,
                color = DBrown
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                if (survey == null) {
                    Text("No survey saved yet.")
                } else {
                    Text("Survey Saved", color = DBrown)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Basic Info
                    Text("Age: ${survey.ageRange?.label ?: "-"}")

                    val genderText = when (survey.gender) {
                        GenderOption.OTHER -> survey.genderOtherText?.takeIf { it.isNotBlank() } ?: "Other"
                        null -> "-"
                        else -> survey.gender.label
                    }
                    Text("Gender: $genderText")
                    Text("Health level: ${survey.healthLevel?.label ?: "-"}")

                    Spacer(modifier = Modifier.height(10.dp))

                    // Training Profile
                    Text("Fitness level: ${survey.fitnessLevel?.label ?: "-"}")
                    Text("Primary goal: ${survey.primaryGoal?.label ?: "-"}")
                    Text("Workout frequency: ${survey.workoutFrequency?.display() ?: "-"}")
                    Text("Preferred duration: ${survey.preferredDuration ?: "-"}")
                    Text("Enjoyed workouts: ${formatLabels(survey.enjoyedWorkouts) { it.label }}")

                    Spacer(modifier = Modifier.height(10.dp))

                    // Safety & Limitations
                    Text("Injuries/limitations: ${formatLabels(survey.injuries) { it.label }}")
                    if (!survey.injuryOtherText.isNullOrBlank()) {
                        Text("Other details: ${survey.injuryOtherText}")
                    }
                    Text("Medications affecting exercise: ${boolToYesNo(survey.takesMedicationsAffectingExercise)}")
                    if (!survey.medicationsText.isNullOrBlank()) {
                        Text("Medications: ${survey.medicationsText}")
                    }
                    Text("Experience: ${survey.previousExperience?.label ?: "-"}")

                    Spacer(modifier = Modifier.height(10.dp))

                    // Coaching & Motivation
                    Text("Coaching pace: ${survey.coachingPace?.label ?: "-"}")
                    Text("Feedback style: ${survey.feedbackStyle?.label ?: "-"}")
                    Text("Motivational reminders: ${boolToYesNo(survey.wantsMotivationalReminders)}")
                    Text("Motivations: ${formatLabels(survey.motivations) { it.label }}")

                    Spacer(modifier = Modifier.height(10.dp))

                    // Environment & Tracking
                    Text("Equipment: ${formatLabels(survey.equipmentAccess) { it.label }}")
                    Text("Workout space: ${survey.workoutSpace?.label ?: "-"}")
                    Text("Tracking: ${formatLabels(survey.progressTracking) { it.label }}")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        UserProfile()
    }
}

private fun boolToYesNo(value: Boolean?): String {
    return when (value) {
        true -> "Yes"
        false -> "No"
        null -> "-"
    }
}

private fun <T> formatLabels(items: Set<T>, label: (T) -> String): String {
    if (items.isEmpty()) return "-"
    return items.joinToString(", ") { label(it) }
}

@Composable
fun UserProfile() {
    var name by remember { mutableStateOf("John Smith") }
    var email by remember { mutableStateOf("johnsmith@example.com") }
    var age by remember { mutableStateOf("25") }
    var weight by remember { mutableStateOf("110 lb") }
    var gender by remember { mutableStateOf("Male") }
    var goals by remember { mutableStateOf("Become physically fit and healthy") }
    var bio by remember { mutableStateOf("Fitness enthusiast.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(width = 4.dp, color = Brown, shape = CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Icon",
                tint = Color.DarkGray,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Gender") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = goals,
            onValueChange = { goals = it },
            label = { Text("Goals") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(
                containerColor = Brown,
                contentColor = Swirl
            )
        ) {
            Text("Save")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    TrAInAppTheme {
        Profile()
    }
}