package eu.tutorials.kl_boox_house


import android.view.MotionEvent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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



@Composable
fun MainScreen() {
    var selectedSeat by remember { mutableStateOf<SeatUi?>(null) }
    val seatData = remember { mutableStateMapOf<String, SeatUi>() }

    // Form fields for selected seat
    var name by remember { mutableStateOf("") }
    var validUntil by remember { mutableStateOf("") }

    // Initialize seat data
    LaunchedEffect(Unit) {
        for (i in 1..57) {
            seatData["seat_$i"] = SeatUi(
                seatNumber = i,
                isOccupied = false
            )
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
            onTouchEvent = { motionEvent,hitResult ->
                if (motionEvent.action == MotionEvent.ACTION_UP) {
                    val hitNode = hitResult?.node as? ModelNode
                    val nodeName = hitNode?.name
                    if (nodeName?.startsWith("seat_") == true) {
                        val seat = seatData[nodeName]
                        if (seat != null) {
                            selectedSeat = seat
                            name = seat.occupiedBy ?: ""
                            validUntil = seat.validUntil ?: ""
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

        // Seat Info Form
        selectedSeat?.let { seat ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Seat ${seat.seatNumber}", style = MaterialTheme.typography.titleLarge, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Status: ${if (seat.isOccupied) "Occupied" else "Available"}", color = Color.Black)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Occupied By") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = validUntil,
                        onValueChange = { validUntil = it },
                        label = { Text("Valid Until (e.g., 5 PM)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val updatedSeat = seat.copy(
                                isOccupied = !seat.isOccupied,
                                occupiedBy = if (!seat.isOccupied) name else null,
                                occupiedSince = if (!seat.isOccupied) System.currentTimeMillis() else null,
                                validUntil = if (!seat.isOccupied) validUntil else null
                            )
                            selectedSeat = updatedSeat
                            seatData["seat_${seat.seatNumber}"] = updatedSeat
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (seat.isOccupied) "Release Seat" else "Occupy Seat")
                    }
                }
            }
        }
    }
}

// Data class with extra field
data class SeatUi(
    val seatNumber: Int,
    val isOccupied: Boolean,
    val occupiedBy: String? = null,
    val occupiedSince: Long? = null,
    val validUntil: String? = null
)

// Traverse seat nodes and attach interaction
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

// Add touch listener via reflection
fun ModelNode.addOnNodeTouchListener(listener: (ModelNode, android.view.MotionEvent) -> Boolean) {
    try {
        val method = javaClass.getMethod("setOnTouchListener", Function2::class.java)
        method.invoke(this, listener)
    } catch (e: Exception) {
        println("Failed to add touch listener: ${e.message}")
    }
}

// Format helper
fun formatTime(timeMillis: Long): String {
    val seconds = (System.currentTimeMillis() - timeMillis) / 1000
    return "$seconds seconds ago"
}
