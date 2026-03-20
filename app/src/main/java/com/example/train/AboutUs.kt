package com.example.train

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutUs() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text(text = "Our Mission", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "At trAIn, our mission is to make fitness accessible, enjoyable, and personalized for everyone.")

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Key Features", style = MaterialTheme.typography.headlineMedium)
        Text(text = "• Interactive workout plans\n• Progress tracking\n• Real-time feedback")

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Disclaimer", style = MaterialTheme.typography.headlineMedium)
        Text(text = "This app is for informational purposes only. Consult a doctor before starting any fitness program.")
    }
}