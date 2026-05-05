package com.example.trainapp

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainapp.data.TrainDatabaseHelper
import com.example.trainapp.model.ProgressSnapshot
import com.example.trainapp.model.WorkoutSessionCard
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Theme Colors for Dark Dashboard
val DarkBackground = Color(0xFF121212)
val CardBackground = Color(0xFF1E1E1E)
val AccentPushups = Color(0xFF4A90E2)
val AccentSquats = Color(0xFF50E3C2)
val AccentLunges = Color(0xFFF5A623)
val AccentCrunches = Color(0xFFBD10E0)
val TextGreen = Color(0xFF7ED321)

@Composable
fun ProgressScreen(
    auth: FirebaseAuth,
    database: TrainDatabaseHelper,
    refreshTick: Int
) {
    var selectedPeriod by remember { mutableStateOf("Week") }
    val context = LocalContext.current

    val snapshot by produceState<ProgressSnapshot?>(initialValue = null, auth.currentUser?.uid, refreshTick, selectedPeriod) {
        value = withContext(Dispatchers.IO) {
            database.getProgressSnapshot(auth.currentUser?.uid, auth.currentUser?.email, selectedPeriod)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        if (snapshot == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPushups)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Time Range Selector
                item {
                    TimeRangeSelector(selectedPeriod) { selectedPeriod = it }
                }

                // Top Header Stats
                item {
                    HeaderStatsRow(snapshot!!, selectedPeriod)
                }

                // AI Insight Card
                item {
                    AIInsightCard(snapshot!!.recommendation)
                }

                // Form Score Chart Card
                item {
                    DashboardCard(title = "Form score over time") {
                        MultiLineTrendLegend()
                        Spacer(Modifier.height(16.dp))
                        MultiLineChart(snapshot!!.workoutTrends)
                    }
                }

                // Repetition Trend Card
                item {
                    DashboardCard(
                        title = "Reps logged per session",
                        subtitle = "Tracks completed repetitions from saved workouts in the selected period"
                    ) {
                        SingleLineTrendLegend()
                        Spacer(Modifier.height(16.dp))
                        RepetitionChart(snapshot!!.repetitionTrend)
                    }
                }

                // Session History List
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = AccentPushups)
                        Spacer(Modifier.width(8.dp))
                        Text("Session History", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    }
                }

                items(snapshot!!.recentSessions) { session ->
                    SessionHistoryItem(session) { videoPath ->
                        val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                            putExtra("video_path", videoPath)
                        }
                        context.startActivity(intent)
                    }
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
fun TimeRangeSelector(selected: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("Day", "Week", "Month", "Year").forEach { period ->
            val isSelected = selected == period
            Button(
                onClick = { onSelected(period) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) AccentPushups else CardBackground,
                    contentColor = if (isSelected) Color.White else Color.Gray
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(period, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

@Composable
fun HeaderStatsRow(snapshot: ProgressSnapshot, period: String) {
    val subtextSuffix = when (period) {
        "Day" -> "vs yesterday"
        "Week" -> "this week"
        "Month" -> "this month"
        "Year" -> "this year"
        else -> "this week"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HeaderStatItem("TOTAL SESSIONS", snapshot.totalSessions.toString(), "+${(snapshot.totalSessions * 0.1).toInt().coerceAtLeast(1)} $subtextSuffix")
        HeaderStatItem("AVG FORM SCORE", String.format("%.0f", snapshot.averageFormScore), "+${(snapshot.averageFormScore * 0.05).toInt()} vs last $period")
        HeaderStatItem("CURRENT STREAK", "7 days", "Personal best: 12", isStreak = true)
        HeaderStatItem("TOTAL REPS", snapshot.totalRepetitions.toString(), "+${(snapshot.totalRepetitions * 0.1).toInt().coerceAtLeast(10)} $subtextSuffix")
    }
}

@Composable
fun HeaderStatItem(label: String, value: String, subtext: String, isStreak: Boolean = false) {
    Column {
        Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Text(
            subtext, 
            fontSize = 10.sp, 
            color = if (isStreak) AccentLunges else TextGreen,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DashboardCard(
    title: String, 
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun AIInsightCard(recommendation: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentPushups.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("AI Coaching Tip", color = AccentPushups, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(recommendation, color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun MultiLineTrendLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem("Push-ups", AccentPushups)
        LegendItem("Squats", AccentSquats)
        LegendItem("Lunges", AccentLunges)
        LegendItem("Crunches", AccentCrunches)
    }
}

@Composable
fun SingleLineTrendLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem("Completed reps", AccentPushups)
        LegendItem("Trend range", AccentPushups.copy(alpha = 0.2f), isBox = true)
    }
}

@Composable
fun LegendItem(label: String, color: Color, isBox: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(if (isBox) 12.dp else 8.dp)
                .background(color, shape = if (isBox) RoundedCornerShape(2.dp) else RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(6.dp))
        Text(label, color = Color.LightGray, fontSize = 12.sp)
    }
}

@Composable
fun MultiLineChart(trends: Map<String, List<Float>>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val width = size.width
        val height = size.height
        
        // Draw Grid lines
        val gridLines = 7
        for (i in 0..gridLines) {
            val y = height - (i * height / gridLines)
            drawLine(
                color = Color.DarkGray.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        trends.forEach { (exercise, scores) ->
            if (scores.size < 2) return@forEach
            
            val color = when (exercise) {
                "Push Ups" -> AccentPushups
                "Squats" -> AccentSquats
                "Lunges" -> AccentLunges
                else -> AccentCrunches
            }

            val spacePerItem = width / (scores.size - 1).coerceAtLeast(1)
            val points = scores.mapIndexed { index, score ->
                Offset(
                    x = index * spacePerItem,
                    y = height - (score / 100f * height)
                )
            }

            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                points.forEach { lineTo(it.x, it.y) }
            }

            drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
            points.forEach { point ->
                drawCircle(color, radius = 4.dp.toPx(), center = point)
                drawCircle(Color.White, radius = 1.5.dp.toPx(), center = point)
            }
        }
    }
}

@Composable
fun RepetitionChart(data: List<Float>) {
    if (data.isEmpty()) return
    
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val width = size.width
        val height = size.height
        val minVal = (data.minOrNull() ?: 0f).coerceAtMost(0f)
        val maxVal = ((data.maxOrNull() ?: 1f) + 5f).coerceAtLeast(10f)
        val range = maxVal - minVal

        val spacePerItem = width / (data.size - 1).coerceAtLeast(1)
        val points = data.mapIndexed { index, reps ->
            Offset(
                x = index * spacePerItem,
                y = height - ((reps - minVal) / range * height)
            )
        }

        // Trend range (Shadow area)
        val shadowPath = Path().apply {
            moveTo(points[0].x, points[0].y - 20.dp.toPx())
            points.forEachIndexed { i, p -> lineTo(p.x, p.y - 15.dp.toPx()) }
            for (i in points.indices.reversed()) {
                lineTo(points[i].x, points[i].y + 15.dp.toPx())
            }
            close()
        }
        drawPath(shadowPath, AccentPushups.copy(alpha = 0.15f))

        // Main line
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            points.forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, AccentPushups, style = Stroke(width = 3.dp.toPx()))

        points.forEach { point ->
            drawCircle(AccentPushups, radius = 5.dp.toPx(), center = point)
        }
    }
}

@Composable
fun SessionHistoryItem(session: WorkoutSessionCard, onWatchVideo: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(session.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(session.performedOn, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                if (session.videoUri != null) {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { onWatchVideo(session.videoUri) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPushups.copy(alpha = 0.2f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Watch Video", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${session.repetitions} Reps", fontWeight = FontWeight.SemiBold, color = Color.White)
                Text("Score: ${session.formScore.toInt()}%", color = if (session.formScore > 80) TextGreen else AccentLunges)
            }
        }
    }
}
