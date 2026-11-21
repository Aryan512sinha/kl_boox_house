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
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = FirebaseFirestore.getInstance()
        db.collection("force_test")
            .add(mapOf("from" to "MainActivity"))
            .addOnSuccessListener { Log.d("FIREBASE", "DATA SAVED SUCCESSFULLY") }
            .addOnFailureListener { Log.e("FIREBASE", "SAVE FAILED", it) }

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
        val seatData = remember { mutableStateMapOf<String, SeatUi>() }

        // Initialize seats and start Firebase listener
        LaunchedEffect(Unit) {
            // Initialize all 57 seats
            for (i in 1..57) {
                if (!seatData.containsKey("seat_$i")) {
                    seatData["seat_$i"] = SeatUi(
                        seatNumber = i,
                        timeSlotBookings = mutableMapOf()
                    )
                }
            }

            // Start Firebase listener with expiry filtering built in
            FirestoreRepository.listenToSeats { firebaseSeats ->
                Log.d("FIREBASE_SYNC", "Listener triggered! Received ${firebaseSeats.size} seats")

                firebaseSeats.forEach { (key, updatedSeat) ->
                    // Filter out expired slots BEFORE storing in seatData
                    val filteredBookings = updatedSeat.timeSlotBookings
                        .filter { (_, booking) -> !isDateExpired(booking.expiryDate) }
                        .toMutableMap()

                    seatData[key] = updatedSeat.copy(timeSlotBookings = filteredBookings)

                    // Delete expired slots from Firebase
                    updatedSeat.timeSlotBookings.forEach { (slotIndex, booking) ->
                        if (isDateExpired(booking.expiryDate)) {
                            Log.d("FIREBASE_SYNC", "Auto-deleting expired slot $slotIndex for seat ${updatedSeat.seatNumber}")
                            FirestoreRepository.deleteBooking(
                                seatNumber = updatedSeat.seatNumber,
                                slotIndex = slotIndex,
                                onSuccess = {
                                    Log.d("FIREBASE_SYNC", "Deleted expired: seat ${updatedSeat.seatNumber}, slot $slotIndex")
                                },
                                onFailure = { e ->
                                    Log.e("FIREBASE_SYNC", "Failed to delete expired slot", e)
                                }
                            )
                        }
                    }
                }

                // Reset seats not present in Firebase
                for (i in 1..57) {
                    val seatKey = "seat_$i"
                    if (!firebaseSeats.containsKey(seatKey) && seatData.containsKey(seatKey)) {
                        seatData[seatKey] = SeatUi(
                            seatNumber = i,
                            timeSlotBookings = mutableMapOf()
                        )
                    }
                }

                Log.d("FIREBASE_SYNC", "Sync complete. Seat count: ${seatData.size}")
            }
        }

        // Periodic expired booking cleanup (backup safety net)
        LaunchedEffect(Unit) {
            delay(5000) // Wait longer for Firebase to fully populate
            Log.d("AUTO_DELETE", "Running initial expired bookings check...")
            removeExpiredBookings(seatData)

            while (true) {
                delay(60000)
                Log.d("AUTO_DELETE", "Running scheduled expired bookings check...")
                removeExpiredBookings(seatData)
            }
        }

        NavHost(navController = navController, startDestination = "loginpage") {
            composable(route = "loginpage") {
                LoginPage { navController.navigate("mainscreen") }
            }

            composable(route = "mainscreen") {
                MainScreen(
                    seatData = seatData,
                    onNavigateToReports = {
                        navController.navigate("reports") { popUpTo("mainscreen") }
                    },
                    onNavigateToSeats = {
                        navController.navigate("seats") { popUpTo("mainscreen") }
                    },
                    onNavigateToMessages = {
                        navController.navigate("messages") { popUpTo("mainscreen") }
                    }
                )
            }

            composable(route = "reports") {
                ReportsScreen(
                    seatData = seatData,
                    onBackPressed = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigate("mainscreen") {
                            popUpTo("mainscreen") { inclusive = true }
                        }
                    },
                    onNavigateToSeats = {
                        navController.navigate("seats") { popUpTo("mainscreen") }
                    },
                    onNavigateToMessages = {
                        navController.navigate("messages") { popUpTo("mainscreen") }
                    }
                )
            }

            composable(route = "seats") {
                SeatsScreen(
                    seatData = seatData,
                    onBackPressed = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigate("mainscreen") {
                            popUpTo("mainscreen") { inclusive = true }
                        }
                    },
                    onNavigateToMessages = {
                        navController.navigate("messages") { popUpTo("mainscreen") }
                    },
                    onNavigateToReports = {
                        navController.navigate("reports") { popUpTo("mainscreen") }
                    }
                )
            }

            composable(route = "messages") {
                MessagesScreen(
                    seatData = seatData,
                    onBackPressed = { navController.popBackStack() },
                    onNavigateToHome = {
                        navController.navigate("mainscreen") {
                            popUpTo("mainscreen") { inclusive = true }
                        }
                    },
                    onNavigateToSeats = {
                        navController.navigate("seats") { popUpTo("mainscreen") }
                    },
                    onNavigateToReports = {
                        navController.navigate("reports") { popUpTo("mainscreen") }
                    }
                )
            }
        }
    }
}