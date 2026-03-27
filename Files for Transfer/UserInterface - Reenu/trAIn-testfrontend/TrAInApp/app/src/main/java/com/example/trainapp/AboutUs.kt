package com.example.trainapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import com.example.trainapp.ui.theme.Brown
import com.example.trainapp.ui.theme.DBrown
import com.example.trainapp.ui.theme.TrAInAppTheme

@Composable
fun AboutUs() {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(color = Beige)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
        
        Spacer(modifier = Modifier.height(70.dp))
        
        Text(text = "About Us",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            fontSize = 30.sp,
            color = DBrown
        )

        // Details
        AboutContents()
        }
}

@Composable
fun AboutContents() {
    Column(modifier = Modifier
        .fillMaxWidth()
        .background(color = Color.White)
        .padding(16.dp)
    ) {
        // Mission Statement
        Text(text = "Our Mission", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, color = DBrown)
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Brown, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We aim to empower users with engaging fitness tools, personalized guidance, and accessible resources to help them achieve their health goals.",
            color = DBrown, style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        //Major Features
        Text(text = "Major Features", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, color = DBrown)
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Brown, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "• Interactive workout plans\n• Progress tracking dashboard\n• Favorites for quick access\n• Time management tools\n• Helpful guides and tips",
            color = DBrown, style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Our Audience
        Text(text = "Our Audience",fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, color = DBrown)
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Brown, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We serve fitness enthusiasts, beginners starting their journey, and anyone looking for structured, motivating tools to improve their lifestyle.",
            color = DBrown, style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        //Disclaimer
        Text(text = "Disclaimer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall, color = DBrown)
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = Brown, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This app is intended for informational and motivational purposes only. It is not a substitute for professional medical advice. Always consult a healthcare provider before starting any new fitness program.",
            color = DBrown, style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview
@Composable
fun AboutUsPreview() {
    TrAInAppTheme {
        AboutUs()
    }
}
