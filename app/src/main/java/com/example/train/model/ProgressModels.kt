package com.example.train.model

data class ProgressSnapshot(
    val repsCompleted: Int,
    val accuracy: Int,
    val timeSpent: String,
    val averageFormScore: Double,
    val totalSessions: Int,
    val totalRepetitions: Int,
    val improvementPercentage: Double,
    val currentStreakDays: Int,
    val recommendation: String,
    val recentSessions: List<WorkoutSessionCard>,
    val repetitionTrend: List<Float>,
)

data class WorkoutSessionCard(
    val title: String,
    val performedOn: String,
    val repetitions: Int,
    val durationMinutes: Int,
    val formScore: Double,
    val feedback: String,
)
