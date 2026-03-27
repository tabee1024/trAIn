package com.example.train

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.train.ui.theme.*

@Composable
fun Home(navController: NavHostController) {
    // Dynamic greeting based on saved survey data
    val survey = UserProfileStore.latestSurvey

    // Logic: If survey exists, use nickname. If no nickname, use "User".
    val userName = survey?.nickname ?: "User"
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Hello, $userName", // This will now show the nickname
                fontSize = 28.sp,
                color = CharcoalBlue,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )

            // Reusing your SearchBar component
            SearchBar(query = searchQuery, onQueryChange = { searchQuery = it })

            ImageCarousel(
                images = listOf(
                    R.drawable.workout_image1,
                    R.drawable.workout_image2,
                    R.drawable.workout_image3
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Monitoring Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MonitoringSection("Progress", R.drawable.progress) { navController.navigate(Screens.Progress.screen) }
                MonitoringSection("Time Spent", R.drawable.time_spend) { navController.navigate(Screens.Progress.screen) }
                MonitoringSection("Favorites", R.drawable.favorites) { navController.navigate(Screens.Favorites.screen) }
            }

            Spacer(modifier = Modifier.height(32.dp))

            WorkoutCard(R.drawable.workout_image) { navController.navigate(Screens.Workouts.screen) }

            Spacer(modifier = Modifier.height(120.dp))
        }

        HelpFab(
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            onClick = { navController.navigate(Screens.Help.screen) }
        )
    }
}

@Composable
fun MonitoringSection(label: String, @DrawableRes drawableResId: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(SteelBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = label,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = CharcoalBlue, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun HelpFab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(70.dp),
        containerColor = AccentGold,
        contentColor = Color.White,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(8.dp)
    ) {
        Text("?", fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ImageCarousel(images: List<Int>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
        items(images) { img ->
            Image(
                painter = painterResource(id = img),
                contentDescription = null,
                modifier = Modifier.height(180.dp).width(300.dp).clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun WorkoutCard(imageRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp).clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(painter = painterResource(id = imageRes), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))
            Text(
                text = "Explore Workouts",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search for exercises...", color = CharcoalBlue.copy(alpha = 0.6f)) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CharcoalBlue) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = CharcoalBlue,
            unfocusedBorderColor = CharcoalBlue.copy(alpha = 0.3f),
            containerColor = Color.Transparent
        )
    )
}