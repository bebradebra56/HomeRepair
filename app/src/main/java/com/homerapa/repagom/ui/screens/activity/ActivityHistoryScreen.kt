package com.homerapa.repagom.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.homerapa.repagom.data.db.ActivityEntity
import com.homerapa.repagom.ui.components.AppTopBar
import com.homerapa.repagom.ui.components.EmptyState
import com.homerapa.repagom.ui.components.toFormattedDate
import com.homerapa.repagom.ui.theme.*
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ActivityHistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = (app as HomeRepairApplication).repository

    val activities = repository.getAllActivities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun ActivityHistoryScreen(
    onBack: () -> Unit,
    viewModel: ActivityHistoryViewModel = viewModel()
) {
    val activities by viewModel.activities.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppTopBar(title = "Activity History", onBack = onBack) }
    ) { padding ->
        if (activities.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Default.History,
                    title = "No Activity Yet",
                    subtitle = "Your actions will appear here as you use the app."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activities, key = { it.id }) { activity ->
                    ActivityItem(activity)
                }
            }
        }
    }
}

@Composable
private fun ActivityItem(activity: ActivityEntity) {
    val (icon, color) = getActivityIconAndColor(activity.action)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeline dot and line
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        activity.action,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = color
                    )
                    Text(
                        activity.timestamp.toFormattedDate(),
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryText.copy(alpha = 0.6f)
                    )
                }
                if (activity.description.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        activity.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }
            }
        }
    }
}

private fun getActivityIconAndColor(action: String): Pair<ImageVector, Color> {
    return when {
        action.contains("Photo") -> Pair(Icons.Default.Photo, NavyBlue)
        action.contains("Issue") && action.contains("Fixed") -> Pair(Icons.Default.CheckCircle, StatusGreen)
        action.contains("Issue") -> Pair(Icons.Default.ReportProblem, WarmRed)
        action.contains("Task") -> Pair(Icons.Default.Task, StatusOrange)
        action.contains("Room") -> Pair(Icons.Default.Home, WarmBrown)
        action.contains("Project") -> Pair(Icons.Default.Folder, NavyBlueMid)
        action.contains("Before/After") -> Pair(Icons.Default.CompareArrows, StatusGreen)
        action.contains("Profile") -> Pair(Icons.Default.Person, LightWood)
        else -> Pair(Icons.Default.Circle, SecondaryText)
    }
}
