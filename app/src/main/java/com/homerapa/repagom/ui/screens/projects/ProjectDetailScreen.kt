package com.homerapa.repagom.ui.screens.projects

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.homerapa.repagom.data.db.RoomEntity
import com.homerapa.repagom.ui.components.*
import com.homerapa.repagom.ui.theme.*
import com.homerapa.repagom.viewmodel.AppViewModel
import com.homerapa.repagom.viewmodel.IssueViewModel
import com.homerapa.repagom.viewmodel.PhotoViewModel
import com.homerapa.repagom.viewmodel.ProjectViewModel
import com.homerapa.repagom.viewmodel.RoomViewModel

@Composable
fun ProjectDetailScreen(
    projectId: Long,
    appViewModel: AppViewModel,
    onBack: () -> Unit,
    onNavigateToRoom: (Long) -> Unit,
    onNavigateToCreateRoom: () -> Unit,
    onNavigateToAddPhoto: () -> Unit,
    onNavigateToIssues: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToBeforeAfter: () -> Unit,
    onNavigateToEdit: () -> Unit,
    projectViewModel: ProjectViewModel = viewModel(),
    roomViewModel: RoomViewModel = viewModel(),
    photoViewModel: PhotoViewModel = viewModel(),
    issueViewModel: IssueViewModel = viewModel()
) {
    val prefs by appViewModel.userPreferences.collectAsStateWithLifecycle()
    val projectState by projectViewModel.uiState.collectAsStateWithLifecycle()
    val roomState by roomViewModel.uiState.collectAsStateWithLifecycle()
    val photoState by photoViewModel.uiState.collectAsStateWithLifecycle()
    val issueState by issueViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) {
        projectViewModel.loadProject(projectId)
        roomViewModel.loadRoomsForProject(projectId)
        photoViewModel.loadPhotosForProject(projectId)
        issueViewModel.loadIssuesForProject(projectId)
    }

    val project = projectState.selectedProject
    val isActive = prefs.activeProjectId == projectId

    Scaffold(
        topBar = {
            AppTopBar(
                title = project?.name ?: "Project",
                onBack = onBack,
                actions = {
                    if (!isActive) {
                        IconButton(onClick = { appViewModel.setActiveProject(projectId) }) {
                            Icon(Icons.Default.Star, contentDescription = "Set Active", tint = Color.White)
                        }
                    } else {
                        Icon(Icons.Default.Star, contentDescription = null, tint = ConstructionYellow, modifier = Modifier.padding(8.dp))
                    }
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateRoom,
                containerColor = ConstructionYellow,
                contentColor = DarkText,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Room")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Project Header
            item {
                project?.let { p ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        if (p.coverPhotoUri != null) {
                            AsyncImage(
                                model = p.coverPhotoUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
                        } else {
                            Box(
                                Modifier.fillMaxSize().background(
                                    Brush.linearGradient(listOf(NavyBlue, NavyBlueMid))
                                )
                            )
                        }
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            if (isActive) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ConstructionYellow
                                ) {
                                    Text(
                                        "⭐ Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DarkText,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                            Text(p.name, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                PropertyTypeIcon(p.propertyType, modifier = Modifier.size(14.dp))
                                Text(p.propertyType, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                                if (p.address.isNotEmpty()) {
                                    Text("·", color = Color.White.copy(alpha = 0.5f))
                                    Text(p.address, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProjectStatItem("${roomState.rooms.size}", "Rooms", Icons.Default.Home, SkyBlue, Modifier.weight(1f))
                    ProjectStatItem("${photoState.photos.size}", "Photos", Icons.Default.Photo, SoftYellow, Modifier.weight(1f))
                    ProjectStatItem(
                        "${issueState.issues.count { it.status !in listOf("Fixed", "Closed") }}",
                        "Open Issues", Icons.Default.Warning, WarmRed, Modifier.weight(1f)
                    )
                }
            }

            // Action chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    item { ActionChip(Icons.Default.AddAPhoto, "Add Photo", NavyBlue, onNavigateToAddPhoto) }
                    item { ActionChip(Icons.Default.ReportProblem, "Issues", WarmRed, onNavigateToIssues) }
                    item { ActionChip(Icons.Default.Task, "Tasks", StatusOrange, onNavigateToTasks) }
                    item { ActionChip(Icons.Default.CompareArrows, "Before/After", StatusGreen, onNavigateToBeforeAfter) }
                }
            }

            // Rooms section
            item {
                SectionHeader(title = "Rooms", action = "Add Room", onAction = onNavigateToCreateRoom)
            }

            if (roomState.rooms.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Home,
                        title = "No Rooms Yet",
                        subtitle = "Add your first room to start tracking"
                    )
                }
            } else {
                items(roomState.rooms, key = { it.id }) { room ->
                    RoomListItem(
                        room = room,
                        onClick = { onNavigateToRoom(room.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // Notes
            if (project?.notes?.isNotEmpty() == true) {
                item {
                    SectionHeader(title = "Notes")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            project.notes,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectStatItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActionChip(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(label, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun RoomListItem(
    room: RoomEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LightWood.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                CategoryIcon(room.name, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(room.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(room.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (room.area > 0f) {
                    Text("${room.area} m²  ·  Floor ${room.floor}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SecondaryText.copy(alpha = 0.5f))
        }
    }
}
