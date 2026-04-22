package com.example.train

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.train.ui.theme.StoneGrey
import com.example.train.ui.theme.*

@Composable
fun Help() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White)
            .padding(16.dp)
    ) {
        item { FAQSection() }
        item { Spacer(Modifier.height(4.dp)) }

        item { ContactSection() }
        item { Spacer(Modifier.height(4.dp)) }

        item { ResourcesSection() }
        item { Spacer(Modifier.height(4.dp)) }

        item { AppInfoSection() }
    }
}

@Composable
private fun FAQSection() {
    SectionHeader("FAQs")

    val faqs = listOf(
        "How do I start a workout?" to
                "Go to the Workouts screen and choose a workout that fits your goals.",
        "Why isn’t my app slow or frozen?" to
                "Restart the app for better performance.",
        "How do I review my workout progress?" to
                "Navigate to Progress screen to view your detailed workout summary.",
        "How to change my password?" to
                "Sign out from your account, select 'Forgot password?', and follow instructions.",
        "How to update profile details?" to
                "Click on the Profile icon on the top right corner of the app, and select edit option." +
                "Don't forget to SAVE your updates after making changes!"
    )

    faqs.forEach { (q, a) ->
        FAQItem(question = q, answer = a)
    }
}

@Composable
private fun FAQItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 8.dp)
            .animateContentSize()
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.titleMedium
        )
        if (expanded) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContactSection() {
    SectionHeader("Contact Us")

    val items = listOf(
        "Email Support" to "traAInsupport@example.com",
    )

    items.forEach { (title, subtitle) ->
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(subtitle) }

        )
    }
}

@Composable
private fun ResourcesSection() {
    SectionHeader("Resources")

    val items = listOf(
        "Video Tutorials" to "Learn how to perform workouts by clicking 'Watch Video Tutorial' option for each workout."
    )

    items.forEach { (title, subtitle) ->
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(subtitle) }

        )
    }
}

@Composable
private fun AppInfoSection() {
    SectionHeader("App Info")

    Column {
        Text("Version: 1.0.0", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}
