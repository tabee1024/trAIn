package com.example.train

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.train.ui.theme.StoneGrey
import com.example.train.ui.theme.*

@Composable
fun Notifications() {
    val notifications = listOf(
        WorkoutNotification.Welcome(
            id = "1",
            title = "Welcome Back!",
            message = "Ready to crush your workout today?",
            timestamp = "Just now"
        ),
        WorkoutNotification.Alert(
            id = "2",
            title = "Hydration Reminder",
            message = "Drink a glass of water before starting.",
            timestamp = "Just now"
        ),
        WorkoutNotification.Achievement(
            id = "3",
            title = "Streak Achieved!",
            message = "Start small. Stay consistent. Watch yourself become unstoppable",
            timestamp = "Just now"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Notifications",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(notifications, key = { it.id }) { notification ->
                NotificationCard(notification)
            }
        }
    }
}

sealed class WorkoutNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String
) {
    class Welcome(
        id: String,
        title: String,
        message: String,
        timestamp: String
    ) : WorkoutNotification(id, title, message, timestamp)

    class Alert(
        id: String,
        title: String,
        message: String,
        timestamp: String
    ) : WorkoutNotification(id, title, message, timestamp)

    class Achievement(
        id: String,
        title: String,
        message: String,
        timestamp: String
    ) : WorkoutNotification(id, title, message, timestamp)
}

@Composable
fun NotificationCard(notification: WorkoutNotification) {
    val backgroundColor = when (notification) {
        is WorkoutNotification.Welcome -> Color(0xFFDFF6FF)
        is WorkoutNotification.Alert -> Color(0xFFFFE5E5)
        is WorkoutNotification.Achievement -> Color(0xFFE8FFE5)
    }

    val icon = when (notification) {
        is WorkoutNotification.Welcome -> Icons.Default.Person
        is WorkoutNotification.Alert -> Icons.Default.Warning
        is WorkoutNotification.Achievement -> Icons.Default.Star
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(notification.title, style = MaterialTheme.typography.titleMedium)
                Text(notification.message, style = MaterialTheme.typography.bodyMedium)
                Text(
                    notification.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
