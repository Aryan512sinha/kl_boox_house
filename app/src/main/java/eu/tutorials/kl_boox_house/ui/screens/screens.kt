package eu.tutorials.kl_boox_house.ui.screens


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.github.sceneview.SceneView
import io.github.sceneview.node.ModelNode


@Composable
fun SceneViewScreen() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            SceneView(context).apply {
                val modelNode = ModelNode(
                    engine=engine,
                    modelInstance = null
                ).apply {
                    loadModelGlbAsync(
                        context = context,
                        glbFileLocation = "kl_boox_house.glb",
                        scaleToUnits = 1.0f
                    )
                }
                addChildNode(modelNode)
            }

        }
    )
}


