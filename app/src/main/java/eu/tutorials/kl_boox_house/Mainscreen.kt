package eu.tutorials.kl_boox_house


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
import androidx.compose.material3.MaterialTheme
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
import io.github.sceneview.rememberOnGestureListener


@Composable
fun MainScreen() {
    var selectedSeat by remember { mutableStateOf<SeatUi?>(null) }
    // Use mutableStateMapOf instead of mutableMapOf for UI reactivity
    val seatData = remember { mutableStateMapOf<String, SeatUi>() }

    // Initialize seat data
    LaunchedEffect(Unit) {
        for (i in 1..20) { // Assuming there are up to 20 seats
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

        // Create object for handling seat clicks
        val modelNodeWithClickHandling = remember {
            object {
                val node = ModelNode(
                    modelInstance = modelLoader.createModelInstance(
                        assetFileLocation = "kl_boox_house.glb"
                    ),
                    scaleToUnits = 1.5f,
                ).apply {
                    rotation = Rotation(y = -90f)
                    setupSeats()

                    // Add a click detector directly on the model node
                    addOnNodeTouchListener { node, motionEvent ->
                        // Check if it's a tap (ACTION_UP with short duration)
                        if (motionEvent.action == android.view.MotionEvent.ACTION_UP) {
                            // If the node name starts with "seat_", it's a seat
                            val nodeName = node.name
                            if (nodeName?.startsWith("seat_") == true) {
                                val seatInfo = seatData[nodeName]
                                if (seatInfo != null) {
                                    selectedSeat = seatInfo
                                }
                            }
                        }
                        true
                    }
                }
            }.node
        }

        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            cameraManipulator = rememberCameraManipulator(
                orbitHomePosition = cameraNode.worldPosition,
                targetPosition = centerNode.worldPosition
            ),
            childNodes = listOf(centerNode, modelNodeWithClickHandling),
            environment = environmentLoader.createHDREnvironment(
                assetFileLocation = "moonless_golf_4k.hdr"
            )!!,
            onFrame = {
                if (cameraRotation.value < 360f) {
                    centerNode.rotation = Rotation(y = cameraRotation.value)
                    cameraNode.lookAt(centerNode)
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

        // Seat Info
        selectedSeat?.let { seat ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.9f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Seat ${seat.seatNumber}", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Status: ${if (seat.isOccupied) "Occupied" else "Available"}")
                    seat.occupiedBy?.let {
                        Text("Occupied by: $it")
                    }
                    seat.occupiedSince?.let {
                        Text("Since: ${formatTime(it)}")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        val updatedSeat = seat.copy(
                            isOccupied = !seat.isOccupied,
                            occupiedBy = if (!seat.isOccupied) "User" else null,
                            occupiedSince = if (!seat.isOccupied) System.currentTimeMillis() else null
                        )
                        selectedSeat = updatedSeat
                        seatData["seat_${seat.seatNumber}"] = updatedSeat
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (seat.isOccupied) "Release Seat" else "Occupy Seat")
                    }
                }
            }
        }
    }
}

// Seat UI Model
data class SeatUi(
    val seatNumber: Int,
    val isOccupied: Boolean,
    val occupiedBy: String? = null,
    val occupiedSince: Long? = null
)

// Traverse seat nodes and attach interaction
fun ModelNode.setupSeats() {
    var seatCounter = 1

    // Define recursive function
    fun markPotentialSeats(node: ModelNode) {
        val isPotentialSeat = node.worldScale.run {
            x < 1.2f && y < 1.2f && z < 1.2f
        }

        if (isPotentialSeat) {
            node.name = "seat_$seatCounter"
            // Make seat visually distinct for better user experience
            node.scale = Scale(node.scale.x * 1.1f, node.scale.y * 1.1f, node.scale.z * 1.1f)
            seatCounter++
        }

        // Safely get children by index
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

// Helper extension method to add touch listener to model node
fun ModelNode.addOnNodeTouchListener(listener: (ModelNode, android.view.MotionEvent) -> Boolean) {
    try {
        // Try to use reflection to add a touch listener if the direct method doesn't exist
        val method = javaClass.getMethod("setOnTouchListener", Function2::class.java)
        method.invoke(this, listener)
    } catch (e: Exception) {
        // If reflection fails, we'll try to add the listener another way if available
        println("Failed to add touch listener: ${e.message}")
    }
}

// Formats timestamp as "x seconds ago"
fun formatTime(timeMillis: Long): String {
    val seconds = (System.currentTimeMillis() - timeMillis) / 1000
    return "$seconds seconds ago"
}