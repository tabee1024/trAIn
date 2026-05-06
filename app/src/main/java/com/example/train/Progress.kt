package com.example.train

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.train.data.TrainProgressDatabaseHelper
import com.example.train.model.ProgressSnapshot
import com.example.train.model.WorkoutSessionCard
import com.example.train.ui.theme.Brown
import com.example.train.ui.theme.SoftBlueTint
import com.example.train.ui.theme.StoneGrey
import com.example.train.ui.theme.TrAInAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun Progress(
    reps: Int = 30,
    accuracy: Int = 92,
    timeSpent: String = "08:14",
    onExit: () -> Unit,
    onRestart: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { TrainProgressDatabaseHelper(context) }
    var selectedPeriod by remember { mutableStateOf("Week") }

    LaunchedEffect(database) {
        withContext(Dispatchers.IO) {
            database.seedDemoProgressIfEmpty()
        }
    }

    val snapshot by produceState<ProgressSnapshot?>(
        initialValue = null,
        selectedPeriod,
    ) {
        value = withContext(Dispatchers.IO) {
            database.getProgressSnapshot(selectedPeriod)
        }
    }

    val displayReps = snapshot?.repsCompleted ?: reps
    val displayAccuracy = snapshot?.accuracy ?: accuracy
    val displayTimeSpent = snapshot?.timeSpent ?: timeSpent

    Column(
        modifier = Modifier
            .background(color = Color.White)
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        Text(
            text = "Workout Summary",
            color = StoneGrey,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        WorkoutStats(
            reps = displayReps,
            accuracy = displayAccuracy,
            timeSpent = displayTimeSpent
        )

        AccuracyIndicator(accuracy = displayAccuracy)

        ProgressPeriodSelector(selectedPeriod) { selectedPeriod = it }

        snapshot?.let { progress ->
            ProgressDatabaseStats(progress)
            CoachingInsight(progress.recommendation)
            RecentWorkoutHistory(progress.recentSessions)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onExit,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StoneGrey,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Go to Home")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = onRestart,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StoneGrey,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Restart Workout")
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun WorkoutStats(
    reps: Int,
    accuracy: Int,
    timeSpent: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatCard("Reps Completed", reps.toString())
        Spacer(modifier = Modifier.height(16.dp))

        StatCard("Accuracy", "$accuracy%")
        Spacer(modifier = Modifier.height(16.dp))

        StatCard("Time Spent", timeSpent)
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(0.5.dp, color = StoneGrey),
        colors = CardDefaults.cardColors(containerColor = SoftBlueTint)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = StoneGrey
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = StoneGrey
            )
        }
    }
}

@Composable
fun AccuracyIndicator(accuracy: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(120.dp),
                color = Color.LightGray.copy(alpha = 0.3f),
                strokeWidth = 8.dp,
            )
            CircularProgressIndicator(
                progress = { accuracy / 100f },
                modifier = Modifier.size(120.dp),
                color = StoneGrey,
                strokeWidth = 8.dp,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$accuracy%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = StoneGrey
                )
                Text(
                    text = "Accuracy",
                    style = MaterialTheme.typography.labelSmall,
                    color = StoneGrey.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun ProgressPeriodSelector(selected: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("Day", "Week", "Month", "Year").forEach { period ->
            val isSelected = selected == period
            Button(
                onClick = { onSelected(period) },
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) StoneGrey else SoftBlueTint,
                    contentColor = if (isSelected) Color.White else StoneGrey,
                ),
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(period, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ProgressDatabaseStats(snapshot: ProgressSnapshot) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniProgressStat("Sessions", snapshot.totalSessions.toString(), Modifier.weight(1f))
            MiniProgressStat("Total Reps", snapshot.totalRepetitions.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniProgressStat("Avg Form", "${snapshot.averageFormScore.toInt()}%", Modifier.weight(1f))
            MiniProgressStat("Streak", "${snapshot.currentStreakDays} days", Modifier.weight(1f))
        }
    }
}

@Composable
fun MiniProgressStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(0.5.dp, StoneGrey.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = SoftBlueTint),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, color = StoneGrey.copy(alpha = 0.75f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = StoneGrey, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CoachingInsight(recommendation: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(0.5.dp, StoneGrey.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("AI Coaching Tip", color = StoneGrey, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text(recommendation, color = Brown, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun RecentWorkoutHistory(sessions: List<WorkoutSessionCard>) {
    Column(Modifier.fillMaxWidth()) {
        Text("Recent Sessions", color = StoneGrey, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(10.dp))
        sessions.take(4).forEach { session ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = SoftBlueTint),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(session.title, color = StoneGrey, fontWeight = FontWeight.Bold)
                        Text("${session.performedOn} - ${session.durationMinutes} min", color = Brown, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${session.repetitions} reps", color = StoneGrey, fontWeight = FontWeight.SemiBold)
                        Text("${session.formScore.toInt()}%", color = Brown, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressPreview() {
    TrAInAppTheme {
        Progress(
            reps = 30,
            accuracy = 55,
            timeSpent = "08:14",
            onExit = {},
            onRestart = {}
        )
    }
}
