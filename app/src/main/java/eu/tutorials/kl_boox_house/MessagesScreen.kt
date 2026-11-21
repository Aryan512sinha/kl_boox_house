package eu.tutorials.kl_boox_house

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

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
    onBackPressed: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSeats: () -> Unit = {},
    onNavigateToReports: () -> Unit = {}
) {
    var selectedNavItem by remember { mutableStateOf(2) }

    val timeSlots = listOf(
        "6:00 AM - 10:00 AM",
        "10:00 AM - 2:00 PM",
        "2:00 PM - 6:00 PM",
        "6:00 PM - 10:00 PM"
    )

    // Auto-remove bookings 2 days past expiry (now also deletes from Firebase)
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60000)
            removeBookingsTwoDaysPastExpiry(seatData)
        }
    }

    val expiringBookings by remember {
        derivedStateOf {
            val bookings = mutableListOf<ExpiringBooking>()
            seatData.forEach { (_, seat) ->
                seat.timeSlotBookings.forEach { (slotIndex, booking) ->
                    val daysUntilExpiry = calculateDaysUntilExpiry(booking.expiryDate)
                    if (daysUntilExpiry <= 1 && daysUntilExpiry >= -2) {
                        bookings.add(
                            ExpiringBooking(
                                seatNumber = seat.seatNumber,
                                timeSlot = timeSlots.getOrNull(slotIndex) ?: "Unknown",
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
            bookings.sortedBy { it.daysUntilExpiry }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AppColors.LightBackground, Color(0xFFE8E8E8))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent,
                shadowElevation = 12.dp
            ) {
                Box(
                    modifier = Modifier.background(
                        Brush.verticalGradient(
                            colors = listOf(AppColors.DarkBackground, AppColors.DarkSecondary)
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.xl),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AppColors.GoldPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = "Messages",
                                    tint = AppColors.GoldPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Expiry Notifications",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (expiringBookings.isNotEmpty()) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AppColors.Error,
                                        modifier = Modifier.size(8.dp)
                                    ) {}
                                }
                                Text(
                                    text = "${expiringBookings.size} ${if (expiringBookings.size == 1) "booking" else "bookings"} expiring soon",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (expiringBookings.isEmpty()) {
                    EnhancedEmptyStateMessages()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Spacing.large),
                        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item { EnhancedStatsCard(expiringBookings) }
                        items(expiringBookings) { booking ->
                            EnhancedExpiringBookingCard(booking = booking)
                        }
                    }
                }
            }
        }

        EnhancedBottomNavigation(
            selectedItem = selectedNavItem,
            onItemSelected = { index ->
                selectedNavItem = index
                when (index) {
                    0 -> onNavigateToHome()
                    1 -> onNavigateToSeats()
                    3 -> onNavigateToReports()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun EnhancedStatsCard(bookings: List<ExpiringBooking>) {
    val expiredCount = bookings.count { it.daysUntilExpiry < 0 }
    val expiringTodayCount = bookings.count { it.daysUntilExpiry == 0 }
    val expiringTomorrowCount = bookings.count { it.daysUntilExpiry == 1 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(
                    colors = listOf(AppColors.GoldLight.copy(alpha = 0.3f), Color.White)
                )
            )
        ) {
            Column(modifier = Modifier.padding(Spacing.xl)) {
                Text(
                    text = "📊 Quick Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(Spacing.large))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    StatChip(expiredCount, "Expired", AppColors.Error, Modifier.weight(1f))
                    StatChip(expiringTodayCount, "Today", AppColors.Warning, Modifier.weight(1f))
                    StatChip(expiringTomorrowCount, "Tomorrow", AppColors.Info, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun StatChip(count: Int, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = color.copy(alpha = 0.1f)) {
        Column(
            modifier = Modifier.padding(Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = color, fontSize = 11.sp)
        }
    }
}

@Composable
fun EnhancedExpiringBookingCard(booking: ExpiringBooking) {
    val isExpired = booking.daysUntilExpiry < 0
    val urgencyLevel = when {
        booking.daysUntilExpiry <= -1 -> UrgencyLevel.EXPIRED
        booking.daysUntilExpiry == 0 -> UrgencyLevel.TODAY
        else -> UrgencyLevel.TOMORROW
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth().scale(scale),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(colors = listOf(Color(0xFFFAFAFA), Color.White))
            )
        ) {
            Column(modifier = Modifier.padding(Spacing.large)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = booking.studentName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(Spacing.small))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AppColors.Info.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.xs),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                                ) {
                                    Text("🪑", fontSize = 12.sp)
                                    Text(
                                        text = "Seat ${booking.seatNumber}",
                                        color = AppColors.Info,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Text("•", color = Color.Gray, fontSize = 12.sp)
                            Text(
                                text = booking.timeSlot,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = urgencyLevel.backgroundColor,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
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

                Spacer(modifier = Modifier.height(Spacing.large))
                Divider(color = Color.LightGray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(Spacing.large))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = AppColors.Success.copy(alpha = 0.05f)
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.large),
                        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        Text(
                            text = "📞 Contact Information",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = AppColors.Success.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        Icons.Default.Call,
                                        contentDescription = "Phone",
                                        tint = AppColors.Success,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Mobile Number", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 11.sp)
                                Text(booking.mobileNo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bill No:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            Surface(shape = RoundedCornerShape(8.dp), color = AppColors.GoldPrimary.copy(alpha = 0.15f)) {
                                Text(
                                    booking.billNo,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.GoldPrimary,
                                    modifier = Modifier.padding(horizontal = Spacing.small, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.medium))

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    DetailRowMessages("Subscription:", booking.subscriptionType)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Expiry Date:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text(
                            booking.expiryDate,
                            color = if (isExpired) AppColors.Error else AppColors.Warning,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Amount Paid:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Text("₹${booking.amountPaid}", color = AppColors.Success, fontWeight = FontWeight.Bold)
                    }
                    val hasDue = booking.dueAmount.toIntOrNull()?.let { it > 0 } ?: false
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Due:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                            Text(
                                "₹${booking.dueAmount}",
                                color = if (hasDue) AppColors.Error else Color.Black,
                                fontWeight = if (hasDue) FontWeight.Bold else FontWeight.Normal
                            )
                            if (hasDue) {
                                Surface(shape = RoundedCornerShape(8.dp), color = AppColors.Error.copy(alpha = 0.15f)) {
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
                }

                Spacer(modifier = Modifier.height(Spacing.medium))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = urgencyLevel.messageBackgroundColor
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.large),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = urgencyLevel.icon, fontSize = 24.sp)
                        Text(
                            text = when {
                                booking.daysUntilExpiry < -1 -> "Expired ${-booking.daysUntilExpiry} days ago"
                                booking.daysUntilExpiry == -1 -> "Expired yesterday"
                                booking.daysUntilExpiry == 0 -> "Expires today - Contact immediately!"
                                else -> "Expires tomorrow - Contact soon"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = urgencyLevel.textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedEmptyStateMessages() {
    Box(
        modifier = Modifier.fillMaxSize().padding(Spacing.xxl * 2),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Surface(
                modifier = Modifier.size(120.dp).scale(scale),
                shape = CircleShape,
                color = AppColors.Success.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "No Messages",
                        tint = AppColors.Success,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xxl))
            Text("All Clear! 🎉", style = MaterialTheme.typography.headlineMedium, color = Color.Black, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(Spacing.medium))
            Text(
                text = "No subscriptions expiring soon.\nYou'll be notified when bookings\nare about to expire.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun DetailRowMessages(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Normal, color = Color.Black)
    }
}

enum class UrgencyLevel(
    val label: String,
    val backgroundColor: Color,
    val textColor: Color,
    val messageBackgroundColor: Color,
    val icon: String
) {
    EXPIRED("EXPIRED", Color(0xFFFF5252), Color.White, Color(0xFFFF5252).copy(alpha = 0.1f), "⚠️"),
    TODAY("TODAY", Color(0xFFFFA500), Color.White, Color(0xFFFFA500).copy(alpha = 0.1f), "⏰"),
    TOMORROW("TOMORROW", Color(0xFF2196F3), Color.White, Color(0xFF2196F3).copy(alpha = 0.1f), "📅")
}

fun calculateDaysUntilExpiry(expiryDateString: String): Int {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val expiryDate = sdf.parse(expiryDateString)
        val currentDate = Date()

        val calExpiry = Calendar.getInstance().apply {
            time = expiryDate
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val calCurrent = Calendar.getInstance().apply {
            time = currentDate
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }

        val diffInMillis = calExpiry.timeInMillis - calCurrent.timeInMillis
        (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) {
        999
    }
}

// FIXED: Now also deletes from Firebase, not just local state
fun removeBookingsTwoDaysPastExpiry(seatData: MutableMap<String, SeatUi>) {
    seatData.toList().forEach { (seatKey, seat) ->
        val slotsToRemove = seat.timeSlotBookings.filter { (_, booking) ->
            calculateDaysUntilExpiry(booking.expiryDate) < -2
        }.keys.toList()

        if (slotsToRemove.isNotEmpty()) {
            val updatedBookings = seat.timeSlotBookings.toMutableMap()
            slotsToRemove.forEach { slot ->
                updatedBookings.remove(slot)
                // Now also deletes from Firebase
                FirestoreRepository.deleteBooking(
                    seatNumber = seat.seatNumber,
                    slotIndex = slot,
                    onSuccess = {
                        android.util.Log.d("MESSAGES_DELETE", "Deleted seat ${seat.seatNumber}, slot $slot")
                    },
                    onFailure = { e ->
                        android.util.Log.e("MESSAGES_DELETE", "Failed to delete seat ${seat.seatNumber}, slot $slot", e)
                    }
                )
            }
            seatData[seatKey] = seat.copy(timeSlotBookings = updatedBookings)
        }
    }
}