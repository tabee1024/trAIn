package com.example.train

sealed class Screens(val screen: String) {
    data object SignUp : Screens("signup")

    // Updated Survey route to include the userId argument for dynamic navigation
    data object Survey : Screens("survey/{userId}") {
        fun createRoute(userId: String) = "survey/$userId"
    }

    data object Home : Screens("home")
    data object Profile : Screens("profile")
    data object Workouts : Screens("workouts")
    data object Notifications : Screens("notifications")
    data object Goals : Screens("goals")
    data object AboutUs : Screens("about")
    data object Settings : Screens("settings")
    data object PushUp : Screens("pushup")
    data object Help : Screens("help")
    data object Favorites : Screens("favorites")
    data object Search : Screens("search")
    data object Progress : Screens("progress")
    data object Squats : Screens("squats")
    data object Lunges : Screens("lunges")
}