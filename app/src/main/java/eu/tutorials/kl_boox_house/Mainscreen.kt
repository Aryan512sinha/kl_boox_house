package eu.tutorials.kl_boox_house


import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.github.sceneview.Scene
import io.github.sceneview.animation.Transition.animateRotation
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
    Box(modifier = Modifier.fillMaxSize()) {
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val environmentLoader = rememberEnvironmentLoader(engine)
        val centerNode = rememberNode(engine)

        val cameraNode = rememberCameraNode(engine) {
            position = Position(y = -0.5f, z = 2.5f)
            lookAt(centerNode)
            centerNode.addChildNode(this)
        }

        val cameraTransition = rememberInfiniteTransition(label = "CameraRotation")
        val cameraRotation by cameraTransition.animateRotation(
            initialValue = Rotation(y = 0f),
            targetValue = Rotation(y = 360f),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8000)
            )
        )

        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            cameraManipulator = rememberCameraManipulator(
                orbitHomePosition = cameraNode.worldPosition,
                targetPosition = centerNode.worldPosition
            ),
            childNodes = listOf(
                centerNode,
                rememberNode {
                    ModelNode(
                        modelInstance = modelLoader.createModelInstance(
                            assetFileLocation = "assets/kl_boox_house.glb"
                        ),
                        scaleToUnits = 1.0f
                    )
                }
            ),
            environment = environmentLoader.createHDREnvironment(
                assetFileLocation = "assets/passendorf_sky_4k.hdr"
            )!!,
            onFrame = {
                centerNode.rotation = cameraRotation
                cameraNode.lookAt(centerNode)
            },
            onGestureListener = rememberOnGestureListener(
                onDoubleTap = { _, node ->
                    node?.let {
                        // Create a new Scale object with increased values
                        val currentScale = it.scale
                        val newScale = Scale(
                            currentScale.x * 1.5f,
                            currentScale.y * 1.5f,
                            currentScale.z * 1.5f
                        )
                        // Assign the new scale object to the node
                        it.scale = newScale
                    }
                }
            )
        )
    }
}
