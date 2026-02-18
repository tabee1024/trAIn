package com.example.trainapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trainapp.ui.theme.DBrown
import com.example.trainapp.ui.theme.Emperor
import com.example.trainapp.ui.theme.Swirl
import com.example.trainapp.ui.theme.TrAInAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            TrAInAppTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation(navController)
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation(navController: NavController) {
    val navigationController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current.applicationContext

    val selected = remember {
        mutableStateOf(Icons.Default.Home)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                Box(modifier = Modifier
                    .background(DBrown)
                    .fillMaxWidth()
                    .height(150.dp)) {
                    Text(text = "")
                }
                //Divider
                HorizontalDivider()
                NavigationDrawerItem(label = {Text(text = "Home", color = Emperor) },
                    selected = false,
                    icon = {Icon(imageVector = Icons.Default.Home, contentDescription = "home", tint = Emperor) },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Home.screen) {
                            popUpTo(0)
                        }
                    }
                    )
                NavigationDrawerItem(label = {Text(text = "Profile", color = Emperor) },
                    selected = false,
                    icon = {Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "home", tint = Emperor) },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Profile.screen) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationDrawerItem(label = {Text(text = "Goals", color = Emperor) },
                    selected = false,
                    icon = {Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "goals", tint = Emperor) },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Goals.screen) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationDrawerItem(label = {Text(text = "Workouts", color = Emperor) },
                    selected = false,
                    icon = {Icon(imageVector = Icons.Default.Build, contentDescription = "workouts", tint = Emperor) },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Workouts.screen) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationDrawerItem(label = {Text(text = "Notifications", color = Emperor) },
                    selected = false,
                    icon = {Icon(imageVector = Icons.Default.Notifications, contentDescription = "notifications", tint = Emperor) },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Notifications.screen) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationDrawerItem(label = {Text(text = "Nutrition", color = Emperor) },
                    selected = false,
                    icon = {Icon(imageVector = Icons.Default.Build, contentDescription = "nutrition", tint = Emperor) },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Nutrition.screen) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationDrawerItem(label = {Text(text = "Help", color = Emperor) },
                    selected = false,
                    icon = {Icon(imageVector = Icons.Default.MailOutline, contentDescription = "help", tint = Emperor) },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Help.screen) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationDrawerItem(label = {Text(text = "About Us", color = Emperor) },
                    selected = false,
                    icon = {Icon(imageVector = Icons.Default.Info, contentDescription = "about us", tint = Emperor) },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.AboutUs.screen) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationDrawerItem(label = {Text(text = "Search", color = Emperor) },
                    selected = false,
                    icon = {Icon(imageVector = Icons.Default.Search, contentDescription = "search", tint = Emperor) },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Search.screen) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationDrawerItem(label = {Text(text = "Settings", color = Emperor) },
                    selected = false,
                    icon = {Icon(imageVector = Icons.Default.Settings, contentDescription = "settings", tint = Emperor) },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navigationController.navigate(Screens.Settings.screen) {
                            popUpTo(0)
                        }
                    }
                )
                NavigationDrawerItem(label = {Text(text = "Logout", color = Emperor) },
                    selected = false,
                    icon = {Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "log out", tint = Emperor) },
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        Toast.makeText(context, "Logout", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                val coroutineScope = rememberCoroutineScope()

                TopAppBar(title = { Text(text = "trAIn") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DBrown,
                        titleContentColor = Swirl,
                        navigationIconContentColor = Swirl,
                        actionIconContentColor = Swirl
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(
                                Icons.Rounded.Menu, contentDescription = "MenuButton"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("profile")
                        }) {
                            Icon(
                                Icons.Rounded.AccountCircle,
                                contentDescription = "Profile"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomAppBar(containerColor = DBrown) {
                    // Home
                    IconButton(onClick = {
                        selected.value = Icons.Default.Home
                        navigationController.navigate(Screens.Home.screen){
                            popUpTo(0)
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(26.dp),
                            tint = if (selected.value == Icons.Default.Home) Color.White else Color.DarkGray)
                    }
                    //Search
                    IconButton(onClick = {
                        selected.value = Icons.Default.Search
                        navigationController.navigate(Screens.Search.screen){
                            popUpTo(0)
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(26.dp),
                            tint = if (selected.value == Icons.Default.Search) Color.White else Color.DarkGray)
                    }
                    // Notifications
                    IconButton(onClick = {
                        selected.value = Icons.Default.Notifications
                        navigationController.navigate(Screens.Notifications.screen){
                            popUpTo(0)
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(26.dp),
                            tint = if (selected.value == Icons.Default.Notifications) Color.White else Color.DarkGray)
                    }
                    // Progress
                    IconButton(onClick = {
                        selected.value = Icons.Default.Face
                        navigationController.navigate(Screens.Progress.screen){
                            popUpTo(0)
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(26.dp),
                            tint = if (selected.value == Icons.Default.Face) Color.White else Color.DarkGray)
                    }
                    // Settings
                    IconButton(onClick = {
                        selected.value = Icons.Default.Settings
                        navigationController.navigate(Screens.Settings.screen){
                            popUpTo(0)
                        }
                    }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(26.dp),
                            tint = if (selected.value == Icons.Default.Settings) Color.White else Color.DarkGray)
                    }

                }
            }
        ) {
            NavHost(
                navController = navigationController,
                startDestination = Screens.Home.screen
            ){
                composable(Screens.Home.screen) {
                    Home(
                        onProgressClick = { navigationController.navigate(Screens.Progress.screen) },
                        onGoalsClick = { navigationController.navigate(Screens.Goals.screen) },
                        onFavoritesClick = { navigationController.navigate(Screens.Progress.screen) },
                        onWorkoutsClick = { navigationController.navigate(Screens.Workouts.screen) },
                        onFABClick = { navigationController.navigate(Screens.Help.screen){popUpTo(0)} }
                    )
                }
                composable(Screens.Profile.screen){Profile()}
                composable(Screens.Goals.screen){Goals()}
                composable(Screens.Notifications.screen){Notifications()}
                composable(Screens.Nutrition.screen){Nutrition()}
                composable(Screens.Help.screen){Help()}
                composable(Screens.Workouts.screen){Workouts(navigationController)}
                composable(Screens.AboutUs.screen){AboutUs()}
                composable(Screens.Search.screen){Search()}
                composable(Screens.Progress.screen){Progress()}
                composable(Screens.Settings.screen){Settings()}
                composable(Screens.PushUp.screen){Push_up()}
                composable(Screens.Squats.screen){Squats()}
                composable(Screens.Lunges.screen){Lunges()}
            }
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun AppPreview() {
    val navController = rememberNavController()
    TrAInAppTheme {
        Navigation(navController)
    }
}
