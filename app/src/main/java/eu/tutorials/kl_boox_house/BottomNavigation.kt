package eu.tutorials.kl_boox_house

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.List
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable

object AppColors {
    val GoldPrimary = Color(0xFFCDA45D)
    val GoldDark = Color(0xFFB8873E)
}

@Composable
fun EnhancedBottomNavigation(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color(0xFF111827),
        shadowElevation = 24.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfessionalNavItem("Home", Icons.Filled.Home, selectedItem == 0, Modifier.weight(1f)) { onItemSelected(0) }
            ProfessionalNavItem("Seats", Icons.Filled.Search, selectedItem == 1, Modifier.weight(1f)) { onItemSelected(1) }
            ProfessionalNavItem("Messages", Icons.Outlined.Email, selectedItem == 2, Modifier.weight(1f)) { onItemSelected(2) }
            ProfessionalNavItem("Reports", Icons.Outlined.List, selectedItem == 3, Modifier.weight(1f)) { onItemSelected(3) }
        }
    }
}

@Composable
private fun ProfessionalNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val foreground = if (selected) AppColors.GoldPrimary else Color.White.copy(alpha = 0.58f)
    Column(
        modifier = modifier
            .height(60.dp)
            .padding(horizontal = 3.dp)
            .background(
                color = if (selected) Color(0xFF263449) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = foreground, modifier = Modifier.size(22.dp))
        Text(
            text = label,
            color = foreground,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(top = 3.dp)
        )
    }
}
