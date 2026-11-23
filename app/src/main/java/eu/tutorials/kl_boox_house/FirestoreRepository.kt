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
        booking: TimeSlotBooking,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
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

        Log.d("FIRESTORE_SAVE", "🔄 Saving: Seat $seatNumber, Slot $slotIndex")

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
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE_SAVE", "❌ Failed to save", e)
                onFailure(e)
            }
    }

    // =========================
    // REAL-TIME LISTENER
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

                val result = mutableMapOf<String, SeatUi>()

                for (doc in snapshot.documents) {
                    try {
                        val seatNumber = doc.getLong("seatNumber")?.toInt() ?: continue
                        val slots = mutableMapOf<Int, TimeSlotBooking>()

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
                                } catch (e: Exception) {
                                    Log.e("FIRESTORE_PARSE", "   ❌ Failed to parse slot: $key", e)
                                }
                            }
                        }

                        result["seat_$seatNumber"] = SeatUi(
                            seatNumber = seatNumber,
                            timeSlotBookings = slots
                        )

                    } catch (e: Exception) {
                        Log.e("FIRESTORE_DOC", "❌ Failed to process document: ${doc.id}", e)
                    }
                }

                onUpdate(result)
            }
    }

    // =========================
    // DELETE BOOKING - WORKING VERSION
    // Uses Read-Modify-Write pattern because FieldValue.delete() doesn't work with dot notation
    // =========================
    fun deleteBooking(
        seatNumber: Int,
        slotIndex: Int,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val documentPath = "seat_$seatNumber"
        val fieldPath = "slots.$slotIndex"

        Log.d("FIRESTORE_DELETE", "🗑️ Deleting: $documentPath → $fieldPath")

        db.collection("seats")
            .document(documentPath)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Log.e("FIRESTORE_DELETE", "❌ Document doesn't exist!")
                    onFailure(Exception("Document not found"))
                    return@addOnSuccessListener
                }

                // Get all current data as a mutable map
                val currentData = doc.data?.toMutableMap() ?: mutableMapOf()

                Log.d("FIRESTORE_DELETE", "📋 Before: ${currentData.keys}")

                // Remove the specific slot field
                val removed = currentData.remove(fieldPath)

                if (removed != null) {
                    Log.d("FIRESTORE_DELETE", "🗑️ Removed field: $fieldPath")
                } else {
                    Log.w("FIRESTORE_DELETE", "⚠️ Field not found: $fieldPath")
                }

                Log.d("FIRESTORE_DELETE", "📋 After: ${currentData.keys}")

                // Write back the modified data (without the deleted field)
                db.collection("seats")
                    .document(documentPath)
                    .set(currentData)
                    .addOnSuccessListener {
                        Log.d("FIRESTORE_DELETE", "✅ Successfully deleted: Seat $seatNumber, Slot $slotIndex")
                        onSuccess()

                        // Verify deletion
                        db.collection("seats")
                            .document(documentPath)
                            .get()
                            .addOnSuccessListener { verifyDoc ->
                                val verifyData = verifyDoc.data
                                if (verifyData?.containsKey(fieldPath) == true) {
                                    Log.e("FIRESTORE_VERIFY", "❌ WARNING: Field still exists!")
                                } else {
                                    Log.d("FIRESTORE_VERIFY", "✅ Verified: Field successfully removed")
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FIRESTORE_DELETE", "❌ Failed to write document", e)
                        onFailure(e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE_DELETE", "❌ Failed to read document", e)
                onFailure(e)
            }
    }
}