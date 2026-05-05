package com.example.trainapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainapp.ui.theme.Beige
import com.example.trainapp.ui.theme.DBrown
import com.example.trainapp.ui.theme.Emperor
import com.example.trainapp.ui.theme.Swirl
import com.example.trainapp.ui.theme.TrAInAppTheme

@Composable
fun Lunges( ){
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .background(color = Beige)
    ) {
        // Top Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        ) {
            Image(painter = painterResource(id = R.drawable.lunges),
                contentDescription = "Lunges",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            //Black shade overlay
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.10f))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Workout details
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Lunges",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = DBrown
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "A classic upper‑body exercise that strengthens your chest, shoulders, triceps, and core.",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontStyle = Italic),
                fontSize = 14.sp,
                color = Emperor
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = DBrown)
            ) {
                Text(text = "Start Workout", color = Swirl, style = MaterialTheme.typography.bodyLarge)
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Arrow"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = DBrown
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "How to Perform",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = DBrown )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LungesPreview() {
    TrAInAppTheme {
        Lunges()
    }
}
