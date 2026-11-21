package eu.tutorials.kl_boox_house

import io.github.sceneview.node.ModelNode
import java.text.SimpleDateFormat
import java.util.*

data class SubscriptionType(val name: String, val days: Int)

fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date())
}

fun calculateExpiryDate(admissionDate: String, days: Int): String {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        val date = sdf.parse(admissionDate) ?: return ""
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_MONTH, days)
        sdf.format(calendar.time)
    } catch (e: Exception) {
        android.util.Log.e("CALC_EXPIRY", "Failed to calculate expiry from: '$admissionDate'", e)
        ""
    }
}

fun calculateDaysBetween(startDate: String, endDate: String): Int {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        val start = sdf.parse(startDate) ?: return 0
        val end = sdf.parse(endDate) ?: return 0
        val diffInMillis = end.time - start.time
        (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) {
        0
    }
}

fun isDateWithinDays(dateString: String, days: Int): Boolean {
    return try {
        if (dateString.isBlank()) return false
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        val targetDate = sdf.parse(dateString) ?: return false

        val calTarget = Calendar.getInstance().apply {
            time = targetDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val calNow = Calendar.getInstance()
        val diffInMillis = calTarget.timeInMillis - calNow.timeInMillis
        val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)

        diffInDays in 0..days
    } catch (e: Exception) {
        android.util.Log.e("DATE_WITHIN", "Failed to check date: '$dateString'", e)
        false
    }
}

fun isDateExpired(dateString: String): Boolean {
    return try {
        if (dateString.isBlank()) {
            android.util.Log.w("EXPIRY_CHECK", "Empty expiry date string")
            return false
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.isLenient = false
        val targetDate = sdf.parse(dateString) ?: return false

        // Booking stays valid until end of expiry day (23:59:59)
        val calTarget = Calendar.getInstance().apply {
            time = targetDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val calNow = Calendar.getInstance()
        val isExpired = calNow.after(calTarget)

        android.util.Log.d(
            "EXPIRY_CHECK",
            "Date: '$dateString' | Expired: $isExpired"
        )

        isExpired
    } catch (e: Exception) {
        android.util.Log.e("EXPIRY_CHECK", "Failed to parse date: '$dateString'", e)
        false
    }
}

// Removes expired bookings from local state AND deletes from Firebase
fun removeExpiredBookings(seatData: MutableMap<String, SeatUi>) {
    android.util.Log.d("AUTO_DELETE", "Checking for expired bookings across ${seatData.size} seats...")

    var totalExpired = 0

    seatData.toList().forEach { (seatKey, seat) ->
        val expiredSlots = mutableListOf<Int>()

        seat.timeSlotBookings.forEach { (slotIndex, booking) ->
            if (booking.expiryDate.isBlank()) {
                android.util.Log.w("AUTO_DELETE", "Skipping seat ${seat.seatNumber}, slot $slotIndex — expiry date is empty")
                return@forEach
            }

            if (isDateExpired(booking.expiryDate)) {
                expiredSlots.add(slotIndex)
                android.util.Log.d(
                    "AUTO_DELETE",
                    "EXPIRED → Seat ${seat.seatNumber}, Slot $slotIndex | ${booking.occupiedBy} | Expiry: ${booking.expiryDate}"
                )
            }
        }

        if (expiredSlots.isNotEmpty()) {
            // Update local state immediately
            val updatedBookings = seat.timeSlotBookings.toMutableMap()
            expiredSlots.forEach { slotIndex -> updatedBookings.remove(slotIndex) }
            seatData[seatKey] = seat.copy(timeSlotBookings = updatedBookings)

            // Delete from Firebase
            expiredSlots.forEach { slotIndex ->
                totalExpired++
                FirestoreRepository.deleteBooking(
                    seatNumber = seat.seatNumber,
                    slotIndex = slotIndex,
                    onSuccess = {
                        android.util.Log.d("AUTO_DELETE", "Firestore deleted: Seat ${seat.seatNumber}, Slot $slotIndex")
                    },
                    onFailure = { e ->
                        android.util.Log.e("AUTO_DELETE_ERROR", "Failed to delete: Seat ${seat.seatNumber}, Slot $slotIndex", e)
                    }
                )
            }
        }
    }

    if (totalExpired > 0) {
        android.util.Log.d("AUTO_DELETE", "Total expired bookings deleted: $totalExpired")
    } else {
        android.util.Log.d("AUTO_DELETE", "No expired bookings found")
    }
}

// ModelNode extension functions — unchanged
fun ModelNode.findAndNameSeats() {
    fun traverseAndName(node: ModelNode, depth: Int = 0) {
        if (node.name?.startsWith("seat_") == true) {
            node.scale = io.github.sceneview.math.Scale(
                node.scale.x * 1.0f,
                node.scale.y * 1.0f,
                node.scale.z * 1.0f
            )
        }
        val childCountMethod = node.javaClass.methods.find { it.name == "getChildCount" }
        val getChildMethod = node.javaClass.methods.find { it.name == "getChild" && it.parameterTypes.size == 1 }
        if (childCountMethod != null && getChildMethod != null) {
            val childCount = childCountMethod.invoke(node) as Int
            for (i in 0 until childCount) {
                val child = getChildMethod.invoke(node, i)
                if (child is ModelNode) traverseAndName(child, depth + 1)
            }
        }
    }
    traverseAndName(this)
}

fun ModelNode.highlightSeat(seatNumber: Int) {
    fun searchAndHighlight(node: ModelNode) {
        if (node.name == "seat_$seatNumber") {
            node.scale = io.github.sceneview.math.Scale(
                node.scale.x * 1.5f,
                node.scale.y * 1.5f,
                node.scale.z * 1.5f
            )
        }
        val childCountMethod = node.javaClass.methods.find { it.name == "getChildCount" }
        val getChildMethod = node.javaClass.methods.find { it.name == "getChild" && it.parameterTypes.size == 1 }
        if (childCountMethod != null && getChildMethod != null) {
            val childCount = childCountMethod.invoke(node) as Int
            for (i in 0 until childCount) {
                val child = getChildMethod.invoke(node, i)
                if (child is ModelNode) searchAndHighlight(child)
            }
        }
    }
    resetAllSeats()
    searchAndHighlight(this)
}

fun ModelNode.resetAllSeats() {
    fun resetSeats(node: ModelNode) {
        if (node.name?.startsWith("seat_") == true) {
            val currentScale = node.scale
            node.scale = io.github.sceneview.math.Scale(
                currentScale.x / 1.5f,
                currentScale.y / 1.5f,
                currentScale.z / 1.5f
            )
        }
        val childCountMethod = node.javaClass.methods.find { it.name == "getChildCount" }
        val getChildMethod = node.javaClass.methods.find { it.name == "getChild" && it.parameterTypes.size == 1 }
        if (childCountMethod != null && getChildMethod != null) {
            val childCount = childCountMethod.invoke(node) as Int
            for (i in 0 until childCount) {
                val child = getChildMethod.invoke(node, i)
                if (child is ModelNode) resetSeats(child)
            }
        }
    }
    resetSeats(this)
}