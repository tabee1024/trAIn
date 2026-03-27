package com.example.train

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.train.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            TrAInAppTheme {
                TrAInNavigationWrapper()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrAInNavigationWrapper() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    // Routes that should NOT show the TopBar or BottomBar
    val authRoutes = listOf(Screens.SignUp.screen, "survey/{userId}")

    var pendingRoute by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pendingRoute?.let { route -> navController.navigate(route) }
        } else {
            android.widget.Toast.makeText(context, "Camera permission required", android.widget.Toast.LENGTH_SHORT).show()
        }
        pendingRoute = null
    }

    val navigateToWorkout: (String) -> Unit = { route ->
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            navController.navigate(route)
        } else {
            pendingRoute = route
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute !in authRoutes,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = OffWhite) {
                TrAInDrawerContent(navController = navController) { scope.launch { drawerState.close() } }
            }
        }
    ) {
        Scaffold(
            containerColor = OffWhite,
            topBar = {
                if (currentRoute !in authRoutes) {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = OffWhite),
                        title = { Text("trAIn", fontWeight = FontWeight.ExtraBold) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate(Screens.Profile.screen) }) {
                                Icon(Icons.Rounded.AccountCircle, contentDescription = "Profile")
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (currentRoute !in authRoutes) TrAInBottomBar(navController)
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screens.SignUp.screen,
                modifier = Modifier.padding(paddingValues)
            ) {
                // SIGN UP -> SURVEY
                composable(Screens.SignUp.screen) {
                    SignUpScreen { userId ->
                        navController.navigate("survey/$userId") {
                            popUpTo(Screens.SignUp.screen) { inclusive = true }
                        }
                    }
                }

                // SURVEY -> HOME
                composable(route = "survey/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: ""
                    SurveyScreenRoute(
                        userId = userId,
                        onDone = {
                            navController.navigate(Screens.Home.screen) {
                                // This ensures the user can't go back to the survey after finishing
                                popUpTo("survey/{userId}") { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screens.Home.screen) { Home(navController) }
                composable(Screens.Workouts.screen) {
                    Workouts(onWorkoutSelected = { route -> navigateToWorkout(route) })
                }
                composable(Screens.Profile.screen) { Profile() }
                composable(Screens.Notifications.screen) { Notifications() }
                composable(Screens.Goals.screen) { Goals() }
                composable(Screens.AboutUs.screen) { AboutUs() }
                composable(Screens.Settings.screen) { Settings() }
                composable(Screens.Search.screen) { Search() }
                composable(Screens.Favorites.screen) { Favorites() }
                composable(Screens.Help.screen) { Help() }
                composable(Screens.Progress.screen) {
                    Progress(reps = 30, accuracy = 92, timeSpent = "08:14",
                        onExit = { navController.navigate(Screens.Home.screen) },
                        onRestart = { navigateToWorkout(Screens.Workouts.screen) }
                    )
                }
                composable(Screens.PushUp.screen) { Push_up(onPushUpClick = { navController.navigate(Screens.Search.screen) }) }
                composable(Screens.Squats.screen) { Squats() }
                composable(Screens.Lunges.screen) { Lunges() }
            }
        }
    }
}

@Composable
fun TrAInBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val items = listOf(
        Triple("Home", Screens.Home.screen, Icons.Default.Home),
        Triple("Search", Screens.Search.screen, Icons.Default.Search),
        Triple("Notifications", Screens.Notifications.screen, Icons.Default.Notifications),
        Triple("Profile", Screens.Profile.screen, Icons.Default.Person),
        Triple("Settings", Screens.Settings.screen, Icons.Default.Settings)
    )
    NavigationBar(containerColor = DeepCoffee, contentColor = DeepNavy) {
        items.forEach { (label, route, icon) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(icon, contentDescription = label, tint = if (isSelected) AccentGold else Color.White) },
                label = null,
                colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
            )
        }
    }
}

@Composable
fun TrAInDrawerContent(navController: NavHostController, onClose: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("trAIn Menu", color = CharcoalBlue, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CharcoalBlue.copy(alpha = 0.2f))
        val menuItems = listOf("Home" to Screens.Home.screen, "Profile" to Screens.Profile.screen, "Goals" to Screens.Goals.screen, "Workouts" to Screens.Workouts.screen, "Settings" to Screens.Settings.screen)
        menuItems.forEach { (title, route) ->
            val isSelected = currentRoute == route
            NavigationDrawerItem(
                label = { Text(title, color = if (isSelected) Color.White else CharcoalBlue) },
                selected = isSelected,
                onClick = { navController.navigate(route); onClose() },
                colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, selectedContainerColor = SteelBlue),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}