package eu.tutorials.kl_boox_house


import android.view.MotionEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.sceneview.Scene

import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale




@Composable
fun MainScreen() {
    var selectedSeat by remember { mutableStateOf<SeatUi?>(null) }
    val seatData = remember { mutableStateMapOf<String, SeatUi>() }

    // Enhanced form fields for selected seat
    var name by remember { mutableStateOf("") }
    var billNo by remember { mutableStateOf("") }
    var selectedTimeSlot by remember { mutableStateOf(0) }
    var admissionDate by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }

    // Time slots
    val timeSlots = listOf(
        "6:00 AM - 10:00 AM",
        "10:00 AM - 2:00 PM",
        "2:00 PM - 6:00 PM",
        "6:00 PM - 10:00 PM"
    )

    // Initialize seat data
    LaunchedEffect(Unit) {
        for (i in 1..57) {
            seatData["seat_$i"] = SeatUi(
                seatNumber = i,
                timeSlotBookings = mutableMapOf()
            )
        }
    }

    // Reset form when seat or time slot changes
    LaunchedEffect(selectedSeat, selectedTimeSlot) {
        selectedSeat?.let { seat ->
            val booking = seat.timeSlotBookings[selectedTimeSlot]
            name = booking?.occupiedBy ?: ""
            billNo = booking?.billNo ?: ""
            admissionDate = booking?.admissionDate ?: getCurrentDate()
            expiryDate = booking?.expiryDate ?: ""
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val environmentLoader = rememberEnvironmentLoader(engine)
        val centerNode = rememberNode(engine)

        val cameraNode = rememberCameraNode(engine) {
            position = Position(x = 0f, y = 5f, z = 2f)
            lookAt(centerNode)
            centerNode.addChildNode(this)
        }

        var hasRotatedOnce by remember { mutableStateOf(false) }
        val cameraRotation = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            if (!hasRotatedOnce) {
                hasRotatedOnce = true
                cameraRotation.animateTo(
                    targetValue = 360f,
                    animationSpec = tween(durationMillis = 6000, easing = LinearEasing)
                )
            }
        }

        val modelNode = remember {
            ModelNode(
                modelInstance = modelLoader.createModelInstance("kl_boox_house_2.glb"),
                scaleToUnits = 1.5f
            ).apply {
                rotation = Rotation(y = -90f)
            }
        }

        var seatsInitialized by remember { mutableStateOf(false) }

        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            cameraManipulator = rememberCameraManipulator(
                orbitHomePosition = cameraNode.worldPosition,
                targetPosition = centerNode.worldPosition
            ),
            childNodes = listOf(centerNode, modelNode),
            environment = environmentLoader.createHDREnvironment("moonless_golf_4k.hdr")!!,
            onTouchEvent = { motionEvent, hitResult ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    val hitNode = hitResult?.node as? ModelNode
                    val nodeName = hitNode?.name
                    if (nodeName?.startsWith("seat_") == true) {
                        val seat = seatData[nodeName]
                        if (seat != null) {
                            selectedSeat = seat
                            selectedTimeSlot = 0 // Reset to first time slot when selecting new seat
                        }
                    }
                }
                true
            },
            onFrame = {
                if (cameraRotation.value < 360f) {
                    centerNode.rotation = Rotation(y = cameraRotation.value)
                    cameraNode.lookAt(centerNode)
                }

                if (!seatsInitialized) {
                    modelNode.setupSeats()
                    seatsInitialized = true
                }
            }
        )

        // Logo Image
        Image(
            painter = painterResource(id = R.drawable.library4),
            contentDescription = "Library Logo",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .size(150.dp)
        )

        // Enhanced Seat Info Form
        selectedSeat?.let { seat ->
            val currentBooking = seat.timeSlotBookings[selectedTimeSlot]
            val isCurrentSlotOccupied = currentBooking != null

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.95f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Text(
                        text = "Seat ${seat.seatNumber}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Black
                    )

                    // Show overall seat status
                    val occupiedSlots = seat.timeSlotBookings.size
                    Text(
                        text = "Occupied Slots: $occupiedSlots/4",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )

                    Divider(color = Color.Gray, thickness = 1.dp)

                    // Time Slot Selector
                    Text(
                        text = "Select Time Slot:",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
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
                                Text(
                                    text = if (isSlotOccupied) "Occupied" else "Available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSlotOccupied) Color.Red else Color.Green
                                )
                            }
                        }
                    }

                    // Show current slot status
                    Text(
                        text = "Current Slot Status: ${if (isCurrentSlotOccupied) "Occupied" else "Available"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCurrentSlotOccupied) Color.Red else Color.Green
                    )

                    Divider(color = Color.Gray, thickness = 1.dp)

                    // Form Fields
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Student Name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCurrentSlotOccupied,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = billNo,
                        onValueChange = { billNo = it },
                        label = { Text("Bill Number") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCurrentSlotOccupied,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = admissionDate,
                            onValueChange = { admissionDate = it },
                            label = { Text("DOA") },
                            modifier = Modifier.weight(1f),
                            enabled = !isCurrentSlotOccupied,
                            placeholder = { Text("DD/MM/YYYY") },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = expiryDate,
                            onValueChange = { expiryDate = it },
                            label = { Text("Expiry Date") },
                            modifier = Modifier.weight(1f),
                            enabled = !isCurrentSlotOccupied,
                            placeholder = { Text("DD/MM/YYYY") },
                            singleLine = true
                        )
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isCurrentSlotOccupied) {
                            Button(
                                onClick = {
                                    val updatedBookings = seat.timeSlotBookings.toMutableMap()
                                    updatedBookings.remove(selectedTimeSlot)
                                    val updatedSeat = seat.copy(
                                        timeSlotBookings = updatedBookings
                                    )
                                    selectedSeat = updatedSeat
                                    seatData["seat_${seat.seatNumber}"] = updatedSeat
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Release Slot", color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = {
                                    val newBooking = TimeSlotBooking(
                                        occupiedBy = name,
                                        billNo = billNo,
                                        admissionDate = admissionDate,
                                        expiryDate = expiryDate,
                                        occupiedSince = System.currentTimeMillis()
                                    )
                                    val updatedBookings = seat.timeSlotBookings.toMutableMap()
                                    updatedBookings[selectedTimeSlot] = newBooking
                                    val updatedSeat = seat.copy(
                                        timeSlotBookings = updatedBookings
                                    )
                                    selectedSeat = updatedSeat
                                    seatData["seat_${seat.seatNumber}"] = updatedSeat
                                },
                                modifier = Modifier.weight(1f),
                                enabled = name.isNotBlank() && billNo.isNotBlank() &&
                                        admissionDate.isNotBlank() && expiryDate.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                            ) {
                                Text("Book Slot", color = Color.White)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                // Reset form
                                name = ""
                                billNo = ""
                                admissionDate = getCurrentDate()
                                expiryDate = ""
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isCurrentSlotOccupied
                        ) {
                            Text("Clear")
                        }
                    }

                    // Display current booking info if slot is occupied
                    if (isCurrentSlotOccupied && currentBooking != null) {
                        Divider(color = Color.Gray, thickness = 1.dp)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Current Booking for ${timeSlots[selectedTimeSlot]}:",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Text("Name: ${currentBooking.occupiedBy}", color = Color.Black)
                                Text("Bill No: ${currentBooking.billNo}", color = Color.Black)
                                Text("DOA: ${currentBooking.admissionDate}", color = Color.Black)
                                Text("Expiry: ${currentBooking.expiryDate}", color = Color.Black)
                                Text("Booked: ${formatTime(currentBooking.occupiedSince)}", color = Color.Black)
                            }
                        }
                    }

                    // Show all bookings for this seat
                    if (seat.timeSlotBookings.isNotEmpty()) {
                        Divider(color = Color.Gray, thickness = 1.dp)

                        Text(
                            text = "All Bookings for this Seat:",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.Black
                        )

                        seat.timeSlotBookings.entries.sortedBy { it.key }.forEach { (slotIndex, booking) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (slotIndex == selectedTimeSlot)
                                        Color.Blue.copy(alpha = 0.1f)
                                    else
                                        Color.Gray.copy(alpha = 0.1f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = timeSlots[slotIndex],
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Black
                                    )
                                    Text("${booking.occupiedBy} (${booking.billNo})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// New data class for individual time slot bookings
data class TimeSlotBooking(
    val occupiedBy: String,
    val billNo: String,
    val admissionDate: String,
    val expiryDate: String,
    val occupiedSince: Long
)

// Enhanced data class that supports multiple bookings per seat
data class SeatUi(
    val seatNumber: Int,
    val timeSlotBookings: MutableMap<Int, TimeSlotBooking> = mutableMapOf()
)

// Helper function to get current date
fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date())
}

// Traverse seat nodes and attach interaction (unchanged)
fun ModelNode.setupSeats() {
    var seatCounter = 1
    fun markPotentialSeats(node: ModelNode) {
        val isPotentialSeat = node.worldScale.run {
            x < 1.2f && y < 1.2f && z < 1.2f
        }

        if (isPotentialSeat) {
            node.name = "seat_$seatCounter"
            node.scale = Scale(node.scale.x * 1.1f, node.scale.y * 1.1f, node.scale.z * 1.1f)
            seatCounter++
        }

        val childCountMethod = node.javaClass.methods.find { it.name == "getChildCount" }
        val getChildMethod = node.javaClass.methods.find { it.name == "getChild" && it.parameterTypes.size == 1 }

        if (childCountMethod != null && getChildMethod != null) {
            val childCount = childCountMethod.invoke(node) as Int
            for (i in 0 until childCount) {
                val child = getChildMethod.invoke(node, i)
                if (child is ModelNode) {
                    markPotentialSeats(child)
                }
            }
        }
    }
    markPotentialSeats(this)
}

// Add touch listener via reflection (unchanged)
fun ModelNode.addOnNodeTouchListener(listener: (ModelNode, android.view.MotionEvent) -> Boolean) {
    try {
        val method = javaClass.getMethod("setOnTouchListener", Function2::class.java)
        method.invoke(this, listener)
    } catch (e: Exception) {
        println("Failed to add touch listener: ${e.message}")
    }
}

// Enhanced format helper
fun formatTime(timeMillis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timeMillis))
}