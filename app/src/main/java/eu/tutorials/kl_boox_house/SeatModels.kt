package eu.tutorials.kl_boox_house

data class TimeSlotBooking(
    val occupiedBy: String,
    val billNo: String,
    val mobileNo: String,
    val admissionDate: String,
    val expiryDate: String,
    val occupiedSince: Long,
    val amountPaid: String = "0",
    val dueAmount: String = "0",
    val paymentMode: String = "Cash",
    val subscriptionType: String = "Monthly"
)

data class SeatUi(
    val seatNumber: Int,
    val timeSlotBookings: MutableMap<Int, TimeSlotBooking> = mutableMapOf()
)
