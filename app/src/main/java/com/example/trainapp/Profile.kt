package com.example.trainapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainapp.ui.theme.Beige
import com.example.trainapp.ui.theme.Brown
import com.example.trainapp.ui.theme.DBrown
import com.example.trainapp.ui.theme.Swirl
//import com.example.trainapp.ui.theme.Toast
import com.example.trainapp.ui.theme.TrAInAppTheme

import androidx.compose.runtime.LaunchedEffect
import com.example.trainapp.model.UserProfile
import com.example.trainapp.ui.theme.TrAInAppTheme

@Composable
fun ProfileScreen(
    profile: UserProfile?,
    onSave: (String, Int?, String?, String?) -> Unit
) {
    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var age by remember(profile) { mutableStateOf(profile?.age?.toString() ?: "") }
    var gender by remember(profile) { mutableStateOf(profile?.gender ?: "") }
    var fitnessLevel by remember(profile) { mutableStateOf(profile?.fitnessLevel ?: "") }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(color = Beige)
        .padding(16.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(color = Color.Transparent)
            .padding(8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Profile", style = MaterialTheme.typography.titleSmall, fontSize = 30.sp, color = DBrown)
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Icon
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

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = gender,
                onValueChange = { gender = it },
                label = { Text("Gender") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = fitnessLevel,
                onValueChange = { fitnessLevel = it },
                label = { Text("Fitness Level") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onSave(name, age.toIntOrNull(), gender, fitnessLevel) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Brown,
                    contentColor = Swirl
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun UserProfilePreview() {
//    TrAInAppTheme {
//        UserProfile()
//    }
//}

@Preview(showBackground = true)
@Composable
fun ProfilePreview() {
    TrAInAppTheme {
        ProfileScreen(profile = null, onSave = { _, _, _, _ -> })
    }
}
