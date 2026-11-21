package eu.tutorials.kl_boox_house

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatsScreen(
    seatData: MutableMap<String, SeatUi>,
    onBackPressed: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {},
    onNavigateToReports: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    var selectedViewMode by remember { mutableStateOf(0) }
    var selectedSeat by remember { mutableStateOf<SeatUi?>(null) }
    var selectedNavItem by remember { mutableStateOf(1) }

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
                    Column(modifier = Modifier.padding(Spacing.large)) {
                        // Header Title
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Seat Management",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                )
                                Text(
                                    text = "${seatData.size} total seats",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.large))

                        // Enhanced Search Bar
                        EnhancedSearchBarSeats(
                            searchQuery = searchQuery,
                            onQueryChange = {
                                searchQuery = it
                                showSearchResults = it.isNotEmpty()
                            },
                            onClear = {
                                searchQuery = ""
                                showSearchResults = false
                            }
                        )

                        Spacer(modifier = Modifier.height(Spacing.large))

                        // Enhanced View Mode Toggle
                        EnhancedViewModeToggle(
                            selectedMode = selectedViewMode,
                            onModeChange = { selectedViewMode = it }
                        )
                    }
                }
            }

            // Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 0.dp)
            ) {
                when {
                    showSearchResults -> {
                        SearchResultsSection(
                            searchQuery = searchQuery,
                            seatData = seatData,
                            timeSlots = timeSlots,
                            onSeatClick = { seat -> selectedSeat = seat }
                        )
                    }
                    selectedViewMode == 0 -> {
                        AvailableSeatsView(
                            seatData = seatData,
                            timeSlots = timeSlots,
                            onSeatClick = { seat -> selectedSeat = seat }
                        )
                    }
                    else -> {
                        AllSeatsView(
                            seatData = seatData,
                            timeSlots = timeSlots,
                            onSeatClick = { seat -> selectedSeat = seat }
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
                    2 -> onNavigateToMessages()
                    3 -> onNavigateToReports()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Enhanced Seat Detail Dialog (Full Screen)
        selectedSeat?.let { seat ->
            EnhancedSeatDetailDialog(
                seat = seat,
                timeSlots = timeSlots,
                onDismiss = { selectedSeat = null }
            )
        }
    }
}

@Composable
fun EnhancedSearchBarSeats(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsPressedAsState()

    val elevation by animateDpAsState(
        targetValue = if (isFocused) 12.dp else 6.dp,
        label = "elevation"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = elevation
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.White,
                        AppColors.GoldLight.copy(alpha = 0.1f)
                    )
                )
            )
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Search by Bill Number",
                        color = Color.Gray.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AppColors.GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(onClick = onClear) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    cursorColor = AppColors.GoldPrimary
                ),
                shape = RoundedCornerShape(32.dp),
                interactionSource = interactionSource
            )
        }
    }
}

@Composable
fun EnhancedViewModeToggle(
    selectedMode: Int,
    onModeChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        EnhancedToggleButton(
            text = "Available Seats",
            icon = "✓",
            isSelected = selectedMode == 0,
            onClick = { onModeChange(0) },
            modifier = Modifier.weight(1f)
        )

        EnhancedToggleButton(
            text = "All Seats",
            icon = "📋",
            isSelected = selectedMode == 1,
            onClick = { onModeChange(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun EnhancedToggleButton(
    text: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.GoldPrimary else Color.White.copy(alpha = 0.2f),
        label = "background"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected) AppColors.GoldPrimary else Color.White.copy(alpha = 0.3f)
        ),
        shadowElevation = if (isSelected) 8.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = Spacing.medium),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(
                text = text,
                textAlign = TextAlign.Center,
                color = if (isSelected) Color.Black else Color.White,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun SearchResultsSection(
    searchQuery: String,
    seatData: MutableMap<String, SeatUi>,
    timeSlots: List<String>,
    onSeatClick: (SeatUi) -> Unit
) {
    val searchResults = remember(searchQuery, seatData) {
        seatData.values.flatMap { seat ->
            seat.timeSlotBookings.entries.mapNotNull { (slotIndex, booking) ->
                if (booking.billNo.contains(searchQuery, ignoreCase = true)) {
                    SearchResult(seat, slotIndex, booking)
                } else null
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Search Results",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.GoldPrimary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${searchResults.size} found",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.xs)
                    )
                }
            }
        }

        if (searchResults.isEmpty()) {
            item {
                EnhancedEmptyState(
                    icon = "🔍",
                    title = "No Results Found",
                    message = "No bookings found with\nBill No: $searchQuery"
                )
            }
        } else {
            items(searchResults) { result ->
                EnhancedSearchResultCard(result, timeSlots) { onSeatClick(result.seat) }
            }
        }
    }
}

@Composable
fun EnhancedSearchResultCard(
    result: SearchResult,
    timeSlots: List<String>,
    onClick: () -> Unit
) {
    val isExpired = isDateExpired(result.booking.expiryDate)
    val isExpiringSoon = isDateWithinDays(result.booking.expiryDate, 7)

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
                indication = rememberRipple(color = AppColors.GoldPrimary),
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AppColors.GoldPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = result.seat.seatNumber.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = AppColors.GoldPrimary
                                )
                            }
                        }

                        Text(
                            text = "Seat ${result.seat.seatNumber}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
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
                            text = when {
                                isExpired -> "EXPIRED"
                                isExpiringSoon -> "EXPIRING"
                                else -> "ACTIVE"
                            },
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.xs)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.medium))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.Info.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = timeSlots[result.slotIndex],
                        color = AppColors.Info,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.medium))
                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(Spacing.medium))

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    DetailRowEnhanced("Name:", result.booking.occupiedBy)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bill No:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.GoldPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                result.booking.billNo,
                                color = AppColors.GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = Spacing.small, vertical = 2.dp)
                            )
                        }
                    }
                    DetailRowEnhanced("Mobile:", result.booking.mobileNo)
                }
            }
        }
    }
}

@Composable
fun AvailableSeatsView(
    seatData: MutableMap<String, SeatUi>,
    timeSlots: List<String>,
    onSeatClick: (SeatUi) -> Unit
) {
    val availableSeats = remember(seatData) {
        seatData.values.filter { it.timeSlotBookings.size < 4 }.sortedBy { it.seatNumber }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Available Seats",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.Success.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${availableSeats.size} seats",
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Success,
                        modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.xs)
                    )
                }
            }
        }

        if (availableSeats.isEmpty()) {
            item {
                EnhancedEmptyState(
                    icon = "🔒",
                    title = "All Seats Full",
                    message = "All seats are currently occupied.\nCheck back later for availability."
                )
            }
        } else {
            items(availableSeats.size) { index ->
                AvailableSeatCard(availableSeats[index], timeSlots) {
                    onSeatClick(availableSeats[index])
                }
            }
        }
    }
}

@Composable
fun AvailableSeatCard(seat: SeatUi, timeSlots: List<String>, onClick: () -> Unit) {
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
                indication = rememberRipple(color = AppColors.GoldPrimary),
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AppColors.GoldPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = seat.seatNumber.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = AppColors.GoldPrimary
                                )
                            }
                        }

                        Text(
                            text = "Seat ${seat.seatNumber}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    val availableSlots = 4 - seat.timeSlotBookings.size
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.Success.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✓", fontSize = 14.sp, color = AppColors.Success)
                            Text(
                                text = "$availableSlots/4 Available",
                                color = AppColors.Success,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    timeSlots.forEachIndexed { index, slot ->
                        val isOccupied = seat.timeSlotBookings.containsKey(index)
                        EnhancedTimeSlotIndicator(
                            timeSlot = slot,
                            isOccupied = isOccupied
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedTimeSlotIndicator(
    timeSlot: String,
    isOccupied: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isOccupied)
            AppColors.Error.copy(alpha = 0.05f)
        else
            AppColors.Success.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOccupied) "🔴" else "🟢",
                    fontSize = 12.sp
                )
                Text(
                    text = timeSlot,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isOccupied)
                    AppColors.Error.copy(alpha = 0.15f)
                else
                    AppColors.Success.copy(alpha = 0.15f)
            ) {
                Text(
                    if (isOccupied) "Occupied" else "Available",
                    color = if (isOccupied) AppColors.Error else AppColors.Success,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.xs)
                )
            }
        }
    }
}

@Composable
fun AllSeatsView(
    seatData: MutableMap<String, SeatUi>,
    timeSlots: List<String>,
    onSeatClick: (SeatUi) -> Unit
) {
    val allSeats = remember(seatData) {
        seatData.values.sortedBy { it.seatNumber }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Seats",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.GoldPrimary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${allSeats.size} total",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.xs)
                    )
                }
            }
        }

        items(allSeats.size) { index ->
            AllSeatCard(allSeats[index], timeSlots) { onSeatClick(allSeats[index]) }
        }
    }
}

@Composable
fun AllSeatCard(seat: SeatUi, timeSlots: List<String>, onClick: () -> Unit) {
    val occupiedSlots = seat.timeSlotBookings.size
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
                indication = rememberRipple(color = AppColors.GoldPrimary),
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                occupiedSlots == 4 -> AppColors.Error.copy(alpha = 0.2f)
                                occupiedSlots > 0 -> AppColors.Warning.copy(alpha = 0.2f)
                                else -> AppColors.Success.copy(alpha = 0.2f)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = seat.seatNumber.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = when {
                                        occupiedSlots == 4 -> AppColors.Error
                                        occupiedSlots > 0 -> AppColors.Warning
                                        else -> AppColors.Success
                                    }
                                )
                            }
                        }

                        Text(
                            text = "Seat ${seat.seatNumber}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            occupiedSlots == 4 -> AppColors.Error.copy(alpha = 0.15f)
                            occupiedSlots > 0 -> AppColors.Warning.copy(alpha = 0.15f)
                            else -> AppColors.Success.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = "$occupiedSlots/4 Occupied",
                            color = when {
                                occupiedSlots == 4 -> AppColors.Error
                                occupiedSlots > 0 -> AppColors.Warning
                                else -> AppColors.Success
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    timeSlots.forEachIndexed { index, slot ->
                        val booking = seat.timeSlotBookings[index]
                        val isOccupied = booking != null

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isOccupied) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                        ) {
                            Row(
                                modifier = Modifier.padding(Spacing.medium),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isOccupied) "🔴" else "🟢",
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = slot,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    if (isOccupied && booking != null) {
                                        Spacer(modifier = Modifier.height(Spacing.xs))
                                        Text(
                                            text = booking.occupiedBy,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isOccupied)
                                        AppColors.Error.copy(alpha = 0.15f)
                                    else
                                        AppColors.Success.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        if (isOccupied) "Occupied" else "Available",
                                        color = if (isOccupied) AppColors.Error else AppColors.Success,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.xs)
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

@Composable
fun EnhancedSeatDetailDialog(
    seat: SeatUi,
    timeSlots: List<String>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.85f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
                shape = RoundedCornerShape(28.dp)
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
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Header
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = AppColors.DarkBackground,
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.xl),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AppColors.GoldPrimary,
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = seat.seatNumber.toString(),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                color = Color.Black
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = "Seat ${seat.seatNumber} Details",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        val occupiedSlots = seat.timeSlotBookings.size
                                        Text(
                                            text = "$occupiedSlots/4 slots occupied",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                EnhancedCloseButton(onClick = onDismiss)
                            }
                        }

                        // Content
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Spacing.xl),
                            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                        ) {
                            items(4) { slotIndex ->
                                val booking = seat.timeSlotBookings[slotIndex]
                                val isOccupied = booking != null

                                EnhancedSlotDetailCard(
                                    timeSlot = timeSlots[slotIndex],
                                    booking = booking,
                                    isOccupied = isOccupied
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedSlotDetailCard(
    timeSlot: String,
    booking: TimeSlotBooking?,
    isOccupied: Boolean
) {
    val isExpired = booking?.let { isDateExpired(it.expiryDate) } ?: false
    val isExpiringSoon = booking?.let { isDateWithinDays(it.expiryDate, 7) } ?: false

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOccupied) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            // Time Slot Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOccupied) "⏰" else "✓",
                        fontSize = 18.sp
                    )
                    Text(
                        text = timeSlot,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                if (isOccupied && booking != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isExpired -> AppColors.Error
                            isExpiringSoon -> AppColors.Warning
                            else -> AppColors.Success
                        }
                    ) {
                        Text(
                            text = when {
                                isExpired -> "EXPIRED"
                                isExpiringSoon -> "EXPIRING"
                                else -> "ACTIVE"
                            },
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.xs)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AppColors.Success.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Available",
                            color = AppColors.Success,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.xs)
                        )
                    }
                }
            }

            if (isOccupied && booking != null) {
                Spacer(modifier = Modifier.height(Spacing.medium))
                Divider(color = Color.Gray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(Spacing.medium))

                // Student Name - Prominent
                Text(
                    text = booking.occupiedBy,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(Spacing.medium))

                // Details Grid
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        InfoChip(
                            icon = "🧾",
                            label = "Bill No",
                            value = booking.billNo,
                            modifier = Modifier.weight(1f)
                        )
                        InfoChip(
                            icon = "📱",
                            label = "Mobile",
                            value = booking.mobileNo,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    InfoChip(
                        icon = "📅",
                        label = "Subscription",
                        value = booking.subscriptionType,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        InfoChip(
                            icon = "📆",
                            label = "DOA",
                            value = booking.admissionDate,
                            modifier = Modifier.weight(1f)
                        )
                        InfoChip(
                            icon = "⏰",
                            label = "Expiry",
                            value = booking.expiryDate,
                            modifier = Modifier.weight(1f),
                            valueColor = when {
                                isExpired -> AppColors.Error
                                isExpiringSoon -> AppColors.Warning
                                else -> Color.Black
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Divider(color = Color.Gray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(Spacing.xs))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.Success.copy(alpha = 0.1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.medium),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "💰 Paid",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "₹${booking.amountPaid}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.Success
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = if ((booking.dueAmount.toIntOrNull() ?: 0) > 0)
                                AppColors.Error.copy(alpha = 0.1f)
                            else
                                Color(0xFFF5F5F5)
                        ) {
                            Column(
                                modifier = Modifier.padding(Spacing.medium),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "📌 Due",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "₹${booking.dueAmount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if ((booking.dueAmount.toIntOrNull() ?: 0) > 0)
                                        AppColors.Error
                                    else
                                        Color.Gray
                                )
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = if (booking.paymentMode == "Online")
                            Color(0xFFE8F5E9)
                        else
                            Color(0xFFE3F2FD)
                    ) {
                        Row(
                            modifier = Modifier.padding(Spacing.medium),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (booking.paymentMode == "Online") "💳" else "💵",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(Spacing.small))
                            Text(
                                text = "Payment: ${booking.paymentMode}",
                                fontWeight = FontWeight.Bold,
                                color = if (booking.paymentMode == "Online")
                                    Color(0xFF2E7D32)
                                else
                                    Color(0xFF1976D2)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Black
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = icon, fontSize = 12.sp)
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun EnhancedEmptyState(
    icon: String,
    title: String,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xxl * 2),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = AppColors.GoldPrimary.copy(alpha = 0.1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = icon,
                        fontSize = 36.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

data class SearchResult(val seat: SeatUi, val slotIndex: Int, val booking: TimeSlotBooking)