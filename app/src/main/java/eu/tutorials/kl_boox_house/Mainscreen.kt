package eu.tutorials.kl_boox_house

import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNode
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    adminName: String = "Aryan",
    seatData: MutableMap<String, SeatUi>,
    onNavigateToReports: () -> Unit = {},
    onNavigateToSeats: () -> Unit = {},
    onNavigateToMessages: () -> Unit = {}
) {
    var selectedSeat by remember { mutableStateOf<SeatUi?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    var selectedNavItem by remember { mutableStateOf(0) }

    // Enhanced form fields for selected seat
    var name by remember { mutableStateOf("") }
    var billNo by remember { mutableStateOf("") }
    var mobileNo by remember { mutableStateOf("") }
    var selectedTimeSlot by remember { mutableStateOf(0) }
    var admissionDate by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var amountPaid by remember { mutableStateOf("") }
    var dueAmount by remember { mutableStateOf("0") }
    var selectedPaymentMode by remember { mutableStateOf(0) } // 0 = Cash, 1 = Online
    var selectedSubscription by remember { mutableStateOf(0) } // 0 = Monthly, 1 = Half-Monthly, 2 = 10 Days

    // Time slots
    val timeSlots = listOf(
        "6:00 AM - 10:00 AM",
        "10:00 AM - 2:00 PM",
        "2:00 PM - 6:00 PM",
        "6:00 PM - 10:00 PM"
    )

    // Payment modes
    val paymentModes = listOf("Cash", "Online")

    // Subscription options
    val subscriptionOptions = listOf(
        SubscriptionType("Monthly", 30),
        SubscriptionType("Half-Monthly", 15),
        SubscriptionType("10 Days", 10)
    )

    // Auto-remove expired bookings
    LaunchedEffect(Unit) {
        removeExpiredBookings(seatData)
    }

    // Back button handler
    BackHandler {
        if (selectedSeat != null) {
            selectedSeat = null
        }
    }

    // Reset form when seat or time slot changes
    LaunchedEffect(selectedSeat, selectedTimeSlot) {
        selectedSeat?.let { seat ->
            val booking = seat.timeSlotBookings[selectedTimeSlot]
            name = booking?.occupiedBy ?: ""
            billNo = booking?.billNo ?: ""
            mobileNo = booking?.mobileNo ?: ""
            admissionDate = booking?.admissionDate ?: getCurrentDate()
            expiryDate = booking?.expiryDate ?: ""
            amountPaid = booking?.amountPaid ?: ""
            dueAmount = booking?.dueAmount ?: "0"
            selectedPaymentMode = if (booking?.paymentMode == "Online") 1 else 0

            // Set subscription type based on existing booking
            booking?.let { b ->
                val subscriptionDays = calculateDaysBetween(b.admissionDate, b.expiryDate)
                selectedSubscription = when {
                    subscriptionDays >= 28 -> 0 // Monthly
                    subscriptionDays >= 14 -> 1 // Half-Monthly
                    else -> 2 // 10 Days
                }
            } ?: run {
                selectedSubscription = 0
            }
        }
    }

    // Auto-calculate expiry date when subscription or admission date changes
    LaunchedEffect(selectedSubscription, admissionDate) {
        if (admissionDate.isNotBlank()) {
            expiryDate = calculateExpiryDate(admissionDate, subscriptionOptions[selectedSubscription].days)
        }
    }

    val occupiedSlots = seatData.values.sumOf { it.timeSlotBookings.size }
    val totalSlots = 57 * 4
    val availableSlots = (totalSlots - occupiedSlots).coerceAtLeast(0)
    val occupancyPercent = if (totalSlots == 0) 0 else occupiedSlots * 100 / totalSlots

    val modelTransition = rememberInfiniteTransition(label = "libraryModelRotation")
    val modelRotation by modelTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "modelRotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
    ) {
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val centerNode = rememberNode(engine)

        // Camera positioning for full model visibility
        val cameraNode = rememberCameraNode(engine) {
            position = Position(x = 0f, y = 5f, z = 10f)
            lookAt(centerNode)
            centerNode.addChildNode(this)
        }

        var modelNode by remember { mutableStateOf<ModelNode?>(null) }

        LaunchedEffect(Unit) {
            modelNode = ModelNode(
                    modelInstance = modelLoader.createModelInstance("kl_boox_house6.glb"),
                    scaleToUnits = 4.8f
            ).apply {
                    position = Position(x = 0f, y = -1.0f, z = 0f)
                rotation = Rotation(x = 0f, y = 0f, z = 0f)
            }
        }

        var seatsInitialized by remember { mutableStateOf(false) }
        modelNode?.let { node ->
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 356.dp)
                    .fillMaxWidth(0.92f)
                    .height(330.dp)
                    .clip(RoundedCornerShape(26.dp)),
                shape = RoundedCornerShape(26.dp),
                color = Color(0xFF1A263A),
                shadowElevation = 16.dp,
                border = BorderStroke(1.dp, Color(0xFFCDA45D).copy(alpha = 0.22f))
            ) {
                Scene(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    engine = engine,
                    modelLoader = modelLoader,
                    cameraNode = cameraNode,
                    cameraManipulator = rememberCameraManipulator(
                        orbitHomePosition = Position(x = 0f, y = 5f, z = 10f),
                        targetPosition = Position(x = 0f, y = -0.5f, z = 0f)
                    ),
                    childNodes = listOf(centerNode, node),
                    onTouchEvent = { motionEvent, hitResult ->
                    if (motionEvent.action == MotionEvent.ACTION_UP) {
                        val hitNode = hitResult?.node as? ModelNode
                        val nodeName = hitNode?.name
                        if (nodeName?.startsWith("seat_") == true) {
                            val seat = seatData[nodeName]
                            if (seat != null) {
                                selectedSeat = seat
                                selectedTimeSlot = 0
                                node.highlightSeat(seat.seatNumber)
                            }
                        }
                    }
                    true
                },
                    onFrame = { frameTimeNanos ->
                        centerNode.rotation = Rotation(y = modelRotation)
                        cameraNode.lookAt(centerNode)
                        if (!seatsInitialized) {
                            node.findAndNameSeats()
                            seatsInitialized = true
                        }
                    }
                )
            }
        }

        // Enhanced Logo and Search Bar with Profile
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .fillMaxWidth(0.92f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Section with Welcome Message
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Welcome Text
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Good to see you, $adminName",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "LIBRARY OVERVIEW",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCDA45D),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Profile Photo
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = Color(0xFFCDA45D),
                    shadowElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = adminName.firstOrNull()?.uppercase() ?: "A",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF1A1A1A),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.11f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
                        Text("$availableSlots", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("AVAILABLE", color = Color(0xFFCDA45D), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.11f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
                        Text("$occupiedSlots", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("OCCUPIED", color = Color(0xFFF39A9A), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFCDA45D),
                    shadowElevation = 6.dp
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
                        Text("$occupancyPercent%", color = Color(0xFF111827), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("UTILIZATION", color = Color(0xFF5E4A2B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Enhanced Search Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 8.dp
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
                            "Search Seat (e.g., 1, 2, 3...)",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFFCDA45D)
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFFCDA45D)
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
            }

            // Search Results Dropdown
            if (showSearchResults) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    LazyColumn {
                        val filteredSeats = seatData.filter {
                            val seatNum = it.value.seatNumber.toString()
                            val query = searchQuery.trim()
                            seatNum == query || seatNum.startsWith(query) || it.key == "seat_$query"
                        }

                        if (filteredSeats.isEmpty()) {
                            item {
                                Text(
                                    text = "No seats found",
                                    modifier = Modifier.padding(16.dp),
                                    color = Color.Gray
                                )
                            }
                        } else {
                            items(filteredSeats.size) { index ->
                                val entry = filteredSeats.entries.elementAt(index)
                                val seat = entry.value

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = false,
                                            onClick = {
                                                selectedSeat = seat
                                                selectedTimeSlot = 0
                                                searchQuery = ""
                                                showSearchResults = false
                                                modelNode?.highlightSeat(seat.seatNumber)
                                            }
                                        )
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Seat ${seat.seatNumber}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )

                                    val occupiedSlots = seat.timeSlotBookings.size
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (occupiedSlots == 4)
                                            Color.Red.copy(alpha = 0.1f)
                                        else
                                            Color.Green.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "$occupiedSlots/4 occupied",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (occupiedSlots == 4) Color.Red else Color.Green,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                if (index < filteredSeats.size - 1) {
                                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Enhanced Seat Info Form
        selectedSeat?.let { seat ->
            val currentBooking = seat.timeSlotBookings[selectedTimeSlot]
            val isCurrentSlotOccupied = currentBooking != null

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(0.95f)
                    .heightIn(max = 650.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Header with close button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Seat ${seat.seatNumber}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = {
                                    selectedSeat = null
                                    modelNode?.resetAllSeats()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.Black
                                )
                            }
                        }
                    }

                    item {
                        // Show overall seat status
                        val occupiedSlots = seat.timeSlotBookings.size
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFCDA45D).copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Occupied Slots: $occupiedSlots/4",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    item {
                        // Time Slot Selector
                        Text(
                            text = "Select Time Slot:",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold
                        )

                        Column {
                            timeSlots.forEachIndexed { index, timeSlot ->
                                val isSlotOccupied = seat.timeSlotBookings.containsKey(index)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = selectedTimeSlot == index,
                                            onClick = { selectedTimeSlot = index }
                                        )
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedTimeSlot == index,
                                        onClick = { selectedTimeSlot = index }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = timeSlot,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSlotOccupied)
                                            Color.Red.copy(alpha = 0.1f)
                                        else
                                            Color.Green.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = if (isSlotOccupied) "Occupied" else "Available",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSlotOccupied) Color.Red else Color.Green,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!isCurrentSlotOccupied) {
                        item {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Student Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = billNo,
                                onValueChange = { billNo = it },
                                label = { Text("Bill Number") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = mobileNo,
                                onValueChange = {
                                    if (it.length <= 10) mobileNo = it
                                },
                                label = { Text("Mobile Number") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                placeholder = { Text("Enter 10-digit mobile number") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            // Subscription Type Selector
                            Text(
                                text = "Subscription Type:",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                subscriptionOptions.forEachIndexed { index, subscription ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .selectable(
                                                selected = selectedSubscription == index,
                                                onClick = { selectedSubscription = index }
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selectedSubscription == index)
                                            Color(0xFFCDA45D).copy(alpha = 0.2f)
                                        else
                                            Color.LightGray.copy(alpha = 0.1f),
                                        border = BorderStroke(
                                            width = 2.dp,
                                            color = if (selectedSubscription == index)
                                                Color(0xFFCDA45D)
                                            else
                                                Color.LightGray.copy(alpha = 0.3f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = selectedSubscription == index,
                                                onClick = { selectedSubscription = index }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${subscription.name} (${subscription.days} days)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Black,
                                                fontWeight = if (selectedSubscription == index)
                                                    FontWeight.Bold
                                                else
                                                    FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            // Date fields
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = admissionDate,
                                    onValueChange = { admissionDate = it },
                                    label = { Text("DOA") },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("DD/MM/YYYY") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = expiryDate,
                                    onValueChange = { },
                                    label = { Text("Expiry Date") },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("Auto-calculated") },
                                    singleLine = true,
                                    enabled = false,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = TextFieldDefaults.colors(
                                        disabledTextColor = Color.Black,
                                        disabledContainerColor = Color.White,
                                        disabledIndicatorColor = Color.Gray,
                                        disabledLabelColor = Color.Gray
                                    )
                                )
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = amountPaid,
                                onValueChange = { amountPaid = it },
                                label = { Text("Amount Paid (₹)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                placeholder = { Text("Enter amount paid") },
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Text(
                                        text = "₹",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = dueAmount,
                                onValueChange = { dueAmount = it },
                                label = { Text("Due Amount (₹)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                placeholder = { Text("Enter due amount") },
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = {
                                    Text(
                                        text = "₹",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            )
                        }

                        item {
                            // Payment Mode Selector
                            Text(
                                text = "Payment Mode:",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                paymentModes.forEachIndexed { index, mode ->
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .selectable(
                                                selected = selectedPaymentMode == index,
                                                onClick = { selectedPaymentMode = index }
                                            ),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selectedPaymentMode == index)
                                            Color(0xFFCDA45D).copy(alpha = 0.2f)
                                        else
                                            Color.LightGray.copy(alpha = 0.1f),
                                        border = BorderStroke(
                                            width = 2.dp,
                                            color = if (selectedPaymentMode == index)
                                                Color(0xFFCDA45D)
                                            else
                                                Color.LightGray.copy(alpha = 0.3f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            RadioButton(
                                                selected = selectedPaymentMode == index,
                                                onClick = { selectedPaymentMode = index }
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = mode,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Black,
                                                fontWeight = if (selectedPaymentMode == index)
                                                    FontWeight.Bold
                                                else
                                                    FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val newBooking = TimeSlotBooking(
                                            occupiedBy = name,
                                            billNo = billNo,
                                            mobileNo = mobileNo,
                                            admissionDate = admissionDate,
                                            expiryDate = expiryDate,
                                            occupiedSince = System.currentTimeMillis(),
                                            amountPaid = amountPaid,
                                            dueAmount = dueAmount,
                                            paymentMode = paymentModes[selectedPaymentMode],
                                            subscriptionType = subscriptionOptions[selectedSubscription].name
                                        )

                                        // 🔹 1. UPDATE LOCAL UI (keep this)
                                        val updatedBookings = seat.timeSlotBookings.toMutableMap()
                                        updatedBookings[selectedTimeSlot] = newBooking
                                        val updatedSeat = seat.copy(timeSlotBookings = updatedBookings)

                                        selectedSeat = updatedSeat
                                        seatData["seat_${seat.seatNumber}"] = updatedSeat

                                        // 🔥 2. SAVE TO FIREBASE (THIS WAS MISSING)
                                        FirestoreRepository.saveBooking(
                                            seatNumber = seat.seatNumber,
                                            slotIndex = selectedTimeSlot,
                                            booking = newBooking
                                        )
                                    },


                                    modifier = Modifier.weight(1f),
                                    enabled = name.isNotBlank() && billNo.isNotBlank() &&
                                            mobileNo.isNotBlank() && mobileNo.length == 10 &&
                                            admissionDate.isNotBlank() && expiryDate.isNotBlank() &&
                                            amountPaid.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Book Slot", color = Color.White)
                                }

                                OutlinedButton(
                                    onClick = {
                                        name = ""
                                        billNo = ""
                                        mobileNo = ""
                                        admissionDate = getCurrentDate()
                                        expiryDate = ""
                                        amountPaid = ""
                                        dueAmount = "0"
                                        selectedPaymentMode = 0
                                        selectedSubscription = 0
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Clear")
                                }
                            }
                        }
                    }

                    // HOMOGENEOUS CURRENT BOOKING CARD - Updated to match ReportsScreen
                    if (isCurrentSlotOccupied && currentBooking != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    // Define expiry status variables
                                    val isExpired = isDateExpired(currentBooking.expiryDate)
                                    val isExpiringSoon = isDateWithinDays(currentBooking.expiryDate, 7)

                                    // Header Row with "Current Booking" and Status Badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Current Booking",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold
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

                                    // Student Name (Larger, more prominent)
                                    Text(
                                        text = currentBooking.occupiedBy,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Details Section - HOMOGENEOUS with ReportsScreen
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Bill No
                                        DetailRowMain(
                                            label = "Bill No:",
                                            value = currentBooking.billNo,
                                            valueColor = Color.Black
                                        )

                                        // Mobile
                                        DetailRowMain(
                                            label = "Mobile:",
                                            value = currentBooking.mobileNo,
                                            valueColor = Color.Black
                                        )

                                        // Subscription Type
                                        DetailRowMain(
                                            label = "Subscription:",
                                            value = currentBooking.subscriptionType,
                                            valueColor = Color.Black
                                        )

                                        // DOA
                                        DetailRowMain(
                                            label = "DOA:",
                                            value = currentBooking.admissionDate,
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
                                                text = currentBooking.expiryDate,
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
                                                text = "₹${currentBooking.amountPaid}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color(0xFF4CAF50),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Due Amount with indicator
                                        val hasDue = currentBooking.dueAmount.toIntOrNull()?.let { it > 0 } ?: false
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
                                                    text = "₹${currentBooking.dueAmount}",
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
                                                color = if (currentBooking.paymentMode == "Online")
                                                    Color(0xFFE8F5E9)
                                                else
                                                    Color(0xFFE3F2FD)
                                            ) {
                                                Text(
                                                    text = currentBooking.paymentMode,
                                                    color = if (currentBooking.paymentMode == "Online")
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
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp)
                .fillMaxWidth(0.86f),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF111827).copy(alpha = 0.92f),
            shadowElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "FULL BUILDING",
                        color = Color(0xFFCDA45D),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "57 seats • 4 time shifts",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF43C59E).copy(alpha = 0.16f)
                ) {
                    Text(
                        "LIVE",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = Color(0xFF69D8B3),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

    }
}

// Helper Composable for MainScreen Detail Rows
@Composable
fun DetailRowMain(
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
