package com.example.trainapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.trainapp.ui.theme.Beige
import com.example.trainapp.ui.theme.Emperor
import com.example.trainapp.ui.theme.TrAInAppTheme

@Composable
fun Help() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(color = Beige)
            .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Help", fontSize = 30.sp, color = Emperor)
        }
    }
}

@Preview
@Composable
fun HelpPreview() {
    TrAInAppTheme {
        Help()
    }
}