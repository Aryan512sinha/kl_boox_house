package eu.tutorials.kl_boox_house

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatsScreen(
    seatData: MutableMap<String, SeatUi>,
    onBackPressed: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    var selectedViewMode by remember { mutableStateOf(0) } // 0 = Available Seats, 1 = All Seats
    var selectedSeat by remember { mutableStateOf<SeatUi?>(null) }

    // Time slots
    val timeSlots = listOf(
        "6:00 AM - 10:00 AM",
        "10:00 AM - 2:00 PM",
        "2:00 PM - 6:00 PM",
        "6:00 PM - 10:00 PM"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2C2C2C),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackPressed) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Seat Management",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        // Placeholder for symmetry
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bill Number Search Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                showSearchResults = it.isNotEmpty()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Search by Bill Number",
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFFFFD700)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        showSearchResults = false
                                    }) {
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
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                cursorColor = Color(0xFFFFD700)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // View Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedViewMode = 0 },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedViewMode == 0)
                                Color(0xFFFFD700)
                            else
                                Color.White.copy(alpha = 0.2f),
                            border = BorderStroke(
                                width = 2.dp,
                                color = if (selectedViewMode == 0)
                                    Color(0xFFFFD700)
                                else
                                    Color.White.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = "Available Seats",
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                color = if (selectedViewMode == 0) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedViewMode = 1 },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedViewMode == 1)
                                Color(0xFFFFD700)
                            else
                                Color.White.copy(alpha = 0.2f),
                            border = BorderStroke(
                                width = 2.dp,
                                color = if (selectedViewMode == 1)
                                    Color(0xFFFFD700)
                                else
                                    Color.White.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = "All Seats",
                                modifier = Modifier.padding(vertical = 12.dp),
                                textAlign = TextAlign.Center,
                                color = if (selectedViewMode == 1) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Main Content
            if (showSearchResults) {
                // Search Results
                SearchResultsSection(
                    searchQuery = searchQuery,
                    seatData = seatData,
                    timeSlots = timeSlots,
                    onSeatClick = { seat ->
                        selectedSeat = seat
                    }
                )
            } else {
                // Seats Grid or List
                when (selectedViewMode) {
                    0 -> AvailableSeatsView(
                        seatData = seatData,
                        timeSlots = timeSlots,
                        onSeatClick = { seat ->
                            selectedSeat = seat
                        }
                    )
                    1 -> AllSeatsView(
                        seatData = seatData,
                        timeSlots = timeSlots,
                        onSeatClick = { seat ->
                            selectedSeat = seat
                        }
                    )
                }
            }
        }

        // Seat Detail Bottom Sheet
        selectedSeat?.let { seat ->
            SeatDetailBottomSheet(
                seat = seat,
                timeSlots = timeSlots,
                onDismiss = { selectedSeat = null }
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Search Results (${searchResults.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (searchResults.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "No bookings found with Bill No: $searchQuery",
                        modifier = Modifier.padding(24.dp),
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(count = searchResults.size) { index ->
                SearchResultCard(
                    result = searchResults[index],
                    timeSlots = timeSlots,
                    onClick = { onSeatClick(searchResults[index].seat) }
                )
            }
        }
    }
}

@Composable
fun SearchResultCard(
    result: SearchResult,
    timeSlots: List<String>,
    onClick: () -> Unit
) {
    val isExpired = isDateExpired(result.booking.expiryDate)
    val isExpiringSoon = isDateWithinDays(result.booking.expiryDate, 7)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seat ${result.seat.seatNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isExpired -> Color.Red
                        isExpiringSoon -> Color(0xFFFFA500)
                        else -> Color.Green
                    }
                ) {
                    Text(
                        text = when {
                            isExpired -> "EXPIRED"
                            isExpiringSoon -> "EXPIRING SOON"
                            else -> "ACTIVE"
                        },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Slot
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE3F2FD)
            ) {
                Text(
                    text = timeSlots[result.slotIndex],
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Student Details
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRowSeats(label = "Name:", value = result.booking.occupiedBy)
                DetailRowSeats(label = "Bill No:", value = result.booking.billNo, valueColor = Color(0xFFFFD700))
                DetailRowSeats(label = "Mobile:", value = result.booking.mobileNo)
                DetailRowSeats(label = "DOA:", value = result.booking.admissionDate)

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
                        text = result.booking.expiryDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            isExpired -> Color.Red
                            isExpiringSoon -> Color(0xFFFFA500)
                            else -> Color.Black
                        },
                        fontWeight = FontWeight.Bold
                    )
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
        seatData.values
            .filter { seat -> seat.timeSlotBookings.size < 4 }
            .sortedBy { it.seatNumber }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    color = Color(0xFFFFD700).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${availableSeats.size} seats",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (availableSeats.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "All seats are fully occupied!",
                        modifier = Modifier.padding(24.dp),
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(count = availableSeats.size) { index ->
                AvailableSeatCard(
                    seat = availableSeats[index],
                    timeSlots = timeSlots,
                    onClick = { onSeatClick(availableSeats[index]) }
                )
            }
        }
    }
}

@Composable
fun AvailableSeatCard(
    seat: SeatUi,
    timeSlots: List<String>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seat ${seat.seatNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                val availableSlots = 4 - seat.timeSlotBookings.size
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Green.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$availableSlots/4 Available",
                        color = Color.Green,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Slots Status
            Text(
                text = "Time Slots:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                timeSlots.forEachIndexed { index, timeSlot ->
                    val isOccupied = seat.timeSlotBookings.containsKey(index)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isOccupied)
                                    Color.Red.copy(alpha = 0.05f)
                                else
                                    Color.Green.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timeSlot,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Black
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isOccupied)
                                Color.Red.copy(alpha = 0.15f)
                            else
                                Color.Green.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isOccupied) "Occupied" else "Available",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isOccupied) Color.Red else Color.Green,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    color = Color(0xFFFFD700).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "${allSeats.size} total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        items(count = allSeats.size) { index ->
            AllSeatCard(
                seat = allSeats[index],
                timeSlots = timeSlots,
                onClick = { onSeatClick(allSeats[index]) }
            )
        }
    }
}

@Composable
fun AllSeatCard(
    seat: SeatUi,
    timeSlots: List<String>,
    onClick: () -> Unit
) {
    val occupiedSlots = seat.timeSlotBookings.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seat ${seat.seatNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        occupiedSlots == 4 -> Color.Red.copy(alpha = 0.15f)
                        occupiedSlots > 0 -> Color(0xFFFFA500).copy(alpha = 0.15f)
                        else -> Color.Green.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = "$occupiedSlots/4 Occupied",
                        color = when {
                            occupiedSlots == 4 -> Color.Red
                            occupiedSlots > 0 -> Color(0xFFFFA500)
                            else -> Color.Green
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Slots Grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                timeSlots.forEachIndexed { index, timeSlot ->
                    val booking = seat.timeSlotBookings[index]
                    val isOccupied = booking != null

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isOccupied)
                            Color(0xFFFFEBEE)
                        else
                            Color(0xFFE8F5E9)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = timeSlot,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                                if (isOccupied && booking != null) {
                                    Text(
                                        text = booking.occupiedBy,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isOccupied)
                                    Color.Red.copy(alpha = 0.15f)
                                else
                                    Color.Green.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isOccupied) "Occupied" else "Available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isOccupied) Color.Red else Color.Green,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
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
fun SeatDetailBottomSheet(
    seat: SeatUi,
    timeSlots: List<String>,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        LazyColumn(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seat ${seat.seatNumber} Details",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black
                        )
                    }
                }
            }

            item {
                val occupiedSlots = seat.timeSlotBookings.size
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Occupied Slots: $occupiedSlots/4",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Time Slot Details
            items(4) { slotIndex ->
                val booking = seat.timeSlotBookings[slotIndex]
                val isOccupied = booking != null

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isOccupied)
                            Color(0xFFFFEBEE)
                        else
                            Color(0xFFE8F5E9)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = timeSlots[slotIndex],
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isOccupied)
                                    Color.Red.copy(alpha = 0.15f)
                                else
                                    Color.Green.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isOccupied) "Occupied" else "Available",
                                    color = if (isOccupied) Color.Red else Color.Green,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        if (isOccupied && booking != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color.Gray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                DetailRowSeats(label = "Name:", value = booking.occupiedBy)
                                DetailRowSeats(label = "Bill No:", value = booking.billNo)
                                DetailRowSeats(label = "Mobile:", value = booking.mobileNo)
                                DetailRowSeats(label = "Subscription:", value = booking.subscriptionType)
                                DetailRowSeats(label = "DOA:", value = booking.admissionDate)

                                val isExpired = isDateExpired(booking.expiryDate)
                                val isExpiringSoon = isDateWithinDays(booking.expiryDate, 7)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Expiry:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = booking.expiryDate,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when {
                                            isExpired -> Color.Red
                                            isExpiringSoon -> Color(0xFFFFA500)
                                            else -> Color.Black
                                        },
                                        fontWeight = FontWeight.Bold
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
fun DetailRowSeats(
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
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor,
            fontWeight = FontWeight.Medium
        )
    }
}

// Data class for search results
data class SearchResult(
    val seat: SeatUi,
    val slotIndex: Int,
    val booking: TimeSlotBooking
)