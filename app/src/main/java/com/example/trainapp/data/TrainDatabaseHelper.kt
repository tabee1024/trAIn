package com.example.trainapp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.trainapp.model.ProgressSnapshot
import com.example.trainapp.model.UserProfile
import com.example.trainapp.model.WorkoutDefinition
import com.example.trainapp.model.WorkoutSessionCard
import com.example.trainapp.model.WorkoutSummary
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

class TrainDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE Users (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                auth_uid TEXT UNIQUE,
                name TEXT,
                email TEXT UNIQUE,
                dob TEXT,
                gender TEXT,
                fitness_level TEXT
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE Exercises (
                exercise_id INTEGER PRIMARY KEY AUTOINCREMENT,
                exercise_name TEXT UNIQUE,
                category TEXT,
                difficulty_level TEXT,
                target_muscle_group TEXT
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE Workout_Sessions (
                session_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                session_date TEXT,
                start_time TEXT,
                end_time TEXT,
                duration INTEGER,
                calories_burned INTEGER,
                video_uri TEXT,
                FOREIGN KEY (user_id) REFERENCES Users(user_id)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE Exercise_Logs (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id INTEGER,
                exercise_id INTEGER,
                sets INTEGER,
                repetitions INTEGER,
                duration INTEGER,
                camera_mode TEXT,
                FOREIGN KEY (session_id) REFERENCES Workout_Sessions(session_id),
                FOREIGN KEY (exercise_id) REFERENCES Exercises(exercise_id)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE AI_Analysis (
                analysis_id INTEGER PRIMARY KEY AUTOINCREMENT,
                log_id INTEGER,
                form_score REAL,
                posture_score REAL,
                stability_score REAL,
                range_of_motion REAL,
                mistakes_detected TEXT,
                correct_rep_percentage REAL,
                feedback TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (log_id) REFERENCES Exercise_Logs(log_id)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE Progress_Statistics (
                stat_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER UNIQUE,
                avg_form_score REAL,
                total_sessions INTEGER,
                total_repetitions INTEGER,
                improvement_percentage REAL,
                last_updated TEXT,
                FOREIGN KEY (user_id) REFERENCES Users(user_id)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE Recommendations (
                recommendation_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                recommendation_text TEXT,
                created_date TEXT,
                FOREIGN KEY (user_id) REFERENCES Users(user_id)
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Recommendations")
        db.execSQL("DROP TABLE IF EXISTS Progress_Statistics")
        db.execSQL("DROP TABLE IF EXISTS AI_Analysis")
        db.execSQL("DROP TABLE IF EXISTS Exercise_Logs")
        db.execSQL("DROP TABLE IF EXISTS Workout_Sessions")
        db.execSQL("DROP TABLE IF EXISTS Exercises")
        db.execSQL("DROP TABLE IF EXISTS Users")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun upsertUserProfile(
        authUid: String?,
        name: String,
        email: String,
        dob: String?,
        gender: String?,
        fitnessLevel: String?,
    ): Long {
        val existing = getUserProfile(authUid, email)
        val values = ContentValues().apply {
            put("auth_uid", authUid ?: existing?.authUid)
            put("name", if (name.isBlank()) existing?.name else name)
            put("email", email)
            put("dob", dob ?: existing?.dateOfBirth)
            put("gender", gender ?: existing?.gender)
            put("fitness_level", fitnessLevel ?: existing?.fitnessLevel)
        }

        return if (existing == null) {
            writableDatabase.insert("Users", null, values)
        } else {
            writableDatabase.update(
                "Users",
                values,
                "user_id = ?",
                arrayOf(existing.userId.toString()),
            )
            existing.userId
        }
    }

    fun getUserProfile(authUid: String?, email: String?): UserProfile? {
        if (!authUid.isNullOrBlank()) {
            readableDatabase.query(
                "Users",
                null,
                "auth_uid = ?",
                arrayOf(authUid),
                null,
                null,
                null,
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    return cursor.toUserProfile()
                }
            }
        }

        val safeEmail = email ?: return null
        readableDatabase.query(
            "Users",
            null,
            "email = ?",
            arrayOf(safeEmail),
            null,
            null,
            null,
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            return cursor.toUserProfile()
        }
    }

    fun saveWorkoutSession(
        authUid: String?,
        fallbackName: String,
        fallbackEmail: String,
        workout: WorkoutDefinition,
        summary: WorkoutSummary,
        cameraMode: String = "Front Camera",
        videoUri: String? = null,
    ) {
        val userId = upsertUserProfile(
            authUid = authUid,
            name = fallbackName,
            email = fallbackEmail,
            dob = null,
            gender = null,
            fitnessLevel = null,
        )
        val exerciseId = ensureExercise(workout)
        val timestamp = LocalDateTime.now()
        val sessionValues = ContentValues().apply {
            put("user_id", userId)
            put("session_date", timestamp.toLocalDate().toString())
            put("start_time", timestamp.minusMinutes(summary.durationMinutes.toLong()).toLocalTime().toString())
            put("end_time", timestamp.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
            put("duration", summary.durationMinutes)
            put("calories_burned", summary.caloriesBurned)
            put("video_uri", videoUri)
        }
        val sessionId = writableDatabase.insert("Workout_Sessions", null, sessionValues)

        val logValues = ContentValues().apply {
            put("session_id", sessionId)
            put("exercise_id", exerciseId)
            put("sets", 1)
            put("repetitions", summary.repetitions)
            put("duration", summary.durationMinutes)
            put("camera_mode", cameraMode)
        }
        val logId = writableDatabase.insert("Exercise_Logs", null, logValues)

        val analysisValues = ContentValues().apply {
            put("log_id", logId)
            put("form_score", summary.formScore)
            put("posture_score", summary.postureScore)
            put("stability_score", summary.stabilityScore)
            put("range_of_motion", summary.rangeOfMotion)
            put("mistakes_detected", summary.mistakesDetected)
            put("correct_rep_percentage", summary.correctRepPercentage)
            put("feedback", summary.feedback)
            put("created_at", timestamp.toString())
        }
        writableDatabase.insert("AI_Analysis", null, analysisValues)

        updateProgressStats(userId)
        val smartRecommendation = generateSmartRecommendation(userId, summary)
        saveRecommendation(userId, smartRecommendation, LocalDate.now())
    }

    private fun generateSmartRecommendation(userId: Long, currentSummary: WorkoutSummary): String {
        val lastSessions = readableDatabase.rawQuery(
            """
            SELECT a.form_score, a.mistakes_detected, s.session_date
            FROM Workout_Sessions s
            JOIN Exercise_Logs l ON s.session_id = l.session_id
            JOIN AI_Analysis a ON a.log_id = l.log_id
            WHERE s.user_id = ?
            ORDER BY s.session_id DESC
            LIMIT 3
            """.trimIndent(),
            arrayOf(userId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(Pair(cursor.getDouble(0), cursor.getString(1)))
                }
            }
        }

        if (lastSessions.size < 2) {
            return "Welcome! ${currentSummary.feedback} Keep showing up to unlock deeper AI insights."
        }

        val avgPastScore = lastSessions.drop(1).map { it.first }.average()
        val currentScore = currentSummary.formScore
        val commonMistakes = lastSessions.flatMap { it.second.split(", ") }.filter { it.isNotBlank() }
            .groupingBy { it }.eachCount()
        
        val topMistake = commonMistakes.maxByOrNull { it.value }?.key

        return when {
            currentScore > avgPastScore + 5 -> {
                "Great improvement! Your form is ${String.format("%.0f%%", currentScore - avgPastScore)} better than your average. ${if (topMistake != null) "Now, try to polish your $topMistake." else "Keep this intensity!"}"
            }
            currentScore < avgPastScore - 5 -> {
                "Your form slipped slightly today. Focus on $topMistake which has been a recurring challenge in your last few sets."
            }
            topMistake != null -> {
                "Consistency is key. You're maintaining a steady pace, but I noticed '$topMistake' is still coming up. Try slowing down the eccentric phase."
            }
            else -> "Solid work today. You're staying consistent with your form. Consider increasing your rep count next session."
        }
    }

    fun getProgressSnapshot(authUid: String?, email: String?, period: String = "Week"): ProgressSnapshot {
        val user = getUserProfile(authUid, email)
            ?: return ProgressSnapshot(0.0, 0, 0, 0.0, "Finish your first workout to get AI coaching.", emptyList())

        val dateFilter = when (period) {
            "Day" -> "date(session_date) >= date('now', '-1 day')"
            "Week" -> "date(session_date) >= date('now', '-7 days')"
            "Month" -> "date(session_date) >= date('now', '-30 days')"
            "Year" -> "date(session_date) >= date('now', '-365 days')"
            else -> "1=1"
        }

        val statsCursor = readableDatabase.rawQuery(
            """
            SELECT 
                AVG(a.form_score),
                COUNT(DISTINCT s.session_id),
                SUM(l.repetitions)
            FROM Workout_Sessions s
            JOIN Exercise_Logs l ON s.session_id = l.session_id
            JOIN AI_Analysis a ON a.log_id = l.log_id
            WHERE s.user_id = ? AND $dateFilter
            """.trimIndent(),
            arrayOf(user.userId.toString())
        )

        var avgFormScore = 0.0
        var totalSessions = 0
        var totalRepetitions = 0
        statsCursor.use { cursor ->
            if (cursor.moveToFirst()) {
                avgFormScore = cursor.getDouble(0)
                totalSessions = cursor.getInt(1)
                totalRepetitions = cursor.getInt(2)
            }
        }

        val improvement = 0.0 // Simplified for now

        val recommendation = readableDatabase.rawQuery(
            """
            SELECT recommendation_text
            FROM Recommendations
            WHERE user_id = ?
            ORDER BY recommendation_id DESC
            LIMIT 1
            """.trimIndent(),
            arrayOf(user.userId.toString()),
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else "Stay consistent and keep your form tight."
        }

        val recentSessions = readableDatabase.rawQuery(
            """
            SELECT e.exercise_name, s.session_date, l.repetitions, s.duration, a.form_score, a.feedback, s.session_id, s.video_uri
            FROM Workout_Sessions s
            JOIN Exercise_Logs l ON s.session_id = l.session_id
            JOIN Exercises e ON e.exercise_id = l.exercise_id
            JOIN AI_Analysis a ON a.log_id = l.log_id
            WHERE s.user_id = ? AND $dateFilter
            ORDER BY s.session_id DESC
            LIMIT 10
            """.trimIndent(),
            arrayOf(user.userId.toString()),
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        WorkoutSessionCard(
                            title = cursor.getString(0),
                            performedOn = cursor.getString(1),
                            repetitions = cursor.getInt(2),
                            durationMinutes = cursor.getInt(3),
                            formScore = cursor.getDouble(4),
                            feedback = cursor.getString(5),
                            sessionId = cursor.getInt(6),
                            videoUri = cursor.getString(7),
                        ),
                    )
                }
            }
        }

        val trends = recentSessions.groupBy { it.title }
            .mapValues { entry -> entry.value.map { it.formScore.toFloat() }.reversed() }

        return ProgressSnapshot(
            averageFormScore = avgFormScore,
            totalSessions = totalSessions,
            totalRepetitions = totalRepetitions,
            improvementPercentage = improvement,
            recommendation = recommendation,
            recentSessions = recentSessions,
            workoutTrends = trends,
            repetitionTrend = recentSessions.map { it.repetitions.toFloat() }.reversed(),
        )
    }

    fun getWorkoutHistory(authUid: String?, email: String?): List<WorkoutSessionCard> {
        val user = getUserProfile(authUid, email) ?: return emptyList()
        
        return readableDatabase.rawQuery(
            """
            SELECT e.exercise_name, s.session_date, l.repetitions, s.duration, a.form_score, a.feedback, s.session_id, s.video_uri
            FROM Workout_Sessions s
            JOIN Exercise_Logs l ON s.session_id = l.session_id
            JOIN Exercises e ON e.exercise_id = l.exercise_id
            JOIN AI_Analysis a ON a.log_id = l.log_id
            WHERE s.user_id = ?
            ORDER BY s.session_date ASC
            """.trimIndent(),
            arrayOf(user.userId.toString()),
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        WorkoutSessionCard(
                            title = cursor.getString(0),
                            performedOn = cursor.getString(1),
                            repetitions = cursor.getInt(2),
                            durationMinutes = cursor.getInt(3),
                            formScore = cursor.getDouble(4),
                            feedback = cursor.getString(5),
                            sessionId = cursor.getInt(6),
                            videoUri = cursor.getString(7),
                        ),
                    )
                }
            }
        }
    }

    private fun ensureExercise(workout: WorkoutDefinition): Long {
        readableDatabase.query(
            "Exercises",
            arrayOf("exercise_id"),
            "exercise_name = ?",
            arrayOf(workout.title),
            null,
            null,
            null,
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(0)
            }
        }

        val values = ContentValues().apply {
            put("exercise_name", workout.title)
            put("category", workout.category)
            put("difficulty_level", workout.difficulty)
            put("target_muscle_group", workout.targetMuscles)
        }
        return writableDatabase.insert("Exercises", null, values)
    }

    private fun updateProgressStats(userId: Long) {
        val aggregate = readableDatabase.rawQuery(
            """
            SELECT 
                AVG(a.form_score),
                COUNT(DISTINCT s.session_id),
                COALESCE(SUM(l.repetitions), 0)
            FROM Workout_Sessions s
            JOIN Exercise_Logs l ON s.session_id = l.session_id
            JOIN AI_Analysis a ON a.log_id = l.log_id
            WHERE s.user_id = ?
            """.trimIndent(),
            arrayOf(userId.toString()),
        )

        var avgForm = 0.0
        var sessions = 0
        var totalReps = 0
        aggregate.use { cursor ->
            if (cursor.moveToFirst()) {
                avgForm = cursor.getDouble(0)
                sessions = cursor.getInt(1)
                totalReps = cursor.getInt(2)
            }
        }

        val priorAverage = readableDatabase.rawQuery(
            "SELECT avg_form_score FROM Progress_Statistics WHERE user_id = ?",
            arrayOf(userId.toString()),
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
        }

        val improvement = if (priorAverage == 0.0) {
            avgForm
        } else {
            max(0.0, avgForm - priorAverage)
        }

        val values = ContentValues().apply {
            put("user_id", userId)
            put("avg_form_score", avgForm)
            put("total_sessions", sessions)
            put("total_repetitions", totalReps)
            put("improvement_percentage", improvement)
            put("last_updated", LocalDate.now().toString())
        }

        val updated = writableDatabase.update(
            "Progress_Statistics",
            values,
            "user_id = ?",
            arrayOf(userId.toString()),
        )
        if (updated == 0) {
            writableDatabase.insert("Progress_Statistics", null, values)
        }
    }

    private fun saveRecommendation(userId: Long, feedback: String, createdDate: LocalDate) {
        val values = ContentValues().apply {
            put("user_id", userId)
            put("recommendation_text", feedback)
            put("created_date", createdDate.toString())
        }
        writableDatabase.insert("Recommendations", null, values)
    }

    private fun android.database.Cursor.getIntOrNull(columnName: String): Int? {
        val index = getColumnIndexOrThrow(columnName)
        return if (isNull(index)) null else getInt(index)
    }

    private fun android.database.Cursor.toUserProfile(): UserProfile {
        return UserProfile(
            userId = getLong(getColumnIndexOrThrow("user_id")),
            authUid = getString(getColumnIndexOrThrow("auth_uid")),
            name = getString(getColumnIndexOrThrow("name")) ?: "",
            email = getString(getColumnIndexOrThrow("email")) ?: "",
            dateOfBirth = getString(getColumnIndexOrThrow("dob")),
            gender = getString(getColumnIndexOrThrow("gender")),
            fitnessLevel = getString(getColumnIndexOrThrow("fitness_level")),
        )
    }

    fun deleteSession(sessionId: Int) {
        writableDatabase.delete("Workout_Sessions", "session_id = ?", arrayOf(sessionId.toString()))
    }

    companion object {
        private const val DATABASE_NAME = "train_application.db"
        private const val DATABASE_VERSION = 7
    }
}
