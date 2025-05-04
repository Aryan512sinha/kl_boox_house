package eu.tutorials.kl_boox_house


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    Box(modifier = Modifier.fillMaxSize()) {
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)
        val environmentLoader = rememberEnvironmentLoader(engine)
        val centerNode = rememberNode(engine)

        val cameraNode = rememberCameraNode(engine) {
            // Moved camera up and back for a vertical view
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
                            assetFileLocation = "kl_boox_house.glb"
                        ),
                        scaleToUnits = 2.0f,
                    ).apply {
                        // ✅ Rotate model vertically downward
                        rotation = Rotation(y = -90f)
                    }
                }
            ),
            environment = environmentLoader.createHDREnvironment(
                assetFileLocation = "moonless_golf_4k.hdr"
            )!!,
            onFrame = {
                if (cameraRotation.value < 360f) {
                    centerNode.rotation = Rotation(y = cameraRotation.value)
                    cameraNode.lookAt(centerNode)
                }
            },
            onGestureListener = rememberOnGestureListener(
                onDoubleTap = { _, node ->
                    node?.let {
                        val currentScale = it.scale
                        val newScale = Scale(
                            currentScale.x * 1.5f,
                            currentScale.y * 1.5f,
                            currentScale.z * 1.5f
                        )
                        it.scale = newScale
                    }
                }
            )
        )
    }
}

