package eu.tutorials.kl_boox_house

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    // =========================
    // SAVE / UPDATE BOOKING
    // =========================
    fun saveBooking(
        seatNumber: Int,
        slotIndex: Int,
        booking: TimeSlotBooking
    ) {
        val slotData = mapOf(
            "name" to booking.occupiedBy,
            "billNo" to booking.billNo,
            "mobileNo" to booking.mobileNo,
            "admissionDate" to booking.admissionDate,
            "expiryDate" to booking.expiryDate,
            "amountPaid" to booking.amountPaid,
            "dueAmount" to booking.dueAmount,
            "paymentMode" to booking.paymentMode,
            "subscriptionType" to booking.subscriptionType,
            "createdAt" to booking.occupiedSince,
            "seatNumber" to seatNumber
        )

        db.collection("seats")
            .document("seat_$seatNumber")
            .set(
                mapOf(
                    "seatNumber" to seatNumber,
                    "slots.$slotIndex" to slotData
                ),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                Log.d("FIRESTORE_SAVE", "✅ Saved: Seat $seatNumber, Slot $slotIndex")
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE_SAVE", "❌ Failed to save", e)
            }
    }

    // =========================
    // REAL-TIME LISTENER - FIXED
    // =========================
    fun listenToSeats(
        onUpdate: (Map<String, SeatUi>) -> Unit
    ) {
        db.collection("seats")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FIRESTORE_LISTEN", "❌ Listen failed", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.w("FIRESTORE_LISTEN", "⚠️ Snapshot is null")
                    return@addSnapshotListener
                }

                Log.d("FIRESTORE_LISTEN", "📥 Received ${snapshot.size()} documents")

                val result = mutableMapOf<String, SeatUi>()

                for (doc in snapshot.documents) {
                    try {
                        val seatNumber = doc.getLong("seatNumber")?.toInt() ?: continue
                        val slots = mutableMapOf<Int, TimeSlotBooking>()

                        // 🔥 FIX: Read slots directly from document root
                        val docData = doc.data ?: continue

                        // Loop through all fields looking for "slots.X"
                        for ((key, value) in docData) {
                            if (key.startsWith("slots.")) {
                                try {
                                    val slotIndex = key.removePrefix("slots.").toInt()
                                    val slotMap = value as? Map<*, *> ?: continue

                                    val booking = TimeSlotBooking(
                                        occupiedBy = slotMap["name"]?.toString() ?: "",
                                        billNo = slotMap["billNo"]?.toString() ?: "",
                                        mobileNo = slotMap["mobileNo"]?.toString() ?: "",
                                        admissionDate = slotMap["admissionDate"]?.toString() ?: "",
                                        expiryDate = slotMap["expiryDate"]?.toString() ?: "",
                                        occupiedSince = (slotMap["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                        amountPaid = slotMap["amountPaid"]?.toString() ?: "0",
                                        dueAmount = slotMap["dueAmount"]?.toString() ?: "0",
                                        paymentMode = slotMap["paymentMode"]?.toString() ?: "Cash",
                                        subscriptionType = slotMap["subscriptionType"]?.toString() ?: "Monthly"
                                    )

                                    slots[slotIndex] = booking
                                    Log.d("FIRESTORE_PARSE", "✅ Parsed: Seat $seatNumber, Slot $slotIndex -> ${booking.occupiedBy}")
                                } catch (e: Exception) {
                                    Log.e("FIRESTORE_PARSE", "❌ Failed to parse slot: $key", e)
                                }
                            }
                        }

                        result["seat_$seatNumber"] = SeatUi(
                            seatNumber = seatNumber,
                            timeSlotBookings = slots
                        )

                        Log.d("FIRESTORE_SEAT", "✅ Loaded Seat $seatNumber with ${slots.size} bookings")

                    } catch (e: Exception) {
                        Log.e("FIRESTORE_DOC", "❌ Failed to process document: ${doc.id}", e)
                    }
                }

                Log.d("FIRESTORE_RESULT", "📤 Returning ${result.size} seats to UI")
                onUpdate(result)
            }
    }

    // =========================
    // DELETE BOOKING (Optional)
    // =========================
    fun deleteBooking(
        seatNumber: Int,
        slotIndex: Int
    ) {
        db.collection("seats")
            .document("seat_$seatNumber")
            .update("slots.$slotIndex", com.google.firebase.firestore.FieldValue.delete())
            .addOnSuccessListener {
                Log.d("FIRESTORE_DELETE", "✅ Deleted: Seat $seatNumber, Slot $slotIndex")
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE_DELETE", "❌ Failed to delete", e)
            }
    }
}