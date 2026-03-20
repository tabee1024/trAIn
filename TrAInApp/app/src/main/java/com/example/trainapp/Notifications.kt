package com.example.trainapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainapp.ui.theme.DBrown
import com.example.trainapp.ui.theme.TrAInAppTheme

@Composable
fun Notifications() {
    Column(modifier = Modifier.fillMaxSize()
        .background(color = Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Notifications", fontSize = 30.sp, color = DBrown)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Loading...", fontSize = 12.sp, color = DBrown)
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsPreview() {
    TrAInAppTheme {
        Notifications()
    }
}
