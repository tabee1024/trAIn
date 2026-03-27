package com.example.train

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
import com.example.train.ui.theme.StoneGrey
import com.example.train.ui.theme.TrAInAppTheme

@Composable
fun Notifications() {
    Column(modifier = Modifier.fillMaxSize()
        .background(color = Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Notifications", fontSize = 30.sp, color = StoneGrey)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "None for now, great work!", fontSize = 12.sp, color = StoneGrey)
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsPreview() {
    TrAInAppTheme {
        Notifications()
    }
}
