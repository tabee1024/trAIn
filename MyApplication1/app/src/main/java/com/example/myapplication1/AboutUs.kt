package com.example.myapplication1

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AboutUs() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Mission
        Text(
            text = "Our Mission",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "At trAIn, our mission is to make fitness accessible, enjoyable, personalized for everyone from your comfort. We aim to empower our users with engaging fitness tools, personalized guidance, and accessible resources to help you achieve your health goals."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Features
        Text(
            text = "Key Features",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "• Interactive workout plans\n• Progress tracking & Analytics\n• Favorites for quick access\n• Real-time feedbacks\n• Helpful guides and tips\n• Proper training"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Our Values
        Text(
            text = "Our Values",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We value user and data privacy."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Audience
        Text(
            text = "Our Audience",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We serve fitness enthusiasts, beginners starting their journey, and anyone looking for structured, motivating tools to improve their lifestyle."
        )

        Spacer(modifier = Modifier.height(16.dp))


        // Disclaimer
        Text(
            text = "Disclaimer",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This app is intended for informational and motivational purposes only. It is not a substitute for professional medical advice. Always consult a healthcare provider before starting any new fitness program."
        )
    }
}

