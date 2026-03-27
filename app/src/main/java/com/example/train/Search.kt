package com.example.train

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.train.ui.theme.DeepNavy
import com.example.train.ui.theme.OffWhite
import com.example.train.ui.theme.StoneGrey
import com.example.train.ui.theme.TrAInAppTheme

@Composable
fun Search() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier
            .fillMaxSize()
            .background(color = OffWhite)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Loading ...", style = MaterialTheme.typography.titleSmall, fontSize = 30.sp, color = DeepNavy)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    TrAInAppTheme {
        Search()
    }
}
