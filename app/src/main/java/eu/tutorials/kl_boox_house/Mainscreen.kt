package eu.tutorials.kl_boox_house

import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Theme Colors
object AppColors {
    val GoldPrimary = Color(0xFFFFD700)
    val GoldLight = Color(0xFFFFF4CC)
    val GoldDark = Color(0xFFFFA000)
    val DarkBackground = Color(0xFF2C2C2C)
    val DarkSecondary = Color(0xFF1C1C1C)
    val LightBackground = Color(0xFFF5F5F5)
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFFA500)
    val Error = Color(0xFFFF5252)
    val Info = Color(0xFF2196F3)
}

// Spacing System
object Spacing {
    val xs = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
}

// NEW: Data class for consolidated bookings
data class ConsolidatedBooking(
    val booking: TimeSlotBooking,
    val timeSlots: List<Pair<Int, String>>,  // List of (slotIndex, timeSlotString)
    val seatNumber: Int
)

// NEW: Helper function to group bookings by student
fun groupBookingsByStudent(
    seat: SeatUi,
    timeSlots: List<String>
): List<ConsolidatedBooking> {
    val groupedBookings = mutableMapOf<String, MutableList<Pair<Int, String>>>()

    // Group time slots by student (using mobile number as unique identifier)
    seat.timeSlotBookings.forEach { (slotIndex, booking) ->
        val key = "${booking.occupiedBy}_${booking.mobileNo}_${booking.billNo}"
        if (!groupedBookings.containsKey(key)) {
            groupedBookings[key] = mutableListOf()
        }
        groupedBookings[key]!!.add(Pair(slotIndex, timeSlots[slotIndex]))
    }

    // Create consolidated bookings
    return groupedBookings.map { (key, slots) ->
        val firstSlot = slots.first().first
        val booking = seat.timeSlotBookings[firstSlot]!!
        ConsolidatedBooking(
            booking = booking,
            timeSlots = slots.sortedBy { it.first },
            seatNumber = seat.seatNumber
        )
    }
}

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

    // Form fields
    var name by remember { mutableStateOf("") }
    var billNo by remember { mutableStateOf("") }
    var mobileNo by remember { mutableStateOf("") }
    var admissionDate by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var amountPaid by remember { mutableStateOf("") }
    var dueAmount by remember { mutableStateOf("0") }
    var selectedPaymentMode by remember { mutableStateOf(0) }
    var selectedSubscription by remember { mutableStateOf(0) }
    var selectedTimeSlots by remember { mutableStateOf(setOf<Int>()) }

    val timeSlots = listOf(
        "6:00 AM - 10:00 AM",
        "10:00 AM - 2:00 PM",
        "2:00 PM - 6:00 PM",
        "6:00 PM - 10:00 PM"
    )

    val paymentModes = listOf("Cash", "Online")

    val subscriptionOptions = listOf(
        SubscriptionType("Monthly", 30),
        SubscriptionType("Half-Monthly", 15),
        SubscriptionType("10 Days", 10)
    )

    // Animation state
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Auto-remove expired bookings
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60000)
            removeExpiredBookings(seatData)
        }
    }

    BackHandler {
        if (selectedSeat != null) {
            selectedSeat = null
        }
    }

    // Reset form when seat changes
    LaunchedEffect(selectedSeat) {
        selectedSeat?.let {
            name = ""
            billNo = ""
            mobileNo = ""
            admissionDate = getCurrentDate()
            expiryDate = ""
            amountPaid = ""
            dueAmount = "0"
            selectedPaymentMode = 0
            selectedSubscription = 0
            selectedTimeSlots = setOf()
        }
    }

    // Auto-calculate expiry date
    LaunchedEffect(selectedSubscription, admissionDate) {
        if (admissionDate.isNotBlank()) {
            expiryDate = calculateExpiryDate(admissionDate, subscriptionOptions[selectedSubscription].days)
        }
    }

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
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val environmentLoader = rememberEnvironmentLoader(engine)
        val centerNode = rememberNode(engine)

        val cameraNode = rememberCameraNode(engine) {
            position = Position(x = 0f, y = 10f, z = 8f)
            lookAt(centerNode)
            centerNode.addChildNode(this)
        }

        var modelNode by remember { mutableStateOf<ModelNode?>(null) }

        LaunchedEffect(Unit) {
            modelNode = ModelNode(
                modelInstance = modelLoader.createModelInstance("kl_boox_house6.glb"),
                scaleToUnits = 6.5f
            ).apply {
                position = Position(x = 0f, y = 0f, z = 0f)
                rotation = Rotation(x = 0f, y = -45f, z = 0f)
            }
        }

        var seatsInitialized by remember { mutableStateOf(false) }
        var environmentReady by remember { mutableStateOf(false) }

        modelNode?.let { node ->
            Scene(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
                    .align(Alignment.Center)
                    .padding(top = 40.dp),
                engine = engine,
                modelLoader = modelLoader,
                cameraNode = cameraNode,
                cameraManipulator = rememberCameraManipulator(
                    orbitHomePosition = Position(x = 0f, y = 10f, z = 8f),
                    targetPosition = Position(x = 0f, y = 0f, z = 0f)
                ),
                childNodes = listOf(centerNode, node),
                environment = environmentLoader.createHDREnvironment("brown_photostudio_02_4k.hdr")!!,
                onTouchEvent = { motionEvent, hitResult ->
                    if (motionEvent.action == MotionEvent.ACTION_UP) {
                        val hitNode = hitResult?.node as? ModelNode
                        val nodeName = hitNode?.name
                        if (nodeName?.startsWith("seat_") == true) {
                            val seat = seatData[nodeName]
                            if (seat != null) {
                                selectedSeat = seat
                                node.highlightSeat(seat.seatNumber)
                            }
                        }
                    }
                    true
                },
                onFrame = { frameTimeNanos ->
                    if (!seatsInitialized) {
                        node.findAndNameSeats()
                        seatsInitialized = true
                    }

                    if (!environmentReady) {
                        try {
                            val sceneClass = this.javaClass
                            val viewField = sceneClass.getDeclaredField("view")
                            viewField.isAccessible = true
                            val view = viewField.get(this)

                            val viewClass = view.javaClass
                            val sceneMethod = viewClass.getMethod("getScene")
                            val scene = sceneMethod.invoke(view)

                            val sceneObjClass = scene.javaClass
                            val setSkyboxMethod = sceneObjClass.getMethod("setSkybox", com.google.android.filament.Skybox::class.java)
                            setSkyboxMethod.invoke(scene, null)

                            environmentReady = true
                        } catch (e: Exception) {
                        }
                    }
                }
            )
        }

        // Enhanced Header Section with Animation
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(600)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .padding(top = Spacing.large)
                    .fillMaxWidth(0.92f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Enhanced Profile Section with Glassmorphism
                GlassmorphicProfileCard(adminName)

                Spacer(modifier = Modifier.height(Spacing.medium))

                // Enhanced Search Bar with Animation
                EnhancedSearchBar(
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

                // Search Results with Animation
                AnimatedVisibility(
                    visible = showSearchResults,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(Spacing.small))
                        EnhancedSearchResults(
                            searchQuery = searchQuery,
                            seatData = seatData,
                            onSeatSelect = { seat ->
                                selectedSeat = seat
                                searchQuery = ""
                                showSearchResults = false
                                modelNode?.highlightSeat(seat.seatNumber)
                            }
                        )
                    }
                }
            }
        }

        // Enhanced Seat Info Form with Animation
        AnimatedVisibility(
            visible = selectedSeat != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedSeat?.let { seat ->
                EnhancedSeatInfoForm(
                    seat = seat,
                    timeSlots = timeSlots,
                    selectedTimeSlots = selectedTimeSlots,
                    onTimeSlotsChange = { selectedTimeSlots = it },
                    name = name,
                    onNameChange = { name = it },
                    billNo = billNo,
                    onBillNoChange = { billNo = it },
                    mobileNo = mobileNo,
                    onMobileNoChange = { if (it.length <= 10) mobileNo = it },
                    admissionDate = admissionDate,
                    onAdmissionDateChange = { admissionDate = it },
                    expiryDate = expiryDate,
                    amountPaid = amountPaid,
                    onAmountPaidChange = { amountPaid = it },
                    dueAmount = dueAmount,
                    onDueAmountChange = { dueAmount = it },
                    selectedPaymentMode = selectedPaymentMode,
                    onPaymentModeChange = { selectedPaymentMode = it },
                    selectedSubscription = selectedSubscription,
                    onSubscriptionChange = { selectedSubscription = it },
                    paymentModes = paymentModes,
                    subscriptionOptions = subscriptionOptions,
                    onDismiss = {
                        selectedSeat = null
                        modelNode?.resetAllSeats()
                    },
                    onBook = {
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

                        selectedTimeSlots.forEach { slotIndex ->
                            FirestoreRepository.saveBooking(
                                seatNumber = seat.seatNumber,
                                slotIndex = slotIndex,
                                booking = newBooking,
                                onSuccess = {
                                    // Let the real-time listener update the UI
                                },
                                onFailure = { e ->
                                    android.util.Log.e("BOOKING_ERROR", "Failed to save booking", e)
                                }
                            )
                        }

                        // Reset form after booking
                        name = ""
                        billNo = ""
                        mobileNo = ""
                        admissionDate = getCurrentDate()
                        expiryDate = ""
                        amountPaid = ""
                        dueAmount = "0"
                        selectedPaymentMode = 0
                        selectedSubscription = 0
                        selectedTimeSlots = setOf()
                    },
                    onClear = {
                        name = ""
                        billNo = ""
                        mobileNo = ""
                        admissionDate = getCurrentDate()
                        expiryDate = ""
                        amountPaid = ""
                        dueAmount = "0"
                        selectedPaymentMode = 0
                        selectedSubscription = 0
                        selectedTimeSlots = setOf()
                    },
                    seatData = seatData
                )
            }
        }

        // Enhanced Bottom Navigation
        EnhancedBottomNavigation(
            selectedItem = selectedNavItem,
            onItemSelected = { index ->
                selectedNavItem = index
                when (index) {
                    1 -> onNavigateToSeats()
                    2 -> onNavigateToMessages()
                    3 -> onNavigateToReports()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun GlassmorphicProfileCard(adminName: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            AppColors.DarkBackground,
                            AppColors.DarkSecondary
                        )
                    )
                )
                .padding(Spacing.large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo with subtle animation
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = AppColors.DarkSecondary,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        AppColors.GoldPrimary.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.library4),
                            contentDescription = "Library Logo",
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.medium))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hi $adminName 👋",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Welcome to the Library",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                // Animated profile badge
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = Color.Transparent,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        AppColors.GoldPrimary,
                                        AppColors.GoldDark
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        AppColors.GoldLight.copy(alpha = shimmerAlpha),
                                        AppColors.GoldPrimary
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = adminName.firstOrNull()?.uppercase() ?: "A",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AppColors.DarkBackground,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedSearchBar(
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
                        "Search Seat (e.g., 1, 2, 3...)",
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
fun EnhancedSearchResults(
    searchQuery: String,
    seatData: MutableMap<String, SeatUi>,
    onSeatSelect: (SeatUi) -> Unit
) {
    val filteredSeats = remember(searchQuery, seatData) {
        seatData.filter {
            val seatNum = it.value.seatNumber.toString()
            val query = searchQuery.trim()
            seatNum == query || seatNum.startsWith(query) || it.key == "seat_$query"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 200.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        LazyColumn {
            if (filteredSeats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.xxl),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No seats found",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                items(filteredSeats.size) { index ->
                    val entry = filteredSeats.entries.elementAt(index)
                    val seat = entry.value

                    EnhancedSearchResultItem(
                        seat = seat,
                        onClick = { onSeatSelect(seat) }
                    )

                    if (index < filteredSeats.size - 1) {
                        Divider(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            modifier = Modifier.padding(horizontal = Spacing.large)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedSearchResultItem(seat: SeatUi, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = AppColors.GoldPrimary),
                onClick = onClick
            )
            .padding(Spacing.large),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Seat ${seat.seatNumber}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        val occupiedSlots = seat.timeSlotBookings.size
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (occupiedSlots == 4)
                AppColors.Error.copy(alpha = 0.15f)
            else
                AppColors.Success.copy(alpha = 0.15f)
        ) {
            Text(
                text = "$occupiedSlots/4 occupied",
                style = MaterialTheme.typography.bodySmall,
                color = if (occupiedSlots == 4) AppColors.Error else AppColors.Success,
                modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.xs),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EnhancedSeatInfoForm(
    seat: SeatUi,
    timeSlots: List<String>,
    selectedTimeSlots: Set<Int>,
    onTimeSlotsChange: (Set<Int>) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    billNo: String,
    onBillNoChange: (String) -> Unit,
    mobileNo: String,
    onMobileNoChange: (String) -> Unit,
    admissionDate: String,
    onAdmissionDateChange: (String) -> Unit,
    expiryDate: String,
    amountPaid: String,
    onAmountPaidChange: (String) -> Unit,
    dueAmount: String,
    onDueAmountChange: (String) -> Unit,
    selectedPaymentMode: Int,
    onPaymentModeChange: (Int) -> Unit,
    selectedSubscription: Int,
    onSubscriptionChange: (Int) -> Unit,
    paymentModes: List<String>,
    subscriptionOptions: List<SubscriptionType>,
    onDismiss: () -> Unit,
    onBook: () -> Unit,
    onClear: () -> Unit,
    seatData: MutableMap<String, SeatUi>
) {
    Card(
        modifier = Modifier
            .padding(bottom = 80.dp, start = Spacing.large, end = Spacing.large)
            .fillMaxWidth(0.95f)
            .heightIn(max = 650.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        LazyColumn(
            modifier = Modifier.padding(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Seat ${seat.seatNumber}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                        val occupiedSlots = seat.timeSlotBookings.size
                        Text(
                            text = "$occupiedSlots/4 slots occupied",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    EnhancedCloseButton(onClick = onDismiss)
                }
            }

            // Time Slots Section
            item {
                Text(
                    text = "Select Time Slot(s)",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(Spacing.small))

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    timeSlots.forEachIndexed { index, timeSlot ->
                        EnhancedTimeSlotItem(
                            timeSlot = timeSlot,
                            index = index,
                            isSelected = selectedTimeSlots.contains(index),
                            isOccupied = seat.timeSlotBookings.containsKey(index),
                            onToggle = {
                                onTimeSlotsChange(
                                    if (selectedTimeSlots.contains(index)) {
                                        selectedTimeSlots - index
                                    } else {
                                        selectedTimeSlots + index
                                    }
                                )
                            }
                        )
                    }
                }

                AnimatedVisibility(visible = selectedTimeSlots.isNotEmpty()) {
                    Column {
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = "✓ ${selectedTimeSlots.size} slot(s) selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Form fields for new booking
            if (selectedTimeSlots.isNotEmpty()) {
                item {
                    EnhancedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        label = "Student Name",
                        icon = "👤"
                    )
                }

                item {
                    EnhancedTextField(
                        value = billNo,
                        onValueChange = onBillNoChange,
                        label = "Bill Number",
                        icon = "🧾",
                        keyboardType = KeyboardType.Number
                    )
                }

                item {
                    EnhancedTextField(
                        value = mobileNo,
                        onValueChange = onMobileNoChange,
                        label = "Mobile Number",
                        icon = "📱",
                        keyboardType = KeyboardType.Phone,
                        placeholder = "10-digit mobile number"
                    )
                }

                item {
                    EnhancedSubscriptionSelector(
                        subscriptionOptions = subscriptionOptions,
                        selectedIndex = selectedSubscription,
                        onSelect = onSubscriptionChange
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        EnhancedTextField(
                            value = admissionDate,
                            onValueChange = onAdmissionDateChange,
                            label = "Admission Date",
                            icon = "📅",
                            modifier = Modifier.weight(1f),
                            placeholder = "DD/MM/YYYY"
                        )

                        EnhancedTextField(
                            value = expiryDate,
                            onValueChange = {},
                            label = "Expiry Date",
                            icon = "⏰",
                            modifier = Modifier.weight(1f),
                            enabled = false
                        )
                    }
                }

                item {
                    EnhancedTextField(
                        value = amountPaid,
                        onValueChange = onAmountPaidChange,
                        label = "Amount Paid",
                        icon = "₹",
                        keyboardType = KeyboardType.Number
                    )
                }

                item {
                    EnhancedTextField(
                        value = dueAmount,
                        onValueChange = onDueAmountChange,
                        label = "Due Amount",
                        icon = "₹",
                        keyboardType = KeyboardType.Number
                    )
                }

                item {
                    EnhancedPaymentModeSelector(
                        paymentModes = paymentModes,
                        selectedIndex = selectedPaymentMode,
                        onSelect = onPaymentModeChange
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                    ) {
                        EnhancedButton(
                            text = "Book ${selectedTimeSlots.size} Slot(s)",
                            onClick = onBook,
                            modifier = Modifier.weight(1f),
                            enabled = name.isNotBlank() && billNo.isNotBlank() &&
                                    mobileNo.isNotBlank() && mobileNo.length == 10 &&
                                    admissionDate.isNotBlank() && expiryDate.isNotBlank() &&
                                    amountPaid.isNotBlank(),
                            isPrimary = true
                        )

                        EnhancedButton(
                            text = "Clear",
                            onClick = onClear,
                            modifier = Modifier.weight(1f),
                            isPrimary = false
                        )
                    }
                }
            }

            // MODIFIED: Show consolidated bookings (grouped by student)
            if (selectedTimeSlots.isEmpty()) {
                val consolidatedBookings = groupBookingsByStudent(seat, timeSlots)

                consolidatedBookings.forEach { consolidatedBooking ->
                    item {
                        ConsolidatedBookingCard(
                            consolidatedBooking = consolidatedBooking,
                            seat = seat,
                            seatData = seatData,
                            paymentModes = paymentModes
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedTimeSlotItem(
    timeSlot: String,
    index: Int,
    isSelected: Boolean,
    isOccupied: Boolean,
    onToggle: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> AppColors.GoldPrimary.copy(alpha = 0.2f)
            isOccupied -> AppColors.Error.copy(alpha = 0.05f)
            else -> AppColors.Success.copy(alpha = 0.05f)
        },
        label = "background"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = if (isSelected) BorderStroke(2.dp, AppColors.GoldPrimary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { if (!isOccupied) onToggle() },
                enabled = !isOccupied,
                colors = CheckboxDefaults.colors(
                    checkedColor = AppColors.GoldPrimary,
                    checkmarkColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.width(Spacing.small))

            Text(
                text = timeSlot,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                modifier = Modifier.weight(1f),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isOccupied)
                    AppColors.Error.copy(alpha = 0.15f)
                else
                    AppColors.Success.copy(alpha = 0.15f)
            ) {
                Text(
                    text = if (isOccupied) "Occupied" else "Available",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOccupied) AppColors.Error else AppColors.Success,
                    modifier = Modifier.padding(horizontal = Spacing.small, vertical = Spacing.xs),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun EnhancedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = "",
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
        },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = AppColors.GoldLight.copy(alpha = 0.1f),
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color(0xFFF5F5F5),
            focusedIndicatorColor = AppColors.GoldPrimary,
            unfocusedIndicatorColor = Color.LightGray.copy(alpha = 0.5f),
            cursorColor = AppColors.GoldPrimary,
            disabledTextColor = Color.Black
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
fun EnhancedSubscriptionSelector(
    subscriptionOptions: List<SubscriptionType>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Subscription Type",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Spacing.small))

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            subscriptionOptions.forEachIndexed { index, subscription ->
                EnhancedRadioOption(
                    text = "${subscription.name} (${subscription.days} days)",
                    isSelected = selectedIndex == index,
                    onClick = { onSelect(index) }
                )
            }
        }
    }
}

@Composable
fun EnhancedPaymentModeSelector(
    paymentModes: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Payment Mode",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(Spacing.small))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            paymentModes.forEachIndexed { index, mode ->
                EnhancedRadioChip(
                    text = mode,
                    icon = if (mode == "Cash") "💵" else "💳",
                    isSelected = selectedIndex == index,
                    onClick = { onSelect(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun EnhancedRadioOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            AppColors.GoldPrimary.copy(alpha = 0.2f)
        else
            Color.LightGray.copy(alpha = 0.1f),
        label = "background"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected)
                AppColors.GoldPrimary
            else
                Color.LightGray.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = AppColors.GoldPrimary
                )
            )
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun EnhancedRadioChip(
    text: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            AppColors.GoldPrimary.copy(alpha = 0.2f)
        else
            Color.LightGray.copy(alpha = 0.1f),
        label = "background"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = AppColors.GoldPrimary),
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(
            width = 2.dp,
            color = if (isSelected)
                AppColors.GoldPrimary
            else
                Color.LightGray.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(Spacing.xs))
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = AppColors.GoldPrimary
                )
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun EnhancedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    if (isPrimary) {
        Button(
            onClick = onClick,
            modifier = modifier.scale(scale),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                disabledContainerColor = Color.LightGray
            ),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(0.dp),
            interactionSource = interactionSource
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (enabled) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AppColors.Success,
                                    Color(0xFF66BB6A)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.LightGray,
                                    Color.LightGray
                                )
                            )
                        }
                    )
                    .padding(vertical = Spacing.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier.scale(scale),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, AppColors.GoldPrimary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AppColors.GoldPrimary
            ),
            interactionSource = interactionSource
        ) {
            Text(text = text, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EnhancedCloseButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val rotation by animateFloatAsState(
        targetValue = if (isPressed) 90f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "rotation"
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .rotate(rotation),
        interactionSource = interactionSource
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = AppColors.Error.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = AppColors.Error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// NEW: Consolidated Booking Card Component
// REPLACE the ConsolidatedBookingCard function with this improved version

@Composable
fun ConsolidatedBookingCard(
    consolidatedBooking: ConsolidatedBooking,
    seat: SeatUi,
    seatData: MutableMap<String, SeatUi>,
    paymentModes: List<String>
) {
    val booking = consolidatedBooking.booking
    val timeSlots = consolidatedBooking.timeSlots

    var isEditing by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editAmountPaid by remember { mutableStateOf(booking.amountPaid) }
    var editDueAmount by remember { mutableStateOf(booking.dueAmount) }
    var editPaymentMode by remember { mutableStateOf(if (booking.paymentMode == "Online") 1 else 0) }

    val isExpired = isDateExpired(booking.expiryDate)
    val isExpiringSoon = isDateWithinDays(booking.expiryDate, 7)

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = AppColors.Error.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "⚠️",
                                fontSize = 32.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Spacing.medium))
                    Text(
                        text = "Delete All Bookings?",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "This will delete all ${timeSlots.size} time slot(s) for this student.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(Spacing.medium))

                    // Show booking details
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Column(
                            modifier = Modifier.padding(Spacing.medium),
                            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            Text(
                                text = "Student: ${booking.occupiedBy}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Mobile: ${booking.mobileNo}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Text(
                                text = "Time Slots: ${timeSlots.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text(
                        text = "This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.Error,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Delete all time slots for this student
                        timeSlots.forEach { (slotIndex, _) ->
                            FirestoreRepository.deleteBooking(
                                seatNumber = seat.seatNumber,
                                slotIndex = slotIndex,
                                onSuccess = {
                                    android.util.Log.d("DELETE_SUCCESS", "Slot $slotIndex deleted")
                                },
                                onFailure = { e ->
                                    android.util.Log.e("DELETE_ERROR", "Failed to delete slot $slotIndex", e)
                                }
                            )
                        }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Error
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = "Delete All",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, Color.LightGray),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                // IMPROVED: Header with compact time slots display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Compact Time Slots Display with Gradient Background
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier.background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        AppColors.Info.copy(alpha = 0.1f),
                                        AppColors.Info.copy(alpha = 0.05f)
                                    )
                                )
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(Spacing.medium),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                            ) {
                                // Clock icon
                                Text(
                                    text = "🕐",
                                    fontSize = 18.sp
                                )

                                // Time slots as comma-separated compact text
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    // Show first 2 slots
                                    val displaySlots = timeSlots.take(2)
                                    displaySlots.forEach { (_, timeSlot) ->
                                        Text(
                                            text = timeSlot,
                                            color = AppColors.Info,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }

                                    // Show "+X more" if more than 2 slots
                                    if (timeSlots.size > 2) {
                                        Text(
                                            text = "+${timeSlots.size - 2} more",
                                            color = AppColors.Info.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(Spacing.small))

                    // Status Badge
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

                // Student Name with Slot Count Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = booking.occupiedBy,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )

                    // Compact Slot Count Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppColors.GoldPrimary.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                        ) {
                            Text(
                                text = "⏰",
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${timeSlots.size}",
                                color = AppColors.GoldDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.medium))
                Divider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(Spacing.medium))

                if (!isEditing) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        DetailRowEnhanced("Bill No:", booking.billNo)
                        DetailRowEnhanced("Mobile:", booking.mobileNo)
                        DetailRowEnhanced("Subscription:", booking.subscriptionType)
                        DetailRowEnhanced("DOA:", booking.admissionDate)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Expiry:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontWeight = FontWeight.Medium)
                            Text(
                                booking.expiryDate,
                                color = when {
                                    isExpired -> AppColors.Error
                                    isExpiringSoon -> AppColors.Warning
                                    else -> Color.Black
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount Paid:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontWeight = FontWeight.Medium)
                            Text("₹${booking.amountPaid}", color = AppColors.Success, fontWeight = FontWeight.Bold)
                        }

                        val hasDue = booking.dueAmount.toIntOrNull()?.let { it > 0 } ?: false
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Due:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontWeight = FontWeight.Medium)
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                Text(
                                    "₹${booking.dueAmount}",
                                    color = if (hasDue) AppColors.Error else Color.Black,
                                    fontWeight = if (hasDue) FontWeight.Bold else FontWeight.Normal
                                )
                                if (hasDue) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
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
                            Text("Payment Mode:", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontWeight = FontWeight.Medium)
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                Text(
                                    if (booking.paymentMode == "Online") "💳" else "💵",
                                    fontSize = 14.sp
                                )
                                Text(
                                    booking.paymentMode,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.medium))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        EnhancedButton(
                            text = "Edit",
                            onClick = {
                                isEditing = true
                                editAmountPaid = booking.amountPaid
                                editDueAmount = booking.dueAmount
                                editPaymentMode = if (booking.paymentMode == "Online") 1 else 0
                            },
                            modifier = Modifier.weight(1f),
                            isPrimary = false
                        )

                        Button(
                            onClick = {
                                showDeleteDialog = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Error.copy(alpha = 0.1f),
                                contentColor = AppColors.Error
                            )
                        ) {
                            Text("Delete All", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                } else {
                    // Edit mode UI
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                        EnhancedTextField(
                            value = editAmountPaid,
                            onValueChange = { editAmountPaid = it },
                            label = "Amount Paid",
                            icon = "₹",
                            keyboardType = KeyboardType.Number
                        )

                        EnhancedTextField(
                            value = editDueAmount,
                            onValueChange = { editDueAmount = it },
                            label = "Due Amount",
                            icon = "₹",
                            keyboardType = KeyboardType.Number
                        )

                        EnhancedPaymentModeSelector(
                            paymentModes = paymentModes,
                            selectedIndex = editPaymentMode,
                            onSelect = { editPaymentMode = it }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                        ) {
                            EnhancedButton(
                                text = "Save",
                                onClick = {
                                    val updatedBooking = booking.copy(
                                        amountPaid = editAmountPaid,
                                        dueAmount = editDueAmount,
                                        paymentMode = paymentModes[editPaymentMode]
                                    )

                                    // Update all time slots for this student
                                    timeSlots.forEach { (slotIndex, _) ->
                                        FirestoreRepository.saveBooking(
                                            seatNumber = seat.seatNumber,
                                            slotIndex = slotIndex,
                                            booking = updatedBooking,
                                            onSuccess = {
                                                android.util.Log.d("UPDATE_SUCCESS", "Slot $slotIndex updated")
                                            },
                                            onFailure = { e ->
                                                android.util.Log.e("UPDATE_ERROR", "Failed to update slot $slotIndex", e)
                                            }
                                        )
                                    }
                                    isEditing = false
                                },
                                modifier = Modifier.weight(1f),
                                isPrimary = true
                            )

                            EnhancedButton(
                                text = "Cancel",
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f),
                                isPrimary = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRowEnhanced(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            fontWeight = FontWeight.Normal
        )
    }
}