package com.example.trainapp.model

import com.example.trainapp.R

data class WorkoutDefinition(
    val id: Int,
    val title: String,
    val category: String,
    val difficulty: String,
    val targetMuscles: String,
    val durationMinutes: Int,
    val imageRes: Int,
    val videoUrl: String,
    val description: String,
    val tips: List<String>,
)

enum class WorkoutType {
    PUSH_UP,
    SQUAT,
    LUNGE;

    companion object {
        fun fromWorkoutId(id: Int): WorkoutType = when (id) {
            1 -> PUSH_UP
            2 -> SQUAT
            else -> LUNGE
        }
    }
}

object WorkoutCatalog {
    val workouts = listOf(
        WorkoutDefinition(
            id = 1,
            title = "Push Ups",
            category = "Strength",
            difficulty = "Beginner",
            targetMuscles = "Chest, shoulders, triceps, core",
            durationMinutes = 15,
            imageRes = R.drawable.push_up,
            videoUrl = "https://www.youtube.com/embed/IODxDxX7oi4",
            description = "Build upper-body strength while keeping your body aligned from head to heels.",
            tips = listOf(
                "Start in a high plank with hands just wider than shoulders.",
                "Lower until your elbows reach roughly 90 degrees.",
                "Keep your core tight and hips level.",
                "Press back up without flaring the elbows too wide.",
            ),
        ),
        WorkoutDefinition(
            id = 2,
            title = "Squats",
            category = "Strength",
            difficulty = "Beginner",
            targetMuscles = "Glutes, quads, hamstrings",
            durationMinutes = 20,
            imageRes = R.drawable.squats,
            videoUrl = "https://www.youtube.com/embed/aclHkVaku9U",
            description = "A lower-body staple focused on depth, balance, and strong knee tracking.",
            tips = listOf(
                "Stand with feet about shoulder width apart.",
                "Sit your hips back and down while keeping your chest tall.",
                "Track knees in line with your toes.",
                "Drive through your heels to stand tall again.",
            ),
        ),
        WorkoutDefinition(
            id = 3,
            title = "Lunges",
            category = "Strength",
            difficulty = "Intermediate",
            targetMuscles = "Quads, glutes, hamstrings, balance",
            durationMinutes = 15,
            imageRes = R.drawable.lunges,
            videoUrl = "https://www.youtube.com/embed/QOVaHwm-Q6U",
            description = "Train leg strength and balance with controlled lowering and upright posture.",
            tips = listOf(
                "Step one leg forward into a split stance.",
                "Lower until both knees approach 90 degrees.",
                "Keep the front knee stacked over the ankle.",
                "Push back up with control and keep your torso upright.",
            ),
        ),
    )

    fun byId(id: Int): WorkoutDefinition = workouts.first { it.id == id }
}

data class UserProfile(
    val userId: Long,
    val authUid: String?,
    val name: String,
    val email: String,
    val dateOfBirth: String?, // Format: YYYY-MM-DD
    val gender: String?,
    val fitnessLevel: String?,
) {
    val age: Int?
        get() {
            val dob = dateOfBirth ?: return null
            return try {
                val birthDate = java.time.LocalDate.parse(dob)
                java.time.Period.between(birthDate, java.time.LocalDate.now()).years
            } catch (e: Exception) {
                null
            }
        }
}

data class ProgressSnapshot(
    val averageFormScore: Double,
    val totalSessions: Int,
    val totalRepetitions: Int,
    val improvementPercentage: Double,
    val recommendation: String,
    val recentSessions: List<WorkoutSessionCard>,
    val workoutTrends: Map<String, List<Float>> = emptyMap(),
    val repetitionTrend: List<Float> = emptyList(),
)

data class WorkoutSessionCard(
    val title: String,
    val performedOn: String,
    val repetitions: Int,
    val durationMinutes: Int,
    val formScore: Double,
    val feedback: String,
    val sessionId: Int = 0,
    val videoUri: String? = null,
)

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
    val feedback: String,
)
