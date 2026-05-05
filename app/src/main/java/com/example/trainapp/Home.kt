package com.example.trainapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainapp.ui.theme.DBrown
import com.example.trainapp.ui.theme.Swirl
import com.example.trainapp.ui.theme.TrAInAppTheme

@Composable
fun Home(
    onProgressClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onWorkoutsClick: () -> Unit,
    onFABClick: () -> Unit
) {
    val userName = "John"
    var searchQuery by remember { mutableStateOf("") }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        // Greeting
        Text(text = "Hello, $userName",
            fontSize = 20.sp,
            color = DBrown,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.headlineSmall
        )

        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        // Image Carousel
        ImageCarousel(
            images = listOf(
                R.drawable.workout_image1,
                R.drawable.workout_image2,
                R.drawable.workout_image3
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Circular Monitoring Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MonitoringSection("Progress", R.drawable.progress, onProgressClick)
            MonitoringSection("Goals", R.drawable.time_spend, onGoalsClick)
            MonitoringSection("Favorites", R.drawable.favorites, onFavoritesClick)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Workout Card
        WorkoutCard(
            imageRes = R.drawable.workout_image,
            onClick = onWorkoutsClick
        )

        //Spacer(modifier = Modifier.height(18.dp))

        // FAB
        HelpFab(onClick = onFABClick)
    }
}

@Composable
fun ImageCarousel(images: List<Int>) {
    LazyRow(horizontalArrangement = Arrangement.SpaceEvenly) {
        items(images) { img ->
            Image(
                painter = painterResource(id = img),
                contentDescription = null,
                modifier = Modifier
                    .height(150.dp)
                    .width(280.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun MonitoringSection(label: String, iconRes: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painterResource(id = iconRes),
                contentDescription = label
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = DBrown,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
fun WorkoutCard(imageRes: Int, onClick: () -> Unit) {
    Card(modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Workout image
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Workouts",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark overlay
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.70f)))

            // Workout card title
            Text(
                text = "Click here to start your next challenge",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HelpFab(onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            containerColor = DBrown
        ) {
            Text(
                text = "?",
                modifier = Modifier
                    .padding(4.dp),
                fontSize = 40.sp,
                color = Swirl
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search...") },
        singleLine = true,
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null)
        }
    )
}


@Preview(showBackground = true)
@Composable
fun HomePreview() {
    TrAInAppTheme {
        Home(
            onProgressClick = {},
            onFavoritesClick = {},
            onGoalsClick = {},
            onWorkoutsClick = {},
            onFABClick = {}
        )
    }
}
