package com.homerapa.repagom.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.homerapa.repagom.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

fun Long.toFormattedDate(): String =
    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(this))

fun Long.toShortDate(): String =
    SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(this))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = NavyBlue,
            titleContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

@Composable
fun StatCard(
    value: String,
    label: String,
    icon: ImageVector,
    backgroundColor: Color,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = textColor.copy(alpha = 0.8f), modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = textColor, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = textColor.copy(alpha = 0.85f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun PriorityChip(priority: String, modifier: Modifier = Modifier) {
    val (color, icon) = when (priority) {
        "Critical" -> Pair(PriorityCritical, Icons.Default.Error)
        "High" -> Pair(PriorityHigh, Icons.Default.KeyboardArrowUp)
        "Medium" -> Pair(PriorityMedium, Icons.Default.Remove)
        else -> Pair(PriorityLow, Icons.Default.KeyboardArrowDown)
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(priority, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val color = when (status) {
        "New" -> StatusNew
        "In Review" -> StatusInReview
        "In Progress" -> StatusInProgress
        "Waiting" -> StatusWaiting
        "Fixed" -> StatusFixed
        "Closed" -> StatusClosed
        "Todo" -> StatusNew
        "Done" -> StatusFixed
        else -> SecondaryText
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Text(
            text = status,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PhotoCard(
    uri: String?,
    label: String = "",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    height: Dp = 120.dp
) {
    Card(
        modifier = modifier
            .height(height)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            if (uri != null) {
                AsyncImage(
                    model = uri,
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(SkyBlue.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Photo, contentDescription = null, tint = NavyBlue.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                }
            }
            if (label.isNotEmpty()) {
                Box(
                    Modifier
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector = Icons.Default.Folder,
    title: String,
    subtitle: String = "",
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(NavyBlue.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = NavyBlue.copy(alpha = 0.4f), modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (subtitle.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textAlign = TextAlign.Center)
        }
        if (action != null) {
            Spacer(Modifier.height(24.dp))
            action()
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    action: String = "",
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface)
        if (action.isNotEmpty()) {
            TextButton(onClick = onAction) {
                Text(action, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = WarmRed)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun GradientHeader(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(NavyBlue, NavyBlueMid, SkyBlueDark)
                )
            )
            .padding(16.dp),
        content = content
    )
}

@Composable
fun CategoryIcon(category: String, modifier: Modifier = Modifier) {
    val (icon, color) = when (category) {
        "Kitchen" -> Pair(Icons.Default.Kitchen, WarmBrown)
        "Bathroom" -> Pair(Icons.Default.Bathtub, StatusBlue)
        "Bedroom" -> Pair(Icons.Default.Bed, NavyBlueMid)
        "Living Room" -> Pair(Icons.Default.Weekend, ConstructionYellow)
        "Hallway" -> Pair(Icons.Default.MeetingRoom, LightWood)
        "Balcony" -> Pair(Icons.Default.Deck, StatusGreen)
        "Crack" -> Pair(Icons.Default.BrokenImage, WarmRed)
        "Paint" -> Pair(Icons.Default.FormatPaint, StatusOrange)
        "Tile" -> Pair(Icons.Default.GridOn, WoodBrown)
        "Electric" -> Pair(Icons.Default.ElectricBolt, SoftYellow)
        "Plumbing" -> Pair(Icons.Default.WaterDrop, StatusBlue)
        "Furniture" -> Pair(Icons.Default.Chair, WarmBrown)
        else -> Pair(Icons.Default.Build, SecondaryText)
    }
    Icon(icon, contentDescription = category, tint = color, modifier = modifier)
}

@Composable
fun LabelTag(
    label: String,
    modifier: Modifier = Modifier
) {
    val color = when (label) {
        "Before" -> NavyBlue
        "After" -> StatusGreen
        "Defect" -> WarmRed
        "In Progress" -> StatusOrange
        "Idea" -> ConstructionYellow
        "Material" -> WoodBrown
        else -> SecondaryText
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun AvatarCircle(
    initials: String,
    size: Dp = 48.dp,
    backgroundColor: Color = NavyBlue
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials.take(2).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.35f).sp
        )
    }
}

@Composable
fun PropertyTypeIcon(type: String, modifier: Modifier = Modifier) {
    val icon = when (type) {
        "Apartment" -> Icons.Default.Apartment
        "House" -> Icons.Default.House
        "Office" -> Icons.Default.Business
        "Single Room" -> Icons.Default.MeetingRoom
        else -> Icons.Default.HomeWork
    }
    Icon(icon, contentDescription = type, modifier = modifier)
}
