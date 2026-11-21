package eu.tutorials.kl_boox_house

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

object AppColors {
    val GoldPrimary = Color(0xFFD6AE5E)
    val GoldDark = Color(0xFFC7963E)
}

/**
 * Enhanced Bottom Navigation - Compact and modern
 * Icons only, no labels for cleaner look
 */
@Composable
fun EnhancedBottomNavigation(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        shadowElevation = 16.dp
    ) {
        Box(
            modifier = Modifier.background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A1A),
                        Color(0xFF0D0D0D)
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                CompactNavItem(
                    icon = Icons.Outlined.Home,
                    isSelected = selectedItem == 0,
                    onClick = { onItemSelected(0) }
                )

                // Seats
                CompactNavItem(
                    icon = Icons.Default.Search,
                    isSelected = selectedItem == 1,
                    onClick = { onItemSelected(1) }
                )

                // Messages
                CompactNavItem(
                    icon = Icons.Outlined.Email,
                    isSelected = selectedItem == 2,
                    onClick = { onItemSelected(2) }
                )

                // Reports
                CompactNavItem(
                    icon = Icons.Outlined.List,
                    isSelected = selectedItem == 3,
                    onClick = { onItemSelected(3) }
                )
            }
        }
    }
}

/**
 * Compact navigation item without labels
 */
@Composable
private fun CompactNavItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Scale animation
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    // Icon color animation
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) AppColors.GoldPrimary else Color.White.copy(alpha = 0.6f),
        animationSpec = tween(300),
        label = "iconColor"
    )

    // Pulsing glow animation for selected item
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Pulsing glow background for selected item
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AppColors.GoldPrimary.copy(alpha = pulseAlpha),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Icon container
        Box(
            modifier = Modifier.size(if (isSelected) 44.dp else 40.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background for selected item
            if (isSelected) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier.background(
                            Brush.radialGradient(
                                colors = listOf(
                                    AppColors.GoldPrimary.copy(alpha = 0.25f),
                                    AppColors.GoldDark.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                    )
                }
            }

            // Icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(if (isSelected) 26.dp else 24.dp),
                tint = iconColor
            )
        }

        // Active indicator dot
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 20.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(4.dp)
                        .scale(scale),
                    shape = CircleShape,
                    color = AppColors.GoldPrimary
                ) {}
            }
        }
    }
}