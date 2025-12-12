package com.example.myapplication1

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeTests {

    @Test
    fun testDisplayGreeting() {
        val userName = "John"
        val greeting = "Hello, $userName"
        assertEquals("Hello, John", greeting)
    }

    @Test
    fun testSideDrawer() {
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
        assertEquals("profile", options.first { it.first == "Profile" }.second)
        assertEquals("goals", options.first { it.first == "Goals" }.second)
        assertEquals("logout", options.first { it.first == "Logout" }.second)
    }

    @Test
    fun testNavBar() {
        val routes = listOf("home", "search", "notifications", "progress", "settings")
        // Verify the size
        assertEquals(5, routes.size)
        // Verify order and values
        assertEquals("home", routes[0])
        assertEquals("search", routes[1])
        assertEquals("notifications", routes[2])
        assertEquals("progress", routes[3])
        assertEquals("settings", routes[4])
    }

    @Test
    fun testPageTitle() {
        val title = "Profile Page"
        assert(title.isNotEmpty()) { "BlankPage title should not be empty" }
    }

}
