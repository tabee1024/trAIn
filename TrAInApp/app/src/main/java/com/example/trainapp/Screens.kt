package com.example.trainapp


sealed class Screens (val screen: String) {
    data object Home: Screens("home")
    data object Profile: Screens("profile")
    data object Workouts: Screens("workouts")
    data object Notifications: Screens("notifications")
    data object Goals: Screens("goals")
    data object Help: Screens("help")
    data object AboutUs: Screens("about us")
    data object Nutrition: Screens("nutrition")
    data object Settings: Screens("settings")
    data object Search: Screens("search")
    data object Progress: Screens("progress")

    object WorkoutList : Screens("workout_list")

    object PushUp : Screens("push_up")

    object Squats : Screens("squats")

    object Lunges : Screens("lunges")
}
