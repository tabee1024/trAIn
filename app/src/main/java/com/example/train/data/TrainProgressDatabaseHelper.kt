package com.example.train.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.train.model.ProgressSnapshot
import com.example.train.model.WorkoutSessionCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class TrainProgressDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE Workout_Sessions (
                session_id INTEGER PRIMARY KEY AUTOINCREMENT,
                exercise_name TEXT NOT NULL,
                session_date TEXT NOT NULL,
                duration_minutes INTEGER NOT NULL,
                calories_burned INTEGER DEFAULT 0
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE Exercise_Logs (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                session_id INTEGER NOT NULL,
                repetitions INTEGER NOT NULL,
                camera_mode TEXT,
                FOREIGN KEY (session_id) REFERENCES Workout_Sessions(session_id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE AI_Analysis (
                analysis_id INTEGER PRIMARY KEY AUTOINCREMENT,
                log_id INTEGER NOT NULL,
                form_score REAL NOT NULL,
                feedback TEXT,
                mistakes_detected TEXT,
                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (log_id) REFERENCES Exercise_Logs(log_id) ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE Recommendations (
                recommendation_id INTEGER PRIMARY KEY AUTOINCREMENT,
                recommendation_text TEXT NOT NULL,
                created_date TEXT NOT NULL
            )
            """.trimIndent(),
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS Recommendations")
        db.execSQL("DROP TABLE IF EXISTS AI_Analysis")
        db.execSQL("DROP TABLE IF EXISTS Exercise_Logs")
        db.execSQL("DROP TABLE IF EXISTS Workout_Sessions")
        onCreate(db)
    }

    fun seedDemoProgressIfEmpty() {
        val count = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM Workout_Sessions",
            emptyArray(),
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
        if (count > 0) return

        val today = LocalDate.now()
        saveWorkoutSession("Push up", today.minusDays(2), 24, 7, 88.0, "Keep your core tight through each rep.")
        saveWorkoutSession("Squats", today.minusDays(1), 28, 8, 90.0, "Strong depth. Keep your knees tracking over toes.")
        saveWorkoutSession("Push up", today, 30, 8, 92.0, "Great control today. Keep your body aligned from head to heels.")
    }

    fun saveWorkoutSession(
        exerciseName: String,
        sessionDate: LocalDate,
        repetitions: Int,
        durationMinutes: Int,
        formScore: Double,
        feedback: String,
    ) {
        writableDatabase.beginTransaction()
        try {
            val sessionId = writableDatabase.insert(
                "Workout_Sessions",
                null,
                ContentValues().apply {
                    put("exercise_name", exerciseName)
                    put("session_date", sessionDate.toString())
                    put("duration_minutes", durationMinutes)
                    put("calories_burned", (durationMinutes * 6) + (repetitions * 2))
                },
            )
            val logId = writableDatabase.insert(
                "Exercise_Logs",
                null,
                ContentValues().apply {
                    put("session_id", sessionId)
                    put("repetitions", repetitions)
                    put("camera_mode", "On-device pose tracking")
                },
            )
            writableDatabase.insert(
                "AI_Analysis",
                null,
                ContentValues().apply {
                    put("log_id", logId)
                    put("form_score", formScore)
                    put("feedback", feedback)
                    put("mistakes_detected", "")
                },
            )
            writableDatabase.insert(
                "Recommendations",
                null,
                ContentValues().apply {
                    put("recommendation_text", buildRecommendation(formScore, feedback))
                    put("created_date", sessionDate.toString())
                },
            )
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    fun getProgressSnapshot(period: String): ProgressSnapshot {
        val dateFilter = when (period) {
            "Day" -> "date(session_date) >= date('now', '-1 day')"
            "Week" -> "date(session_date) >= date('now', '-7 days')"
            "Month" -> "date(session_date) >= date('now', '-30 days')"
            "Year" -> "date(session_date) >= date('now', '-365 days')"
            else -> "1=1"
        }

        val sessions = readableDatabase.rawQuery(
            """
            SELECT s.exercise_name, s.session_date, l.repetitions, s.duration_minutes, a.form_score, a.feedback
            FROM Workout_Sessions s
            JOIN Exercise_Logs l ON s.session_id = l.session_id
            JOIN AI_Analysis a ON a.log_id = l.log_id
            WHERE $dateFilter
            ORDER BY s.session_date DESC, s.session_id DESC
            LIMIT 10
            """.trimIndent(),
            emptyArray(),
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        WorkoutSessionCard(
                            title = cursor.getString(0),
                            performedOn = formatDate(cursor.getString(1)),
                            repetitions = cursor.getInt(2),
                            durationMinutes = cursor.getInt(3),
                            formScore = cursor.getDouble(4),
                            feedback = cursor.getString(5) ?: "Workout completed.",
                        ),
                    )
                }
            }
        }

        val latest = sessions.firstOrNull()
        val averageFormScore = sessions.map { it.formScore }.average().takeUnless { it.isNaN() } ?: 0.0
        val totalRepetitions = sessions.sumOf { it.repetitions }
        val totalMinutes = latest?.durationMinutes ?: 0

        return ProgressSnapshot(
            repsCompleted = latest?.repetitions ?: 0,
            accuracy = averageFormScore.roundToInt().coerceIn(0, 100),
            timeSpent = "%02d:%02d".format(totalMinutes, 0),
            averageFormScore = averageFormScore,
            totalSessions = sessions.size,
            totalRepetitions = totalRepetitions,
            improvementPercentage = calculateImprovement(period),
            currentStreakDays = getCurrentStreakDays(),
            recommendation = latestRecommendation(),
            recentSessions = sessions,
            repetitionTrend = sessions.asReversed().map { it.repetitions.toFloat() },
        )
    }

    private fun calculateImprovement(period: String): Double {
        val currentWindow = getAverageForWindow(period, previous = false)
        val previousWindow = getAverageForWindow(period, previous = true)
        if (previousWindow == 0.0) return 0.0
        return ((currentWindow - previousWindow) / previousWindow) * 100.0
    }

    private fun getAverageForWindow(period: String, previous: Boolean): Double {
        val filter = when (period) {
            "Day" -> if (previous) {
                "date(session_date) >= date('now', '-2 day') AND date(session_date) < date('now', '-1 day')"
            } else {
                "date(session_date) >= date('now', '-1 day')"
            }
            "Week" -> if (previous) {
                "date(session_date) >= date('now', '-14 days') AND date(session_date) < date('now', '-7 days')"
            } else {
                "date(session_date) >= date('now', '-7 days')"
            }
            "Month" -> if (previous) {
                "date(session_date) >= date('now', '-60 days') AND date(session_date) < date('now', '-30 days')"
            } else {
                "date(session_date) >= date('now', '-30 days')"
            }
            "Year" -> if (previous) {
                "date(session_date) >= date('now', '-730 days') AND date(session_date) < date('now', '-365 days')"
            } else {
                "date(session_date) >= date('now', '-365 days')"
            }
            else -> "1=1"
        }
        return readableDatabase.rawQuery(
            """
            SELECT AVG(a.form_score)
            FROM Workout_Sessions s
            JOIN Exercise_Logs l ON s.session_id = l.session_id
            JOIN AI_Analysis a ON a.log_id = l.log_id
            WHERE $filter
            """.trimIndent(),
            emptyArray(),
        ).use { cursor ->
            if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getDouble(0) else 0.0
        }
    }

    private fun latestRecommendation(): String {
        return readableDatabase.rawQuery(
            """
            SELECT recommendation_text
            FROM Recommendations
            ORDER BY recommendation_id DESC
            LIMIT 1
            """.trimIndent(),
            emptyArray(),
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(0)
            } else {
                "Complete a workout to unlock AI coaching tips."
            }
        }
    }

    private fun getCurrentStreakDays(): Int {
        val dates = readableDatabase.rawQuery(
            """
            SELECT DISTINCT session_date
            FROM Workout_Sessions
            ORDER BY session_date DESC
            """.trimIndent(),
            emptyArray(),
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    runCatching { LocalDate.parse(cursor.getString(0)) }.getOrNull()?.let(::add)
                }
            }
        }
        if (dates.isEmpty()) return 0

        var expectedDate = LocalDate.now()
        if (dates.first().isBefore(expectedDate)) expectedDate = dates.first()

        var streak = 0
        for (date in dates) {
            when {
                date == expectedDate -> {
                    streak += 1
                    expectedDate = expectedDate.minusDays(1)
                }
                date.isBefore(expectedDate) -> break
            }
        }
        return streak
    }

    private fun buildRecommendation(formScore: Double, feedback: String): String {
        return when {
            formScore >= 90 -> "Great work. $feedback"
            formScore >= 75 -> "Good session. Slow down slightly and keep your alignment consistent."
            else -> "Focus on controlled reps before increasing speed or volume."
        }
    }

    private fun formatDate(value: String): String {
        return runCatching {
            LocalDate.parse(value).format(DateTimeFormatter.ofPattern("MMM d"))
        }.getOrDefault(value)
    }

    companion object {
        private const val DATABASE_NAME = "train_progress.db"
        private const val DATABASE_VERSION = 1
    }
}
