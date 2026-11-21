package eu.tutorials.kl_boox_house

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 FIREBASE TEST
        val db = FirebaseFirestore.getInstance()
        db.collection("force_test")
            .add(mapOf("from" to "MainActivity"))
            .addOnSuccessListener {
                Log.d("FIREBASE", "DATA SAVED SUCCESSFULLY")
            }
            .addOnFailureListener {
                Log.e("FIREBASE", "SAVE FAILED", it)
            }

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
                Log.d("FIREBASE_SYNC", "Received ${firebaseSeats.size} seats from Firebase")

                // Update seatData with Firebase data
                seatData.clear()

                // Re-initialize all 57 seats
                for (i in 1..57) {
                    seatData["seat_$i"] = firebaseSeats["seat_$i"] ?: SeatUi(
                        seatNumber = i,
                        timeSlotBookings = mutableMapOf()
                    )
                }
            }
        }

        NavHost(navController = navController, startDestination = "loginpage") {
            composable(route = "loginpage") {
                LoginPage {
                    navController.navigate("mainscreen")
                }
            }

            composable(route = "mainscreen") {
                MainScreen(
                    seatData = seatData,
                    onNavigateToReports = {
                        navController.navigate("reports")
                    },
                    onNavigateToSeats = {
                        navController.navigate("seats")
                    },
                    onNavigateToMessages = {
                        navController.navigate("messages")
                    }
                )
            }

            composable(route = "reports") {
                ReportsScreen(
                    seatData = seatData,
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = "seats") {
                SeatsScreen(
                    seatData = seatData,
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }

            composable(route = "messages") {
                MessagesScreen(
                    seatData = seatData,
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}