package com.example.train

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainNavigationWrapper()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationWrapper() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    // Track current route to hide bars on Sign-Up page
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val userName = "John"

    // Only enable the drawer gesture if NOT on the signup page
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentRoute != "signup",
        drawerContent = {
            if (currentRoute != "signup") {
                ModalDrawerSheet {
                    DrawerContent(navController) {
                        scope.launch { drawerState.close() }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                // Hide Top Bar on Sign-Up
                if (currentRoute != "signup") {
                    TopAppBar(
                        title = { Text("trAIn", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { navController.navigate("profile") }) {
                                Icon(Icons.Default.Person, contentDescription = "Profile")
                            }
                        }
                    )
                }
            },
            bottomBar = {
                // Hide Bottom Bar on Sign-Up
                if (currentRoute != "signup") {
                    BottomNavigationBar(navController)
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "signup", // App now starts here
                modifier = Modifier.padding(padding)
            ) {
                composable("signup") {
                    SignUpScreen(onSignUpSuccess = {
                        navController.navigate("home") {
                            // Clear signup from history so back button exits app
                            popUpTo("signup") { inclusive = true }
                        }
                    })
                }
                composable("home") { HomePageContent(userName, navController) }
                composable("profile") { BlankPage("Profile Page") }
                composable("workouts") { BlankPage("Workouts Page") }
                composable("progress") { BlankPage("Progress Page") }
                composable("favorites") { BlankPage("Favorites Page") }
                composable("time") { BlankPage("Time Spent Page") }
                composable("notifications") { BlankPage("Notifications Page") }
                composable("settings") { BlankPage("Settings Page") }
                composable("about") { AboutUs() }
                composable("search") { BlankPage("Search Page") }
                composable("logout") {
                    // Navigate back to signup and clear everything
                    navController.navigate("signup") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }
}

@Composable
fun HomePageContent(userName: String, navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hello, $userName", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        ImageCarousel() // Your smooth sliding carousel

        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { navController.navigate("progress") }, modifier = Modifier.weight(1f)) { Text("Progress", fontSize = 11.sp) }
            Button(onClick = { navController.navigate("favorites") }, modifier = Modifier.weight(1f)) { Text("Favorites", fontSize = 11.sp) }
            Button(onClick = { navController.navigate("time") }, modifier = Modifier.weight(1f)) { Text("Time", fontSize = 11.sp) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("workouts") },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("START WORKOUT")
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        val items = listOf(
            Triple("home", Icons.Default.Home, "Home"),
            Triple("search", Icons.Default.Search, "Search"),
            Triple("notifications", Icons.Default.Notifications, "Alerts"),
            Triple("progress", Icons.Default.FitnessCenter, "Stats"),
            Triple("settings", Icons.Default.Settings, "Settings")
        )
        items.forEach { (route, icon, label) ->
            NavigationBarItem(
                icon = { Icon(icon, label) },
                label = { Text(label) },
                selected = currentRoute == route,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun DrawerContent(navController: NavHostController, onDestinationClicked: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Navigation", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        HorizontalDivider()
        val options = listOf("Profile" to "profile", "Workouts" to "workouts", "About Us" to "about", "Logout" to "logout")
        options.forEach { (title, route) ->
            NavigationDrawerItem(
                label = { Text(title) },
                selected = false,
                onClick = {
                    navController.navigate(route)
                    onDestinationClicked()
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

@Composable
fun BlankPage(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}