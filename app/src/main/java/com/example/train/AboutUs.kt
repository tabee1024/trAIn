package com.example.train

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutUs() {
    // Universal Background: Beige (#E3E7D3)
    Surface(color = Color(0xFFE3E7D3), modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- HEADER SECTION ---
            Text(
                "CSUN Senior Design — Spring 2026",
                color = Color(0xFF6096BA), // Steel Blue
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "trAIn: AI Fitness Coach",
                color = Color(0xFF2E4057), // Charcoal Blue
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "An AI-powered mobile coach that watches your form, counts your reps, and guides your journey—all via on-device computer vision.",
                color = Color(0xFF2E4057),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- TECH SPECS ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TechBadge("30 FPS", "Real-Time")
                TechBadge("MEDIA PIPE", "Pose Model")
                TechBadge("ON-DEVICE", "AI Inference")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- THE PROBLEM SECTION ---
            AboutSectionHeader("The Mission")
            Text(
                "Beginners often face barriers like high trainer costs and fear of injury due to poor form. trAIn breaks these barriers by providing instant feedback and guided routines at zero cost.",
                color = Color(0xFF2E4057).copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- CORE FEATURES ---
            AboutSectionHeader("Core Features")
            FeatureItem("Smart Onboarding", "Personalized routines based on your goals.")
            FeatureItem("Live Pose Estimation", "33 body landmarks tracked in milliseconds.")
            FeatureItem("Form Correction", "Instant cues for depth, alignment, and posture.")
            FeatureItem("AI Coach", "Motivation and insights from 'Doctor Dopamine'.")

            Spacer(modifier = Modifier.height(32.dp))

            // --- FOOTER / DISCLAIMER ---
            Divider(color = Color(0xFF2E4057).copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "This application is a Senior Design project. Consult a medical professional before beginning any high-intensity physical activity.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AboutSectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFF2E4057),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun FeatureItem(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("• $title", fontWeight = FontWeight.Bold, color = Color(0xFFA57F60)) // Faded Copper
        Text(desc, color = Color(0xFF2E4057), fontSize = 14.sp, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
fun TechBadge(top: String, bottom: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xFF2E4057), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(top, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(bottom, color = Color(0xFF6096BA), fontSize = 10.sp)
    }
}