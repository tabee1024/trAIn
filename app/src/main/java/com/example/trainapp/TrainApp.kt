package com.example.trainapp

import android.app.Activity
import android.content.Intent
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import java.util.Calendar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.trainapp.camera.CameraWorkoutActivity
import com.example.trainapp.data.TrainDatabaseHelper
import com.example.trainapp.model.ProgressSnapshot
import com.example.trainapp.model.UserProfile
import com.example.trainapp.model.WorkoutCatalog
import com.example.trainapp.model.WorkoutDefinition
import com.example.trainapp.ui.theme.Beige
import com.example.trainapp.ui.theme.DBrown
import com.example.trainapp.ui.theme.Swirl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val LOGIN_ROUTE = "login"
private const val SIGNUP_ROUTE = "signup"
private const val DASHBOARD_ROUTE = "dashboard"
private const val WORKOUTS_ROUTE = "workouts"
private const val PROGRESS_ROUTE = "progress"
private const val PROFILE_ROUTE = "profile"
private const val GOALS_ROUTE = "goals"
private const val NUTRITION_ROUTE = "nutrition"
private const val NOTIFICATIONS_ROUTE = "notifications"
private const val SETTINGS_ROUTE = "settings"
private const val HELP_ROUTE = "help"
private const val ABOUT_ROUTE = "about"
private const val DETAIL_PREFIX = "workout/"

@Composable
fun TrainApp() {
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var isCheckingAuth by remember { mutableStateOf(true) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
            isCheckingAuth = false
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Beige) {
        when {
            isCheckingAuth -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DBrown)
                }
            }
            currentUser == null -> AuthFlow(auth = auth)
            else -> MainFlow(auth = auth)
        }
    }
}

@Composable
private fun AuthFlow(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val database = remember { TrainDatabaseHelper(context) }

    NavHost(
        navController = navController,
        startDestination = LOGIN_ROUTE,
    ) {
        composable(LOGIN_ROUTE) {
            LoginScreen(
                onLogin = { email, password ->
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnFailureListener { Toast.makeText(context, it.message, Toast.LENGTH_LONG).show() }
                },
                onSignupClick = { navController.navigate(SIGNUP_ROUTE) },
            )
        }
        composable(SIGNUP_ROUTE) {
            SignupScreen(
                onSignup = { name, dob, gender, fitnessLevel, email, password ->
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener { result ->
                            database.upsertUserProfile(
                                authUid = result.user?.uid,
                                name = name,
                                email = email,
                                dob = dob,
                                gender = gender.ifBlank { null },
                                fitnessLevel = fitnessLevel.ifBlank { null },
                            )
                        }
                        .addOnFailureListener { Toast.makeText(context, it.message, Toast.LENGTH_LONG).show() }
                },
                onLoginClick = { navController.popBackStack() },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainFlow(auth: FirebaseAuth) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val database = remember { TrainDatabaseHelper(context) }
    var refreshTick by remember { mutableIntStateOf(0) }
    val currentUser = auth.currentUser
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route

    val profileState by produceState<UserProfile?>(initialValue = null, currentUser?.uid, refreshTick) {
        value = withContext(Dispatchers.IO) {
            database.getUserProfile(currentUser?.uid, currentUser?.email)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("trAIN") },
                actions = {
                    TextButton(onClick = { auth.signOut() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                        Spacer(Modifier.width(6.dp))
                        Text("Logout")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                listOf(
                    Triple(DASHBOARD_ROUTE, Icons.Default.FitnessCenter, "Home"),
                    Triple(WORKOUTS_ROUTE, Icons.AutoMirrored.Filled.List, "Workouts"),
                    Triple(PROGRESS_ROUTE, Icons.Default.BarChart, "Progress"),
                    Triple(PROFILE_ROUTE, Icons.Default.AccountCircle, "Profile"),
                ).forEach { (route, icon, label) ->
                    NavigationBarItem(
                        selected = currentDestination == route,
                        onClick = {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = DASHBOARD_ROUTE,
            modifier = Modifier.padding(padding),
        ) {
            composable(DASHBOARD_ROUTE) {
                DashboardScreen(
                    profile = profileState,
                    onOpenWorkouts = { navController.navigate(WORKOUTS_ROUTE) },
                    onOpenProgress = { navController.navigate(PROGRESS_ROUTE) },
                    onNavigate = { navController.navigate(it) },
                )
            }
            composable(WORKOUTS_ROUTE) {
                WorkoutListScreen(
                    workouts = WorkoutCatalog.workouts,
                    onWorkoutSelected = { navController.navigate("$DETAIL_PREFIX${it.id}") },
                )
            }
            composable("$DETAIL_PREFIX{id}") { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 1
                WorkoutDetailScreen(
                    workout = WorkoutCatalog.byId(workoutId),
                    onBack = { navController.popBackStack() },
                    onWorkoutFinished = { refreshTick++ },
                )
            }
            composable(PROGRESS_ROUTE) {
                ProgressScreen(
                    auth = auth,
                    database = database,
                    refreshTick = refreshTick,
                )
            }
            composable(PROFILE_ROUTE) {
                ProfileScreen(
                    profile = profileState,
                    email = currentUser?.email.orEmpty(),
                    onSave = { name, dob, gender, fitnessLevel ->
                        database.upsertUserProfile(
                            authUid = currentUser?.uid,
                            name = name,
                            email = currentUser?.email ?: "",
                            dob = dob,
                            gender = gender,
                            fitnessLevel = fitnessLevel
                        )
                        refreshTick++
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            composable(GOALS_ROUTE) {
                FeatureScreen("Goals", "Set weekly rep goals, improve your form score, and track your consistency.")
            }
            composable(NUTRITION_ROUTE) {
                FeatureScreen("Nutrition", "Pair your workouts with simple meal ideas, hydration reminders, and recovery tips.")
            }
            composable(NOTIFICATIONS_ROUTE) {
                FeatureScreen("Notifications", "Stay on top of workouts with practice reminders and AI coaching summaries.")
            }
            composable(SETTINGS_ROUTE) {
                FeatureScreen("Settings", "Manage your account, display preferences, and app notifications.")
            }
            composable(HELP_ROUTE) {
                HelpScreen(onBack = { navController.popBackStack() })
            }
            composable(ABOUT_ROUTE) {
                AboutUsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onSignupClick: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = DBrown
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text("Welcome to trAIN", style = MaterialTheme.typography.headlineMedium, color = DBrown)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onLogin(email, password) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Login")
        }
        TextButton(onClick = onSignupClick) {
            Text("Don't have an account? Sign Up")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignup: (String, String, String, String, String, String) -> Unit,
    onLoginClick: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var fitnessLevel by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, day ->
            dob = String.format("%04d-%02d-%02d", year, month + 1, day)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Create Account", style = MaterialTheme.typography.headlineLarge, color = DBrown)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = dob,
            onValueChange = { },
            label = { Text("Date of Birth") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Default.DateRange, "Select Date")
                }
            }
        )
        Spacer(Modifier.height(12.dp))

        // Gender Selector
        var genderExpanded by remember { mutableStateOf(false) }
        val genders = listOf("Male", "Female", "Other", "Prefer not to say")
        ExposedDropdownMenuBox(
            expanded = genderExpanded,
            onExpandedChange = { genderExpanded = !genderExpanded }
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                genders.forEach { selection ->
                    DropdownMenuItem(
                        text = { Text(selection) },
                        onClick = {
                            gender = selection
                            genderExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Fitness Level Selector
        var fitnessExpanded by remember { mutableStateOf(false) }
        val levels = listOf("Beginner", "Intermediate", "Advanced")
        ExposedDropdownMenuBox(
            expanded = fitnessExpanded,
            onExpandedChange = { fitnessExpanded = !fitnessExpanded }
        ) {
            OutlinedTextField(
                value = fitnessLevel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fitness Level") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fitnessExpanded) },
                modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = fitnessExpanded,
                onDismissRequest = { fitnessExpanded = false }
            ) {
                levels.forEach { selection ->
                    DropdownMenuItem(
                        text = { Text(selection) },
                        onClick = {
                            fitnessLevel = selection
                            fitnessExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { onSignup(name, dob, gender, fitnessLevel, email, password) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Sign Up")
        }
        TextButton(onClick = onLoginClick) {
            Text("Already have an account? Login")
        }
    }
}

@Composable
fun DashboardScreen(
    profile: UserProfile?,
    onOpenWorkouts: () -> Unit,
    onOpenProgress: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Home(
        onProgressClick = onOpenProgress,
        onGoalsClick = { onNavigate(GOALS_ROUTE) },
        onFavoritesClick = { },
        onWorkoutsClick = onOpenWorkouts,
        onFABClick = { onNavigate(HELP_ROUTE) }
    )
}

@Composable
fun WorkoutListScreen(
    workouts: List<WorkoutDefinition>,
    onWorkoutSelected: (WorkoutDefinition) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Choose a Workout", style = MaterialTheme.typography.headlineMedium, color = DBrown)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(workouts) { workout ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onWorkoutSelected(workout) },
                    colors = CardDefaults.cardColors(containerColor = Swirl)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(workout.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("${workout.durationMinutes} min • ${workout.difficulty}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutDetailScreen(
    workout: WorkoutDefinition,
    onBack: () -> Unit,
    onWorkoutFinished: () -> Unit,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            onWorkoutFinished()
            onBack()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text(workout.title, style = MaterialTheme.typography.headlineMedium, color = DBrown)
        Spacer(Modifier.height(8.dp))
        Text(workout.description)
        Spacer(Modifier.height(16.dp))
        Text("Tips:", fontWeight = FontWeight.Bold)
        workout.tips.forEach { tip ->
            Text("• $tip")
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                val intent = Intent(context, CameraWorkoutActivity::class.java).apply {
                    putExtra(CameraWorkoutActivity.EXTRA_WORKOUT_ID, workout.id)
                }
                launcher.launch(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Workout")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: UserProfile?,
    email: String,
    onSave: (String, String?, String?, String?) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember(profile) { mutableStateOf(profile?.name ?: "") }
    var dob by remember(profile) { mutableStateOf(profile?.dateOfBirth ?: "") }
    var gender by remember(profile) { mutableStateOf(profile?.gender ?: "") }
    var fitnessLevel by remember(profile) { mutableStateOf(profile?.fitnessLevel ?: "") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, day ->
            dob = String.format("%04d-%02d-%02d", year, month + 1, day)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = DBrown
        )
        Spacer(Modifier.height(16.dp))

        Text(email, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Spacer(Modifier.height(32.dp))

        if (!isEditing) {
            ProfileInfoRow("Name", name)
            ProfileInfoRow("Date of Birth", dob.ifBlank { "Not set" })
            ProfileInfoRow("Age", profile?.age?.toString() ?: "N/A")
            ProfileInfoRow("Gender", gender.ifBlank { "Not set" })
            ProfileInfoRow("Fitness Level", fitnessLevel.ifBlank { "Not set" })

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Edit Profile")
            }
        } else {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            // Date of Birth Selector
            OutlinedTextField(
                value = dob,
                onValueChange = { },
                label = { Text("Date of Birth") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.DateRange, "Select Date")
                    }
                }
            )
            Spacer(Modifier.height(16.dp))

            // Gender Selector
            var genderExpanded by remember { mutableStateOf(false) }
            val genders = listOf("Male", "Female", "Other", "Prefer not to say")
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genders.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                gender = selection
                                genderExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Fitness Level Selector
            var fitnessExpanded by remember { mutableStateOf(false) }
            val levels = listOf("Beginner", "Intermediate", "Advanced")
            ExposedDropdownMenuBox(
                expanded = fitnessExpanded,
                onExpandedChange = { fitnessExpanded = !fitnessExpanded }
            ) {
                OutlinedTextField(
                    value = fitnessLevel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fitness Level") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fitnessExpanded) },
                    modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true).fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = fitnessExpanded,
                    onDismissRequest = { fitnessExpanded = false }
                ) {
                    levels.forEach { selection ->
                        DropdownMenuItem(
                            text = { Text(selection) },
                            onClick = {
                                fitnessLevel = selection
                                fitnessExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(40.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(
                    onClick = { isEditing = false },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        onSave(name, dob, gender, fitnessLevel)
                        isEditing = false
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.titleLarge, color = DBrown, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray.copy(alpha = 0.5f)))
    }
}

@Composable
fun FeatureScreen(title: String, description: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineLarge, color = DBrown)
        Spacer(modifier = Modifier.height(16.dp))
        Text(description, textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun HelpScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Help & Support", style = MaterialTheme.typography.headlineMedium, color = DBrown)
        Spacer(Modifier.height(16.dp))
        Text("Contact us at support@trainapp.com for any queries.")
        Spacer(Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}

@Composable
fun AboutUsScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("About trAIN", style = MaterialTheme.typography.headlineMedium, color = DBrown)
        Spacer(Modifier.height(16.dp))
        Text("trAIN is an AI-powered fitness companion designed to help you perfect your form and track your progress.")
        Spacer(Modifier.weight(1f))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
