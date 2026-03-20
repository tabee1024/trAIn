package com.example.trainapp

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainapp.ui.theme.Brown
import com.example.trainapp.ui.theme.DBrown
import com.example.trainapp.ui.theme.Swirl
import com.example.trainapp.ui.theme.TrAInAppTheme

enum class ProfileMode { READ, EDIT }

@SuppressLint("UnrememberedMutableState")
@Composable
fun Profile() {
var mode by remember { mutableStateOf(ProfileMode.READ) }
    var name by remember { mutableStateOf("John Smith") }
    var email by remember { mutableStateOf("johnsmith@example.com") }
    var age by remember { mutableStateOf("25") }
    var weight by remember { mutableStateOf("110 lb") }
    var gender by remember { mutableStateOf("Male") }
    var pronouns by remember { mutableStateOf("He/Him/His") }
    var goals by remember { mutableStateOf("Become physically fit and healthy") }
    var bio by remember { mutableStateOf("Fitness enthusiast.") }

    when (mode) {
        ProfileMode.READ -> ProfileReadOnly(
            name = name,
            email = email,
            age = age,
            weight = weight,
            gender = gender,
            pronouns = pronouns,
            goals = goals,
            bio = bio,
            onEdit = { mode = ProfileMode.EDIT }
        )

        ProfileMode.EDIT -> ProfileEdit(
            name = name,
            email = email,
            age = age,
            weight = weight,
            gender = gender,
            pronouns = pronouns,
            goals = goals,
            bio = bio,
            onNameChange = { name = it },
            onEmailChange = { email = it },
            onAgeChange = { age = it},
            onWeightChange = { weight = it },
            onGenderChange = { gender = it },
            onPronounsChange = { pronouns = it },
            onGoalsChange = { goals = it },
            onBioChange = { bio = it },
            onSave = { mode = ProfileMode.READ },
            onCancel = { mode = ProfileMode.READ }
        )
    }
}

@Composable
fun ProfileEdit(
    name: String,
    email: String,
    age: String,
    weight: String,
    gender: String,
    pronouns: String,
    goals: String,
    bio: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onPronounsChange: (String) -> Unit,
    onGoalsChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
){
    val context = LocalContext.current.applicationContext

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(70.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween)
        {
            Text(text = "Cancel",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.clickable { onCancel() }
            )
            Text(text = "Save",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onSave()
                }
            )
        }

        Text(text = "Profile", style = MaterialTheme.typography.titleSmall, fontSize = 30.sp, color = DBrown)
        Spacer(modifier = Modifier.height(8.dp))
        // Profile Icon/Image placeholder
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

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Age
        OutlinedTextField(
            value = age,
            onValueChange = onAgeChange,
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Weight
        OutlinedTextField(
            value = weight,
            onValueChange = onWeightChange,
            label = { Text("Weight") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Gender
        OutlinedTextField(
            value = gender,
            onValueChange = onGenderChange,
            label = { Text("Gender") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pronouns
        OutlinedTextField(
            value = pronouns,
            onValueChange = onPronounsChange,
            label = { Text("Pronouns") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Goals
        OutlinedTextField(
            value = goals,
            onValueChange = onGoalsChange,
            label = { Text("Goals") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Bio
        OutlinedTextField(
            value = bio,
            onValueChange = onBioChange,
            label = { Text("Bio") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Save Button
        Button(onClick = {Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()},
            colors = ButtonDefaults.buttonColors(
                containerColor = DBrown, // background color
                contentColor = Color.White // icon text color
            )
        ) {
            Text("Save")
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun ProfileReadOnly(
    name: String,
    email: String,
    age: String,
    weight: String,
    gender: String,
    pronouns: String,
    goals: String,
    bio: String,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(70.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Edit",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onEdit() }
            )
        }

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

        Spacer(Modifier.height(16.dp))

        Text( text = name, style = MaterialTheme.typography.headlineSmall )

        Spacer(Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Swirl),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow("Name", name)
                HorizontalDivider()
                ProfileInfoRow("Email", email)
                HorizontalDivider()
                ProfileInfoRow("Age", age)
                HorizontalDivider()
                ProfileInfoRow("Weight", weight)
                HorizontalDivider()
                ProfileInfoRow("Gender", gender)
                HorizontalDivider()
                ProfileInfoRow("Pronouns", pronouns)
                HorizontalDivider()
                ProfileInfoRow("Goals", goals)
                HorizontalDivider()
                ProfileInfoRow("Bio", bio)
            }
        }
        Spacer(modifier = Modifier.height(80.dp))

    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column( modifier = Modifier.fillMaxWidth()
        .padding(vertical = 8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}


@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    TrAInAppTheme {
        Profile()
    }
}
