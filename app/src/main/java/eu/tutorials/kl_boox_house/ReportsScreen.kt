package eu.tutorials.kl_boox_house

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    seatData: Map<String, SeatUi>,
    onBackPressed: () -> Unit
) {
    var selectedReportDate by remember { mutableStateOf(getCurrentDate()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Active Students Report",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Date Selector Card - Made more compact
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Select Date for Payment Summary",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = selectedReportDate,
                        onValueChange = { selectedReportDate = it },
                        label = { Text("Date", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("DD/MM/YYYY") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFFD700),
                            cursorColor = Color(0xFFFFD700)
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                                    contentDescription = "Select Date",
                                    tint = Color(0xFFFFD700)
                                )
                            }
                        }
                    )
                }
            }

            // Payment Summary Card - Made SMALLER
            val allBookings = seatData.values.flatMap { seat ->
                seat.timeSlotBookings.values
            }.filter { booking ->
                booking.admissionDate == selectedReportDate
            }

            val cashPayments = allBookings.count { it.paymentMode == "Cash" }
            val onlinePayments = allBookings.count { it.paymentMode == "Online" }

            val totalCashAmount = allBookings
                .filter { it.paymentMode == "Cash" }
                .sumOf {
                    val paid = it.amountPaid.toIntOrNull() ?: 0
                    val due = it.dueAmount.toIntOrNull() ?: 0
                    paid + due
                }

            val totalOnlineAmount = allBookings
                .filter { it.paymentMode == "Online" }
                .sumOf {
                    val paid = it.amountPaid.toIntOrNull() ?: 0
                    val due = it.dueAmount.toIntOrNull() ?: 0
                    paid + due
                }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Payment Summary - $selectedReportDate",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Cash Summary - Compact
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2196F3).copy(alpha = 0.2f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "💵 Cash",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "$cashPayments payments",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Black,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "₹$totalCashAmount",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2196F3),
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Online Summary - Compact
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "💳 Online",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "$onlinePayments payments",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Black,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "₹$totalOnlineAmount",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50),
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Divider(color = Color.Black.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Total: ₹${totalCashAmount + totalOnlineAmount}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // NEW: Due Students Card
            val dueStudents = getAllActiveBookings(seatData).filter { activeBooking ->
                val dueAmount = activeBooking.booking.dueAmount.toIntOrNull() ?: 0
                dueAmount > 0
            }

            if (dueStudents.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5252).copy(alpha = 0.15f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⚠️ Students with Due",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F),
                                fontSize = 14.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFD32F2F)
                            ) {
                                Text(
                                    text = "${dueStudents.size}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0xFFD32F2F).copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(8.dp))

                        // List of due students - compact view
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            dueStudents.take(5).forEach { activeBooking ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = activeBooking.booking.occupiedBy,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Bill: ${activeBooking.booking.billNo}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFD32F2F).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "₹${activeBooking.booking.dueAmount}",
                                            color = Color(0xFFD32F2F),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            if (dueStudents.size > 5) {
                                Text(
                                    text = "+ ${dueStudents.size - 5} more in list below",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Active Students List (sorted by expiry date)
            val activeStudentsCount = getAllActiveBookings(seatData).size
            Text(
                text = "Active Students ($activeStudentsCount)",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
                fontSize = 16.sp
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeStudents = getAllActiveBookings(seatData)
                        .sortedBy { parseDate(it.booking.expiryDate) }

                    if (activeStudents.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No active students",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(activeStudents.size) { index ->
                            val item = activeStudents[index]
                            StudentReportCard(
                                studentName = item.booking.occupiedBy,
                                seatNumber = item.seatNumber,
                                timeSlot = item.timeSlot,
                                billNo = item.booking.billNo,
                                mobileNo = item.booking.mobileNo,
                                admissionDate = item.booking.admissionDate,
                                expiryDate = item.booking.expiryDate,
                                amountPaid = item.booking.amountPaid,
                                dueAmount = item.booking.dueAmount,
                                paymentMode = item.booking.paymentMode,
                                subscriptionType = item.booking.subscriptionType
                            )
                        }
                    }
                }
            }
        }

        // DatePicker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                selectedReportDate = sdf.format(Date(millis))
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK", color = Color(0xFFFFD700))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = androidx.compose.material3.DatePickerDefaults.colors(
                        selectedDayContainerColor = Color(0xFFFFD700),
                        todayContentColor = Color(0xFFFFD700),
                        todayDateBorderColor = Color(0xFFFFD700)
                    )
                )
            }
        }
    }
}

// COMPACT EXPANDABLE STUDENT CARD - Perfect for 200+ students
@Composable
fun StudentReportCard(
    studentName: String,
    seatNumber: Int,
    timeSlot: Int,
    billNo: String,
    mobileNo: String,
    admissionDate: String,
    expiryDate: String,
    amountPaid: String,
    dueAmount: String,
    paymentMode: String,
    subscriptionType: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    val timeSlots = listOf(
        "6:00 AM - 10:00 AM",
        "10:00 AM - 2:00 PM",
        "2:00 PM - 6:00 PM",
        "6:00 PM - 10:00 PM"
    )

    val isExpiringSoon = isDateWithinDays(expiryDate, 7)
    val isExpired = isDateExpired(expiryDate)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isExpanded,
                onClick = { isExpanded = !isExpanded }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 8.dp else 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // COMPACT VIEW - Always visible (Name, Seat, Time Slot, Status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Name
                Text(
                    text = studentName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                // Right side: Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when {
                        isExpired -> Color.Red
                        isExpiringSoon -> Color(0xFFFFA500)
                        else -> Color.Green
                    }
                ) {
                    Text(
                        text = when {
                            isExpired -> "EXPIRED"
                            isExpiringSoon -> "EXPIRING"
                            else -> "ACTIVE"
                        },
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Seat and Time Slot Row (Always visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "Seat $seatNumber",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE3F2FD)
                ) {
                    Text(
                        text = timeSlots.getOrNull(timeSlot) ?: "Unknown",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // EXPANDED VIEW - Only visible when clicked
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Details Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Bill No
                    DetailRow(
                        label = "Bill No:",
                        value = billNo,
                        valueColor = Color.Black
                    )

                    // Mobile
                    DetailRow(
                        label = "Mobile:",
                        value = mobileNo,
                        valueColor = Color.Black
                    )

                    // Subscription Type
                    DetailRow(
                        label = "Subscription:",
                        value = subscriptionType,
                        valueColor = Color.Black
                    )

                    // DOA
                    DetailRow(
                        label = "DOA:",
                        value = admissionDate,
                        valueColor = Color.Black
                    )

                    // Expiry Date with highlighting
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Expiry:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = expiryDate,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                isExpired -> Color.Red
                                isExpiringSoon -> Color(0xFFFFA500)
                                else -> Color.Black
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Amount Paid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Amount Paid:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "₹$amountPaid",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Due Amount with indicator
                    val hasDue = dueAmount.toIntOrNull()?.let { it > 0 } ?: false
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Due:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "₹$dueAmount",
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

                    // Payment Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Payment:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (paymentMode == "Online")
                                Color(0xFFE8F5E9)
                            else
                                Color(0xFFE3F2FD)
                        ) {
                            Text(
                                text = paymentMode,
                                color = if (paymentMode == "Online")
                                    Color(0xFF2E7D32)
                                else
                                    Color(0xFF1976D2),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Normal
        )
    }
}

// Data class to hold active booking information
data class ActiveBookingInfo(
    val seatNumber: Int,
    val timeSlot: Int,
    val booking: TimeSlotBooking
)

// Helper function to get all active bookings across all seats
fun getAllActiveBookings(seatData: Map<String, SeatUi>): List<ActiveBookingInfo> {
    val activeBookings = mutableListOf<ActiveBookingInfo>()

    seatData.values.forEach { seat ->
        seat.timeSlotBookings.forEach { (timeSlotIndex, booking) ->
            activeBookings.add(
                ActiveBookingInfo(
                    seatNumber = seat.seatNumber,
                    timeSlot = timeSlotIndex,
                    booking = booking
                )
            )
        }
    }

    return activeBookings
}

// Helper function to parse date for sorting
fun parseDate(dateString: String): Long {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.parse(dateString)?.time ?: Long.MAX_VALUE
    } catch (e: Exception) {
        Long.MAX_VALUE
    }
}