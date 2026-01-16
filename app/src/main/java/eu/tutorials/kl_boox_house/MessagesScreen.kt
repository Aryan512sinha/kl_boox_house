package eu.tutorials.kl_boox_house

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ExpiringBooking(
    val seatNumber: Int,
    val timeSlot: String,
    val studentName: String,
    val billNo: String,
    val mobileNo: String,
    val expiryDate: String,
    val daysUntilExpiry: Int,
    val subscriptionType: String,
    val amountPaid: String,
    val dueAmount: String
)

@Composable
fun MessagesScreen(
    seatData: MutableMap<String, SeatUi>,
    onBackPressed: () -> Unit
) {
    val timeSlots = listOf(
        "6:00 AM - 10:00 AM",
        "10:00 AM - 2:00 PM",
        "2:00 PM - 6:00 PM",
        "6:00 PM - 10:00 PM"
    )

    // Auto-remove bookings that are 2 days past expiry
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60000) // Check every minute
            removeBookingsTwoDaysPastExpiry(seatData)
        }
    }

    // Get all expiring bookings (expiring within 1 day or already expired but within 2 days past expiry)
    val expiringBookings by remember {
        derivedStateOf {
            val bookings = mutableListOf<ExpiringBooking>()

            seatData.forEach { (seatKey, seat) ->
                seat.timeSlotBookings.forEach { (slotIndex, booking) ->
                    val daysUntilExpiry = calculateDaysUntilExpiry(booking.expiryDate)

                    // Show if expiring within 1 day OR already expired but within 2 days past expiry
                    if (daysUntilExpiry <= 1 && daysUntilExpiry >= -2) {
                        bookings.add(
                            ExpiringBooking(
                                seatNumber = seat.seatNumber,
                                timeSlot = timeSlots.getOrNull(slotIndex) ?: "Unknown Slot",
                                studentName = booking.occupiedBy,
                                billNo = booking.billNo,
                                mobileNo = booking.mobileNo,
                                expiryDate = booking.expiryDate,
                                daysUntilExpiry = daysUntilExpiry,
                                subscriptionType = booking.subscriptionType,
                                amountPaid = booking.amountPaid,
                                dueAmount = booking.dueAmount
                            )
                        )
                    }
                }
            }

            // Sort: Expired first, then by days until expiry (ascending)
            bookings.sortedBy { it.daysUntilExpiry }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2C2C2C),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        Icons.Default.Email,
                        contentDescription = "Messages",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Expiry Notifications",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${expiringBookings.size} booking(s) expiring soon",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Messages List
            if (expiringBookings.isEmpty()) {
                // Empty State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(120.dp),
                            shape = CircleShape,
                            color = Color(0xFFE8F5E9)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = "No Messages",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "All Clear!",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "No subscriptions expiring soon.\nYou'll be notified when bookings\nare about to expire.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(expiringBookings) { booking ->
                        ExpiringBookingCard(booking = booking)
                    }

                    // Bottom padding
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ExpiringBookingCard(booking: ExpiringBooking) {
    val isExpired = booking.daysUntilExpiry < 0
    val urgencyLevel = when {
        booking.daysUntilExpiry <= -1 -> UrgencyLevel.EXPIRED
        booking.daysUntilExpiry == 0 -> UrgencyLevel.TODAY
        else -> UrgencyLevel.TOMORROW
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row with Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Left side - Student info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.studentName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1976D2).copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Seat ${booking.seatNumber}",
                                color = Color(0xFF1976D2),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = "•",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = booking.timeSlot,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                // Right side - Urgency Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = urgencyLevel.backgroundColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = urgencyLevel.textColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = urgencyLevel.label,
                            color = urgencyLevel.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(12.dp))

            // Contact Information Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Contact Information",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Phone Number
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Default.Call,
                                    contentDescription = "Phone",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Mobile Number",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                            Text(
                                text = booking.mobileNo,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bill Number
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bill No:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = booking.billNo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subscription Details
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow(
                    label = "Subscription:",
                    value = booking.subscriptionType,
                    valueColor = Color.Black
                )

                DetailRow(
                    label = "Expiry Date:",
                    value = booking.expiryDate,
                    valueColor = if (isExpired) Color.Red else Color(0xFFFFA500),
                    valueFontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Amount Paid:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = "₹${booking.amountPaid}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Due Amount with indicator
                val hasDue = booking.dueAmount.toIntOrNull()?.let { it > 0 } ?: false
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Due:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "₹${booking.dueAmount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (hasDue) Color.Red else Color.Black,
                            fontWeight = if (hasDue) FontWeight.Bold else FontWeight.Normal
                        )
                        if (hasDue) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Red.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "PENDING",
                                    color = Color.Red,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Expiry Message
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = urgencyLevel.messageBackgroundColor
            ) {
                Text(
                    text = when {
                        booking.daysUntilExpiry < -1 -> "⚠️ Expired ${-booking.daysUntilExpiry} days ago - Will be auto-removed in ${2 + booking.daysUntilExpiry} day(s)"
                        booking.daysUntilExpiry == -1 -> "⚠️ Expired yesterday - Will be auto-removed tomorrow"
                        booking.daysUntilExpiry == 0 -> "⏰ Expires today - Contact immediately!"
                        else -> "📅 Expires tomorrow - Contact student soon"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = urgencyLevel.textColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    valueFontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = valueFontWeight
        )
    }
}

enum class UrgencyLevel(
    val label: String,
    val backgroundColor: Color,
    val textColor: Color,
    val messageBackgroundColor: Color
) {
    EXPIRED(
        label = "EXPIRED",
        backgroundColor = Color.Red,
        textColor = Color.White,
        messageBackgroundColor = Color.Red.copy(alpha = 0.1f)
    ),
    TODAY(
        label = "TODAY",
        backgroundColor = Color(0xFFFFA500),
        textColor = Color.White,
        messageBackgroundColor = Color(0xFFFFA500).copy(alpha = 0.1f)
    ),
    TOMORROW(
        label = "TOMORROW",
        backgroundColor = Color(0xFFFFEB3B),
        textColor = Color(0xFF333333),
        messageBackgroundColor = Color(0xFFFFEB3B).copy(alpha = 0.1f)
    )
}

// Helper function to calculate days until expiry (negative if already expired)
fun calculateDaysUntilExpiry(expiryDateString: String): Int {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val expiryDate = sdf.parse(expiryDateString)
        val currentDate = Date()

        // Reset time components to compare dates only
        val calExpiry = Calendar.getInstance().apply {
            time = expiryDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val calCurrent = Calendar.getInstance().apply {
            time = currentDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffInMillis = calExpiry.timeInMillis - calCurrent.timeInMillis
        (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) {
        999 // Return high number if date parsing fails
    }
}

// Helper function to remove bookings that are 2 days past expiry
fun removeBookingsTwoDaysPastExpiry(seatData: MutableMap<String, SeatUi>) {
    seatData.forEach { (seatKey, seat) ->
        val slotsToRemove = seat.timeSlotBookings.filter { (_, booking) ->
            val daysUntilExpiry = calculateDaysUntilExpiry(booking.expiryDate)
            daysUntilExpiry < -2 // Remove if more than 2 days past expiry
        }.keys.toList()

        if (slotsToRemove.isNotEmpty()) {
            val updatedBookings = seat.timeSlotBookings.toMutableMap()
            slotsToRemove.forEach { slot ->
                updatedBookings.remove(slot)
            }
            seatData[seatKey] = seat.copy(timeSlotBookings = updatedBookings)
        }
    }
}