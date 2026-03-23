package com.homerapa.repagom.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.homerapa.repagom.data.db.IssueEntity
import com.homerapa.repagom.data.db.PhotoEntity
import com.homerapa.repagom.data.db.ProjectEntity
import com.homerapa.repagom.ui.components.*
import com.homerapa.repagom.ui.theme.*
import com.homerapa.repagom.viewmodel.AppViewModel
import com.homerapa.repagom.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    appViewModel: AppViewModel,
    onNavigateToProjects: () -> Unit,
    onNavigateToProject: (Long) -> Unit,
    onNavigateToIssues: () -> Unit,
    onNavigateToAddPhoto: (Long) -> Unit,
    onNavigateToCreateProject: () -> Unit,
    onNavigateToActivityHistory: () -> Unit,
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val prefs by appViewModel.userPreferences.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            DashboardHeader(
                userName = prefs.userName,
                onActivityHistory = onNavigateToActivityHistory
            )
        }

        // Active Project Card
        item {
            if (uiState.activeProject != null) {
                ActiveProjectCard(
                    project = uiState.activeProject!!,
                    openIssues = uiState.openIssuesCount,
                    photos = uiState.photosCount,
                    tasks = uiState.inProgressTasksCount,
                    onClick = { onNavigateToProject(uiState.activeProject!!.id) }
                )
            } else {
                NoProjectCard(onCreateProject = onNavigateToCreateProject)
            }
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    value = uiState.openIssuesCount.toString(),
                    label = "Open Issues",
                    icon = Icons.Default.Warning,
                    backgroundColor = WarmRed,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = uiState.photosCount.toString(),
                    label = "Photos",
                    icon = Icons.Default.Photo,
                    backgroundColor = NavyBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = uiState.inProgressTasksCount.toString(),
                    label = "In Progress",
                    icon = Icons.Default.Engineering,
                    backgroundColor = StatusOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions
        item {
            SectionHeader(title = "Quick Actions")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    QuickActionButton(
                        icon = Icons.Default.AddAPhoto,
                        label = "Add Photo",
                        color = NavyBlue,
                        onClick = {
                            val pid = uiState.activeProject?.id
                            if (pid != null) onNavigateToAddPhoto(pid)
                        }
                    )
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.ReportProblem,
                        label = "Add Issue",
                        color = WarmRed,
                        onClick = onNavigateToIssues
                    )
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.CreateNewFolder,
                        label = "New Project",
                        color = WarmBrown,
                        onClick = onNavigateToCreateProject
                    )
                }
                item {
                    QuickActionButton(
                        icon = Icons.Default.GridView,
                        label = "All Projects",
                        color = StatusGreen,
                        onClick = onNavigateToProjects
                    )
                }
            }
        }

        // Recent Photos
        if (uiState.recentPhotos.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recent Photos",
                    action = "See All",
                    onAction = {
                        uiState.activeProject?.let { onNavigateToProject(it.id) }
                    }
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.recentPhotos) { photo ->
                        PhotoCard(
                            uri = photo.uri,
                            label = photo.label,
                            modifier = Modifier.width(140.dp),
                            height = 110.dp
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        // Recent Issues
        if (uiState.recentIssues.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recent Issues",
                    action = "See All",
                    onAction = onNavigateToIssues
                )
            }
            items(uiState.recentIssues.take(3)) { issue ->
                DashboardIssueCard(
                    issue = issue,
                    onClick = onNavigateToIssues,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Before/After Progress
        item {
            BeforeAfterProgressCard(
                count = uiState.beforeAfterCount,
                onViewAll = {
                    uiState.activeProject?.let { onNavigateToProject(it.id) }
                }
            )
        }
    }
}

@Composable
private fun DashboardHeader(
    userName: String,
    onActivityHistory: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(listOf(NavyBlue, NavyBlueMid))
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        if (userName.isNotEmpty()) "Hello, $userName 👋" else "Hello there 👋",
                        color = SkyBlue.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Dashboard",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onActivityHistory) {
                    Icon(Icons.Default.History, contentDescription = "Activity", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ActiveProjectCard(
    project: ProjectEntity,
    openIssues: Int,
    photos: Int,
    tasks: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SkyBlue),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Active Project",
                        style = MaterialTheme.typography.labelMedium,
                        color = NavyBlue.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        project.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = NavyBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        project.propertyType,
                        style = MaterialTheme.typography.bodySmall,
                        color = NavyBlue.copy(alpha = 0.6f)
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = NavyBlue.copy(alpha = 0.6f),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MiniStat("$openIssues issues", Icons.Default.Warning)
                MiniStat("$photos photos", Icons.Default.Photo)
                MiniStat("$tasks tasks", Icons.Default.Engineering)
            }
        }
    }
}

@Composable
private fun MiniStat(text: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = NavyBlue.copy(alpha = 0.7f))
        Text(text, style = MaterialTheme.typography.labelSmall, color = NavyBlue.copy(alpha = 0.8f))
    }
}

@Composable
private fun NoProjectCard(onCreateProject: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SoftYellow.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text("No Active Project", style = MaterialTheme.typography.titleMedium, color = NavyBlue, fontWeight = FontWeight.Bold)
            Text("Create your first project to get started", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onCreateProject,
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Create Project")
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(90.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun DashboardIssueCard(
    issue: IssueEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(WarmRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ReportProblem, contentDescription = null, tint = WarmRed, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(issue.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = DarkText)
                Text(issue.category, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            }
            Column(horizontalAlignment = Alignment.End) {
                PriorityChip(issue.priority)
                Spacer(Modifier.height(4.dp))
                StatusChip(issue.status)
            }
        }
    }
}

@Composable
private fun BeforeAfterProgressCard(count: Int, onViewAll: () -> Unit) {
    if (count == 0) return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onViewAll),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StatusGreen.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CompareArrows, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Before / After", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StatusGreen)
                Text("$count comparisons completed", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StatusGreen)
        }
    }
}
