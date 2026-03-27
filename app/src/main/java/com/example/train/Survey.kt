package com.example.train

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/* -----------------------------
   Data model + enums
----------------------------- */

enum class AgeRange(val label: String) {
    UNDER_18("< 18"), AGE_18_21("18 - 21"), AGE_22_26("22 - 26"),
    AGE_27_35("27 - 35"), AGE_36_44("36 - 44"), AGE_45_55("45 - 55"),
    AGE_56_67("56 - 67"), AGE_68_PLUS("68+")
}

enum class GenderOption(val label: String) {
    MALE("Male"), FEMALE("Female"), NON_BINARY("Non-binary"),
    PREFER_NOT_TO_SAY("Prefer not to say"), OTHER("Other")
}

enum class HealthLevel(val label: String) {
    POOR("Poor"), FAIR("Fair"), GOOD("Good"), VERY_GOOD("Very Good"), EXCELLENT("Excellent")
}

enum class FitnessLevel(val label: String) {
    BEGINNER("Beginner"), AVERAGE("Average"), EXPERIENCED("Experienced"), ATHLETE("Athlete")
}

enum class PrimaryGoal(val label: String) {
    WEIGHT_LOSS("Weight Loss"), MUSCLE_GAIN("Muscle Gain"),
    IMPROVE_ENDURANCE("Improve Endurance"), GENERAL_FITNESS("General Fitness"),
    ATHLETIC_PERFORMANCE("Athletic Performance"), MENTAL_HEALTH("Mental Health / Stress Relief")
}

enum class FrequencyWord(val label: String) {
    ONCE("Once"), TWICE("Twice"), SEVERAL_TIMES("Several times"), EVERY("Every")
}

enum class TimeUnit(val singular: String, val plural: String) {
    DAY("Day", "Days"), WEEK("Week", "Weeks"), MONTH("Month", "Months"), YEAR("Year", "Years");
    fun labelFor(n: Int?): String = if ((n ?: 1) == 1) singular else plural
}

data class WorkoutFrequency(
    val word: FrequencyWord,
    val everyN: Int?,
    val unit: TimeUnit,
) {
    fun display(): String = when (word) {
        FrequencyWord.SEVERAL_TIMES -> "Several times per ${unit.labelFor(2).lowercase()}"
        FrequencyWord.TWICE -> "Twice per ${unit.labelFor(2).lowercase()}"
        FrequencyWord.ONCE -> "Once per ${(everyN ?: 1)} ${unit.labelFor(everyN ?: 1).lowercase()}"
        FrequencyWord.EVERY -> "Every ${(everyN ?: 1)} ${unit.labelFor(everyN ?: 1).lowercase()}"
    }
}

enum class WorkoutType(val label: String) {
    STRENGTH("Strength"), CARDIO("Cardio"), HIIT("HIIT"), YOGA_MOBILITY("Yoga / Mobility"),
    SPORTS("Sports"), BODYWEIGHT("Bodyweight"), OUTDOOR("Outdoor")
}

enum class InjuryOrConcern(val label: String) {
    NONE("None"), HIGH_CHOLESTEROL("High cholesterol"), KNEE("Knee Issues"),
    BACK("Back Pain"), SHOULDER("Shoulder Issues"), ANKLE("Ankle Issues"), OTHER("Other")
}

enum class ExperienceLevel(val label: String) {
    NONE("None"), LT_6_MONTHS("< 6 months"), MONTHS_6_12("6–12 months"),
    YEARS_1_3("1–3 years"), YEARS_3_PLUS("3+ years")
}

data class UserSurvey(
    val nickname: String? = null,
    val ageRange: AgeRange? = null,
    val gender: GenderOption? = null,
    val genderOtherText: String? = null,
    val healthLevel: HealthLevel? = null,
    val fitnessLevel: FitnessLevel? = null,
    val primaryGoal: PrimaryGoal? = null,
    val workoutFrequency: WorkoutFrequency? = null,
    val enjoyedWorkouts: Set<WorkoutType> = emptySet(),
    val injuries: Set<InjuryOrConcern> = emptySet(),
    val previousExperience: ExperienceLevel? = null
)

/* -----------------------------
   Local Store
----------------------------- */

object UserProfileStore {
    var latestSurvey by mutableStateOf<UserSurvey?>(null)
    fun saveLocal(survey: UserSurvey) { latestSurvey = survey }
}

/* -----------------------------
   Survey UI
----------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreenRoute(userId: String, onDone: () -> Unit) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("Fitness Profile") }) }) { padding ->
        Box(Modifier.padding(padding)) { SurveyContent(userId, onDone) }
    }
}

@Composable
private fun SurveyContent(userId: String, onSubmitted: () -> Unit) {
    val scroll = rememberScrollState()
    var nickname by remember { mutableStateOf("") }
    var ageRange by remember { mutableStateOf<AgeRange?>(null) }
    var gender by remember { mutableStateOf<GenderOption?>(null) }
    var healthLevel by remember { mutableStateOf<HealthLevel?>(null) }
    var fitnessLevel by remember { mutableStateOf<FitnessLevel?>(null) }
    var primaryGoal by remember { mutableStateOf<PrimaryGoal?>(null) }
    var experience by remember { mutableStateOf<ExperienceLevel?>(null) }
    var injuries by remember { mutableStateOf(setOf<InjuryOrConcern>()) }
    var submitting by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scroll).padding(20.dp)) {
        Text("Welcome, $userId", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))

        Text("What should we call you?", fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Nickname") },
            placeholder = { Text("e.g. Alex") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        SingleChoiceGroup("Age Range", AgeRange.entries, ageRange) { ageRange = it }
        SingleChoiceGroup("Gender", GenderOption.entries, gender) { gender = it }
        SingleChoiceGroup("Health Level", HealthLevel.entries, healthLevel) { healthLevel = it }
        SingleChoiceGroup("Fitness Level", FitnessLevel.entries, fitnessLevel) { fitnessLevel = it }
        SingleChoiceGroup("Primary Goal", PrimaryGoal.entries, primaryGoal) { primaryGoal = it }
        SingleChoiceGroup("Experience", ExperienceLevel.entries, experience) { experience = it }

        Text("Injuries/Limitations", fontWeight = FontWeight.Bold)
        InjuryOrConcern.entries.forEach { opt ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = opt in injuries, onCheckedChange = { isChecked ->
                    injuries = if (isChecked) injuries + opt else injuries - opt
                })
                Text(opt.label)
            }
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                submitting = true
                val survey = UserSurvey(
                    nickname = nickname.ifBlank { userId },
                    ageRange = ageRange,
                    gender = gender,
                    healthLevel = healthLevel,
                    fitnessLevel = fitnessLevel,
                    primaryGoal = primaryGoal,
                    injuries = injuries,
                    previousExperience = experience
                )
                UserProfileStore.saveLocal(survey)
                onSubmitted()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !submitting && ageRange != null && primaryGoal != null
        ) { Text(if (submitting) "Saving..." else "Submit") }
    }
}

@Composable
private fun <T> SingleChoiceGroup(title: String, options: List<T>, selected: T?, onSelect: (T) -> Unit) where T : Enum<T> {
    val labelProvider: (T) -> String = {
        when(it) {
            is AgeRange -> it.label
            is GenderOption -> it.label
            is HealthLevel -> it.label
            is FitnessLevel -> it.label
            is PrimaryGoal -> it.label
            is ExperienceLevel -> it.label
            else -> it.name
        }
    }
    Text(title, fontWeight = FontWeight.Bold)
    options.forEach { opt ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = (opt == selected), onClick = { onSelect(opt) })
            Text(labelProvider(opt))
        }
    }
    Spacer(Modifier.height(12.dp))
}