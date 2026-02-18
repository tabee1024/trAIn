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

@Composable
fun Profile() {
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
        //Spacer(modifier = Modifier.height(8.dp))
        UserProfile()
    }
}

@Composable
fun UserProfile() {
    var name by remember { mutableStateOf("John Smith") }
    var email by remember { mutableStateOf("johnsmith@example.com") }
    var age by remember { mutableStateOf("25") }
    var weight by remember { mutableStateOf("110 lb")}
    var gender by remember { mutableStateOf("Male") }
    //var pronouns by remember { mutableStateOf("He/Him/His") }
    var goals by remember { mutableStateOf("Become physically fit and healthy") }
    var bio by remember { mutableStateOf("Fitness enthusiast.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Age
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Weight
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Gender
        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Gender") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(6.dp))

//        // Pronouns
//        OutlinedTextField(
//            value = pronouns,
//            onValueChange = { pronouns = it },
//            label = { Text("Pronouns") },
//            modifier = Modifier.fillMaxWidth(),
//            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//        )

        Spacer(modifier = Modifier.height(6.dp))

        // Goals
        OutlinedTextField(
            value = goals,
            onValueChange = { goals = it },
            label = { Text("Goals") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Bio
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

        // Save Button
        Button(
            onClick = { },
            colors = ButtonDefaults.buttonColors(
                containerColor = Brown, // background color
                contentColor = Swirl // text/icon color
            )
            //modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
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
        Profile()
    }
}
