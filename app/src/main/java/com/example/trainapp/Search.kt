package com.example.trainapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainapp.ui.theme.Swirl
import com.example.trainapp.ui.theme.Toast
import com.example.trainapp.ui.theme.TrAInAppTheme

@Composable
fun Search() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(color = Swirl)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .align(Alignment.TopStart),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start) {
            Column(modifier = Modifier
                .fillMaxSize()
                .background(color = Toast)
                .padding(8.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Search", style = MaterialTheme.typography.titleSmall, fontSize = 30.sp, color = Swirl)
            }
            Spacer(modifier = Modifier.height(16.dp))

        }
    }
}


@Preview
@Composable
fun SearchPreview() {
    TrAInAppTheme {
        Search()
    }
}