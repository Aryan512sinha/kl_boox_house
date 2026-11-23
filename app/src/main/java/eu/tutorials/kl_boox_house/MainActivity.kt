package eu.tutorials.kl_boox_house

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import android.util.Log

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MyApp()
            }
        }
    }

    @Composable
    fun MyApp() {
        val navController = rememberNavController()

        // Shared seat data across screens
        val seatData = remember { mutableStateMapOf<String, SeatUi>() }

        // 🔥 FIX: Initialize Firebase listener ONCE when app starts
        LaunchedEffect(Unit) {
            // Initialize all 57 seats first (so UI doesn't break)
            for (i in 1..57) {
                if (!seatData.containsKey("seat_$i")) {
                    seatData["seat_$i"] = SeatUi(
                        seatNumber = i,
                        timeSlotBookings = mutableMapOf()
                    )
                }
            }

            // 🔥 CRITICAL: Start listening to Firebase updates
            FirestoreRepository.listenToSeats { firebaseSeats ->
                // Update only changed or missing seats; never rebuild the entire map.
                for (i in 1..57) {
                    val key = "seat_$i"
                    val updated = firebaseSeats[key] ?: SeatUi(seatNumber = i, timeSlotBookings = mutableMapOf())
                    if (seatData[key] != updated) {
                        seatData[key] = updated
                    }
                }
            }
        }

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        fun navigateTo(route: String) {
            if (currentRoute == route) return
            navController.navigate(route) {
                popUpTo("mainscreen") {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = "loginpage",
                modifier = Modifier.weight(1f),
                enterTransition = { fadeIn(tween(120)) },
                exitTransition = { fadeOut(tween(80)) },
                popEnterTransition = { fadeIn(tween(120)) },
                popExitTransition = { fadeOut(tween(80)) }
            ) {
                composable(route = "loginpage") {
                    LoginPage {
                        navController.navigate("mainscreen") {
                            popUpTo("loginpage") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                composable(route = "mainscreen") {
                    MainScreen(
                        seatData = seatData,
                        onNavigateToReports = { navigateTo("reports") },
                        onNavigateToSeats = { navigateTo("seats") },
                        onNavigateToMessages = { navigateTo("messages") }
                    )
                }

                composable(route = "reports") {
                    ReportsScreen(
                        seatData = seatData,
                        onBackPressed = { navigateTo("mainscreen") }
                    )
                }

                composable(route = "seats") {
                    SeatsScreen(
                        seatData = seatData,
                        onBackPressed = { navigateTo("mainscreen") }
                    )
                }

                composable(route = "messages") {
                    MessagesScreen(
                        seatData = seatData,
                        onBackPressed = { navigateTo("mainscreen") }
                    )
                }
            }

            if (currentRoute != "loginpage" && currentRoute != null) {
                EnhancedBottomNavigation(
                    selectedItem = when (currentRoute) {
                        "seats" -> 1
                        "messages" -> 2
                        "reports" -> 3
                        else -> 0
                    },
                    onItemSelected = { index ->
                        when (index) {
                            0 -> navigateTo("mainscreen")
                            1 -> navigateTo("seats")
                            2 -> navigateTo("messages")
                            3 -> navigateTo("reports")
                        }
                    }
                )
            }
        }
    }
}