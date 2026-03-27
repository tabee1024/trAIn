package com.example.myapplication1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
//import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.FitnessCenter
//import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.TextButton
import androidx.navigation.compose.NavHost
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
//import com.example.myapplication1.ui.theme.MyApplication1Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Home()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val userName = "John" // Replace with dynamic user name

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(navController)
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("    trAIn") },
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
            },
            bottomBar = {
                BottomNavigationBar(navController)
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(padding)
            ) {
                composable("home") { HomePageContent(userName, navController) }
                composable("profile") { BlankPage("Profile Page") }
                composable("goals") { BlankPage("Goals Page") }
                composable("workouts") { BlankPage("Workouts Page") }
                composable("progress") { BlankPage("Progress Page") }
                composable("favorites") { BlankPage("Favorites Page") }
                composable("time") { BlankPage("Time Spend Page") }
                composable("notifications") { BlankPage("Notifications Page") }
                composable("nutrition") { BlankPage("Nutrition Page") }
                composable("help") { BlankPage("Help/Support Page") }
                //composable("about") { BlankPage("About Us Page") }
                composable("about") {(AboutUs())}
                composable("search") { BlankPage("Search Page") }
                composable("settings") { BlankPage("Settings Page") }
                composable("logout") { BlankPage("You have been logged out.") }
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

        // Sliding images carousel
        ImageCarousel()

        Spacer(modifier = Modifier.height(16.dp))


        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { navController.navigate("progress") }) { Text("Progress") }
            Button(onClick = { navController.navigate("favorites") }) { Text("Favorites") }
            Button(onClick = { navController.navigate("time") }) { Text("Time Spend") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("help") }) { Text("Help") }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("workouts") }) {
            Text("Start Workout")
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, "Home") },
            selected = true,
            onClick = { navController.navigate("home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Search, "Search") },
            selected = false,
            onClick = { navController.navigate("search") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Notifications, "Notifications") },
            selected = false,
            onClick = { navController.navigate("notifications") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.FitnessCenter, "Progress") },
            selected = false,
            onClick = { navController.navigate("progress") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, "Settings") },
            selected = false,
            onClick = { navController.navigate("settings") }
        )
    }
}

@Composable
fun DrawerContent(navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val options = listOf(
            "Profile" to "profile",
            "Goals" to "goals",
            "Workouts" to "workouts",
            "Progress" to "progress",
            "Notifications" to "notifications",
            "Nutrition" to "nutrition",
            "Help/Support" to "help",
            "About Us" to "about",
            "Logout" to "logout"
        )
        options.forEach { (title, route) ->
            TextButton(onClick = { navController.navigate(route) }) {
                Text(title)
            }
        }
    }
}

@Composable
fun BlankPage(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    Home()
}

@Preview(showBackground = true, name = "Home Page")
@Composable
fun HomePagePreview() {
    HomePageContent(userName = "John", navController = rememberNavController())
}

@Preview(showBackground = true, name = "Blank Page")
@Composable
fun BlankPagePreview() {
    BlankPage("Profile Page")
}

@Preview(showBackground = true, name = "Drawer")
@Composable
fun DrawerPreview() {
    DrawerContent(navController = rememberNavController())
}

@Preview(showBackground = true, name = "Bottom Navigation")
@Composable
fun BottomNavPreview() {
    BottomNavigationBar(navController = rememberNavController())
}

