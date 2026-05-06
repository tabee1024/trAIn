package com.example.train.data

// The profile of the person working out
data class UserProfile(
    val userId: Long,
    val authUid: String?,
    val name: String,
    val email: String,
    val dateOfBirth: String?,
    val gender: String?,
    val fitnessLevel: String?
)

// Summary data passed from the Camera/AI Activity
data class WorkoutSummary(
    val repetitions: Int,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val formScore: Double,
    val postureScore: Double,
    val stabilityScore: Double,
    val rangeOfMotion: Double,
    val correctRepPercentage: Double,
    val mistakesDetected: String,
    val feedback: String
)

// Metadata about the exercise (Push-up, Squat, etc.)
data class WorkoutDefinition(
    val title: String,
    val category: String,
    val difficulty: String,
    val targetMuscles: String
)

// Used for the "History" list UI
data class WorkoutSessionCard(
    val title: String,
    val performedOn: String,
    val repetitions: Int,
    val durationMinutes: Int,
    val formScore: Double,
    val feedback: String,
    val sessionId: Int,
    val videoUri: String?
)

// A high-level view of progress for the Dashboard
data class ProgressSnapshot(
    val averageFormScore: Double,
    val totalSessions: Int,
    val totalRepetitions: Int,
    val improvementPercentage: Double,
    val recommendation: String,
    val recentSessions: List<WorkoutSessionCard>,
    val workoutTrends: Map<String, List<Float>>,
    val bodyweightTrend: List<Float>
)