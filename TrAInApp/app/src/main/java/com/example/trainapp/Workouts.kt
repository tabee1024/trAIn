package com.example.trainapp

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.trainapp.ui.theme.Beige
import com.example.trainapp.ui.theme.DBrown
import com.example.trainapp.ui.theme.TrAInAppTheme

@Composable
fun Workouts(navController: NavController) {
    Column(modifier = Modifier
        .fillMaxSize()
        .background(color = Beige)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Spacer(modifier = Modifier.height(70.dp))

        Text(text = "Workouts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            fontSize = 30.sp,
            color = DBrown
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scrollable workout list
        WorkoutList(navController = navController, modifier = Modifier.weight(1f))
    }
}

@Composable
fun WorkoutList(navController: NavController, modifier: Modifier = Modifier) {
    val workouts = listOf(
        Workout(1,"Push up", "Duration: 20 min", "Intermediate","push_up", "push_up"),
        Workout(2, "Squats", "Duration: 35 min", "Beginner", "squats", "squats"),
        Workout(3, "Lunges", "Duration: 15 min", "Beginner", "lunges", "lunges")
    )

    LazyColumn(modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(workouts) { workout ->
            WorkoutItem(workout, navController)

        }
    }
}
data class Workout(
    val id: Int,
    val title: String,
    val duration: String,
    val difficulty: String,
    //val imageResId: Int,
    val imageName: String,
    val screen: String
)
@SuppressLint("LocalContextResourcesRead", "DiscouragedApi")
@Composable
fun WorkoutItem(workout: Workout, navController: NavController) {
    val context = LocalContext.current
    val imageResId = remember(workout.imageName) {
        context.resources.getIdentifier(
            workout.imageName, "drawable", context.packageName
        ) }
    Card(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth()
            .clickable {
                //navController.navigate(workout.id.toString())
                navController.navigate(workout.screen)
            },
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(modifier = Modifier.height(200.dp)) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.70f))
            )
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier) {
                    Text(
                        workout.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(60.dp))
                    Text(workout.duration,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        workout.difficulty,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@SuppressLint("SuspiciousIndentation")
@Preview(showBackground = true)
@Composable
fun WorkoutsPreview() {
    val navController = rememberNavController()
        TrAInAppTheme {
            Workouts(navController)
        }
}
