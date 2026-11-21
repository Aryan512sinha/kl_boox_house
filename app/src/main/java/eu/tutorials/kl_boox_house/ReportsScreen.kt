package eu.tutorials.kl_boox_house

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// NEW: Consolidated Booking Info for Reports (similar to MainScreen)
data class ConsolidatedReportBooking(
    val booking: TimeSlotBooking,
    val timeSlots: List<Pair<Int, String>>,
    val seatNumber: Int
)

// NEW: Helper function to consolidate bookings by bill number
fun consolidateBookingsByBillNo(
    seatData: Map<String, SeatUi>,
    timeSlots: List<String>
): List<ConsolidatedReportBooking> {
    val allBookings = mutableMapOf<String, MutableList<Triple<Int, Int, TimeSlotBooking>>>()

    // Collect all bookings grouped by bill number
    seatData.values.forEach { seat ->
        seat.timeSlotBookings.forEach { (slotIndex, booking) ->
            val key = booking.billNo
            if (!allBookings.containsKey(key)) {
                allBookings[key] = mutableListOf()
            }
            allBookings[key]!!.add(Triple(seat.seatNumber, slotIndex, booking))
        }
    }

    // Create consolidated bookings
    return allBookings.map { (_, bookingList) ->
        val firstBooking = bookingList.first()
        val seatNumber = firstBooking.first
        val booking = firstBooking.third
        val slots = bookingList.map { Pair(it.second, timeSlots[it.second]) }.sortedBy { it.first }

        ConsolidatedReportBooking(
            booking = booking,
            timeSlots = slots,
            seatNumber = seatNumber
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    seatData: Map<String, SeatUi>,
    onBackPressed: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSeats: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {}
) {
    var selectedReportDate by remember { mutableStateOf(getCurrentDate()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var selectedNavItem by remember { mutableStateOf(3) }

    val timeSlots = listOf(
        "6:00 AM - 10:00 AM",
        "10:00 AM - 2:00 PM",
        "2:00 PM - 6:00 PM",
        "6:00 PM - 10:00 PM"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.LightBackground,
                        Color(0xFFE8E8E8)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Enhanced Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 12.dp
            ) {
                Box(
                    modifier = Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(
                                AppColors.DarkBackground,
                                AppColors.DarkSecondary
                            )
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(Spacing.xl)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AppColors.GoldPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("📊", fontSize = 28.sp)
                                }
                            }

                            Column {
                                Text(
                                    text = "Active Students Report",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = "Financial & booking overview",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.large))

                        // Enhanced Date Selector
                        EnhancedDateSelector(
                            selectedDate = selectedReportDate,
                            onDateChange = { selectedReportDate = it },
                            onShowPicker = { showDatePicker = true }
                        )
                    }
                }
            }

            // Content
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.large),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
                contentPadding = PaddingValues(top = Spacing.large, bottom = 80.dp)
            ) {
                // Payment Summary Card - FIXED: Only count amount paid, not due
                item {
                    EnhancedPaymentSummaryCardFixed(
                        seatData = seatData,
                        selectedDate = selectedReportDate
                    )
                }

                // Due Students Section - FIXED: Consolidated and expandable
                item {
                    val consolidatedBookings = consolidateBookingsByBillNo(seatData, timeSlots)
                    val dueStudents = consolidatedBookings.filter {
                        (it.booking.dueAmount.toIntOrNull() ?: 0) > 0
                    }

                    if (dueStudents.isNotEmpty()) {
                        EnhancedDueStudentsCardFixed(dueStudents = dueStudents)
                    }
                }

                // Active Students Section - FIXED: Consolidated bookings
                item {
                    val consolidatedBookings = consolidateBookingsByBillNo(seatData, timeSlots)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Students",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.GoldPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${consolidatedBookings.size}",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.xs)
                            )
                        }
                    }
                }

                val consolidatedBookings = consolidateBookingsByBillNo(seatData, timeSlots)
                    .sortedBy { parseDate(it.booking.expiryDate) }

                if (consolidatedBookings.isEmpty()) {
                    item {
                        EnhancedEmptyState(
                            icon = "📚",
                            title = "No Active Students",
                            message = "There are no active bookings\nat the moment."
                        )
                    }
                } else {
                    items(consolidatedBookings.size) { index ->
                        EnhancedStudentReportCardFixed(
                            consolidatedBooking = consolidatedBookings[index],
                            timeSlots = timeSlots
                        )
                    }
                }
            }
        }

        // Enhanced Bottom Navigation
        EnhancedBottomNavigation(
            selectedItem = selectedNavItem,
            onItemSelected = { index ->
                selectedNavItem = index
                when (index) {
                    0 -> onNavigateToHome()
                    1 -> onNavigateToSeats()
                    2 -> onNavigateToMessages()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // DatePicker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            selectedReportDate = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }) {
                        Text("OK", color = AppColors.GoldPrimary, fontWeight = FontWeight.Bold)
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
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = AppColors.GoldPrimary,
                        todayContentColor = AppColors.GoldPrimary,
                        todayDateBorderColor = AppColors.GoldPrimary
                    )
                )
            }
        }
    }
}

@Composable
fun EnhancedDateSelector(
    selectedDate: String,
    onDateChange: (String) -> Unit,
    onShowPicker: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.15f),
        shadowElevation = 4.dp
    ) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = onDateChange,
            label = {
                Text(
                    "Date for Payment Summary",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("DD/MM/YYYY", color = Color.White.copy(alpha = 0.5f)) },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                focusedIndicatorColor = AppColors.GoldPrimary,
                unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f),
                cursorColor = AppColors.GoldPrimary,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            trailingIcon = {
                IconButton(onClick = onShowPicker) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_my_calendar),
                        contentDescription = "Select Date",
                        tint = AppColors.GoldPrimary
                    )
                }
            }
        )
    }
}

// FIXED: Payment Summary - Only counts amount paid, not due amount, and counts each bill number only once
@Composable
fun EnhancedPaymentSummaryCardFixed(
    seatData: Map<String, SeatUi>,
    selectedDate: String
) {
    // FIXED: Group by bill number to avoid counting the same payment multiple times
    val uniqueBookingsByBillNo = seatData.values.flatMap { seat ->
        seat.timeSlotBookings.values
    }.filter { it.admissionDate == selectedDate }
        .distinctBy { it.billNo } // Only count each bill number once!

    val cashPayments = uniqueBookingsByBillNo.count { it.paymentMode == "Cash" }
    val onlinePayments = uniqueBookingsByBillNo.count { it.paymentMode == "Online" }

    // FIXED: Only sum amountPaid, not including dueAmount, and only once per bill number
    val totalCashAmount = uniqueBookingsByBillNo.filter { it.paymentMode == "Cash" }
        .sumOf { it.amountPaid.toIntOrNull() ?: 0 }

    val totalOnlineAmount = uniqueBookingsByBillNo.filter { it.paymentMode == "Online" }
        .sumOf { it.amountPaid.toIntOrNull() ?: 0 }

    val totalAmount = totalCashAmount + totalOnlineAmount

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(
                        AppColors.GoldPrimary.copy(alpha = 0.9f),
                        AppColors.GoldDark.copy(alpha = 0.9f)
                    )
                )
            )
        ) {
            Column(modifier = Modifier.padding(Spacing.xl)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💰", fontSize = 24.sp)
                    Text(
                        text = "Payment Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Text(
                    text = selectedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(Spacing.xl))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    PaymentModeCard(
                        icon = "💵",
                        label = "Cash",
                        count = cashPayments,
                        amount = totalCashAmount,
                        color = AppColors.Info,
                        modifier = Modifier.weight(1f)
                    )

                    PaymentModeCard(
                        icon = "💳",
                        label = "Online",
                        count = onlinePayments,
                        amount = totalOnlineAmount,
                        color = AppColors.Success,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.large))
                Divider(color = Color.Black.copy(alpha = 0.2f), thickness = 2.dp)
                Spacer(modifier = Modifier.height(Spacing.large))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total Revenue",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "₹$totalAmount",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.1f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${cashPayments + onlinePayments}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentModeCard(
    icon: String,
    label: String,
    count: Int,
    amount: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(Spacing.small))
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
            Text(
                text = "$count payments",
                fontSize = 12.sp,
                color = Color.Black.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(Spacing.small))
            Text(
                text = "₹$amount",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 20.sp
            )
        }
    }
}

// FIXED: Due Students Card - Now expandable and consolidated
@Composable
fun EnhancedDueStudentsCardFixed(dueStudents: List<ConsolidatedReportBooking>) {
    var isExpanded by remember { mutableStateOf(false) }
    val totalDue = dueStudents.sumOf { it.booking.dueAmount.toIntOrNull() ?: 0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppColors.Error.copy(alpha = 0.1f),
                        Color.White
                    )
                )
            )
        ) {
            Column(modifier = Modifier.padding(Spacing.xl)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 24.sp)
                        Column {
                            Text(
                                text = "Students with Due",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Error
                            )
                            Text(
                                text = "Total: ₹$totalDue",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = AppColors.Error,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${dueStudents.size}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.large))
                Divider(color = AppColors.Error.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(Spacing.large))

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                    // Show first 5 students
                    dueStudents.take(5).forEach { consolidatedBooking ->
                        DueStudentItemFixed(consolidatedBooking)
                    }

                    // FIXED: Clickable expand button if more than 5 students
                    if (dueStudents.size > 5) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded },
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.Error.copy(alpha = 0.05f)
                        ) {
                            Row(
                                modifier = Modifier.padding(Spacing.medium),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isExpanded)
                                        "Show less"
                                    else
                                        "+ ${dueStudents.size - 5} more students with due",
                                    fontSize = 13.sp,
                                    color = AppColors.Error,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(Spacing.xs))
                                Text(
                                    text = if (isExpanded) "▲" else "▼",
                                    color = AppColors.Error,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Show remaining students when expanded
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                            dueStudents.drop(5).forEach { consolidatedBooking ->
                                DueStudentItemFixed(consolidatedBooking)
                            }
                        }
                    }
                }
            }
        }
    }
}

// FIXED: Due Student Item - Shows consolidated booking
@Composable
fun DueStudentItemFixed(consolidatedBooking: ConsolidatedReportBooking) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF5F5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    consolidatedBooking.booking.occupiedBy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Bill:",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        consolidatedBooking.booking.billNo,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text("•", fontSize = 12.sp, color = Color.Gray)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AppColors.Info.copy(alpha = 0.1f)
                    ) {
                        Text(
                            "${consolidatedBooking.timeSlots.size} slot${if (consolidatedBooking.timeSlots.size > 1) "s" else ""}",
                            fontSize = 10.sp,
                            color = AppColors.Info,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Spacing.xs, vertical = 2.dp)
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = AppColors.Error.copy(alpha = 0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Text("₹", fontWeight = FontWeight.Bold, color = AppColors.Error)
                    Text(
                        text = consolidatedBooking.booking.dueAmount,
                        color = AppColors.Error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// FIXED: Student Report Card - Shows consolidated booking with multiple time slots
@Composable
fun EnhancedStudentReportCardFixed(
    consolidatedBooking: ConsolidatedReportBooking,
    timeSlots: List<String>
) {
    var isExpanded by remember { mutableStateOf(false) }

    val isExpiringSoon = isDateWithinDays(consolidatedBooking.booking.expiryDate, 7)
    val isExpired = isDateExpired(consolidatedBooking.booking.expiryDate)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { isExpanded = !isExpanded }
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 8.dp else 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFAFA),
                        Color.White
                    )
                )
            )
        ) {
            Column(modifier = Modifier.padding(Spacing.large)) {
                // Header - Always Visible
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isExpired -> AppColors.Error.copy(alpha = 0.2f)
                                isExpiringSoon -> AppColors.Warning.copy(alpha = 0.2f)
                                else -> AppColors.Success.copy(alpha = 0.2f)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = consolidatedBooking.seatNumber.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = when {
                                        isExpired -> AppColors.Error
                                        isExpiringSoon -> AppColors.Warning
                                        else -> AppColors.Success
                                    }
                                )
                            }
                        }

                        Column {
                            Text(
                                text = consolidatedBooking.booking.occupiedBy,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                "Seat ${consolidatedBooking.seatNumber}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isExpired -> AppColors.Error
                            isExpiringSoon -> AppColors.Warning
                            else -> AppColors.Success
                        }
                    ) {
                        Text(
                            when {
                                isExpired -> "EXPIRED"
                                isExpiringSoon -> "EXPIRING"
                                else -> "ACTIVE"
                            },
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.xs)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.medium))

                // Time Slots Display - Compact
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    consolidatedBooking.timeSlots.take(2).forEach { (slotIndex, _) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.Info.copy(alpha = 0.1f)
                        ) {
                            Text(
                                timeSlots[slotIndex],
                                color = AppColors.Info,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.xs)
                            )
                        }
                    }

                    if (consolidatedBooking.timeSlots.size > 2) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.GoldPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "+${consolidatedBooking.timeSlots.size - 2}",
                                color = AppColors.GoldDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.xs)
                            )
                        }
                    }
                }

                // Expanded Content
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(Spacing.large))

                        // Show all time slots when expanded
                        if (consolidatedBooking.timeSlots.size > 2) {
                            Text(
                                text = "All Time Slots:",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                consolidatedBooking.timeSlots.forEach { (slotIndex, _) ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = AppColors.Info.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            timeSlots[slotIndex],
                                            color = AppColors.Info,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(Spacing.medium))
                        }

                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(Spacing.large))

                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                            DetailRowReports("Bill No:", consolidatedBooking.booking.billNo)
                            DetailRowReports("Mobile:", consolidatedBooking.booking.mobileNo)
                            DetailRowReports("Subscription:", consolidatedBooking.booking.subscriptionType)
                            DetailRowReports("DOA:", consolidatedBooking.booking.admissionDate)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Expiry:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                Text(
                                    consolidatedBooking.booking.expiryDate,
                                    color = when {
                                        isExpired -> AppColors.Error
                                        isExpiringSoon -> AppColors.Warning
                                        else -> Color.Black
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Divider(color = Color.LightGray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(Spacing.xs))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Amount Paid:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                Text("₹${consolidatedBooking.booking.amountPaid}", color = AppColors.Success, fontWeight = FontWeight.Bold)
                            }

                            val hasDue = (consolidatedBooking.booking.dueAmount.toIntOrNull() ?: 0) > 0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Due:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                    Text(
                                        "₹${consolidatedBooking.booking.dueAmount}",
                                        color = if (hasDue) AppColors.Error else Color.Black,
                                        fontWeight = if (hasDue) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (hasDue) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = AppColors.Error.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                "PENDING",
                                                color = AppColors.Error,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = Spacing.xs, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Payment:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (consolidatedBooking.booking.paymentMode == "Online")
                                        Color(0xFFE8F5E9) else Color(0xFFE3F2FD)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.xs),
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                                    ) {
                                        Text(
                                            if (consolidatedBooking.booking.paymentMode == "Online") "💳" else "💵",
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            consolidatedBooking.booking.paymentMode,
                                            color = if (consolidatedBooking.booking.paymentMode == "Online")
                                                Color(0xFF2E7D32) else Color(0xFF1976D2),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRowReports(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Normal, color = Color.Black)
    }
}

fun parseDate(dateString: String): Long {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.parse(dateString)?.time ?: Long.MAX_VALUE
    } catch (e: Exception) {
        Long.MAX_VALUE
    }
}