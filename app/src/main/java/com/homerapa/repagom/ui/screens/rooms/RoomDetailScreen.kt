package com.homerapa.repagom.ui.screens.rooms

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.homerapa.repagom.data.db.IssueEntity
import com.homerapa.repagom.data.db.PhotoEntity
import com.homerapa.repagom.ui.components.*
import com.homerapa.repagom.ui.theme.*
import com.homerapa.repagom.viewmodel.IssueViewModel
import com.homerapa.repagom.viewmodel.PhotoViewModel
import com.homerapa.repagom.viewmodel.RoomViewModel

@Composable
fun RoomDetailScreen(
    roomId: Long,
    onBack: () -> Unit,
    onNavigateToAddPhoto: (Long) -> Unit,
    onNavigateToPhoto: (Long, Long) -> Unit,
    onNavigateToCreateIssue: (Long) -> Unit,
    onNavigateToIssue: (Long) -> Unit,
    onNavigateToCreateTask: (Long) -> Unit,
    roomViewModel: RoomViewModel = viewModel(),
    photoViewModel: PhotoViewModel = viewModel(),
    issueViewModel: IssueViewModel = viewModel()
) {
    val roomState by roomViewModel.uiState.collectAsStateWithLifecycle()
    val photoState by photoViewModel.uiState.collectAsStateWithLifecycle()
    val issueState by issueViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(roomId) {
        roomViewModel.loadRoom(roomId)
        photoViewModel.loadPhotosForRoom(roomId)
        issueViewModel.loadIssuesForRoom(roomId)
    }

    val room = roomState.selectedRoom
    var showDeleteIssue by remember { mutableStateOf<IssueEntity?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = room?.name ?: "Room",
                onBack = onBack,
                actions = {
                    room?.let {
                        IconButton(onClick = { onNavigateToAddPhoto(it.projectId) }) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Add Photo", tint = Color.White)
                        }
                        IconButton(onClick = { onNavigateToCreateIssue(it.projectId) }) {
                            Icon(Icons.Default.ReportProblem, contentDescription = "Add Issue", tint = Color.White)
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Room Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    if (room?.coverPhotoUri != null) {
                        AsyncImage(
                            model = room.coverPhotoUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                    } else {
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.linearGradient(listOf(WarmBrown, LightWood))
                            )
                        )
                    }
                    Column(
                        modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                    ) {
                        room?.let { r ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                CategoryIcon(r.name, modifier = Modifier.size(18.dp))
                                Text(r.category, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                                if (r.area > 0f) {
                                    Text("·", color = Color.White.copy(alpha = 0.5f))
                                    Text("${r.area} m²", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                                }
                                Text("·", color = Color.White.copy(alpha = 0.5f))
                                Text("Floor ${r.floor}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }

            // Stats row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        value = photoState.photos.size.toString(),
                        label = "Photos",
                        icon = Icons.Default.Photo,
                        backgroundColor = NavyBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = issueState.issues.count { it.status !in listOf("Fixed", "Closed") }.toString(),
                        label = "Open Issues",
                        icon = Icons.Default.Warning,
                        backgroundColor = WarmRed,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        value = issueState.issues.count { it.status in listOf("Fixed", "Closed") }.toString(),
                        label = "Fixed",
                        icon = Icons.Default.CheckCircle,
                        backgroundColor = StatusGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    room?.let {
                        FilledTonalButton(
                            onClick = { onNavigateToAddPhoto(it.projectId) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = NavyBlue.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Photo", color = NavyBlue)
                        }
                        FilledTonalButton(
                            onClick = { onNavigateToCreateIssue(it.projectId) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = WarmRed.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = WarmRed, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Issue", color = WarmRed)
                        }
                        FilledTonalButton(
                            onClick = { onNavigateToCreateTask(it.projectId) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = StatusOrange.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Task, contentDescription = null, tint = StatusOrange, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Task", color = StatusOrange)
                        }
                    }
                }
            }

            // Photo Timeline
            if (photoState.photos.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Photo Timeline",
                        action = "Add Photo",
                        onAction = { room?.let { onNavigateToAddPhoto(it.projectId) } }
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(photoState.photos) { photo ->
                            RoomPhotoCard(
                                photo = photo,
                                onClick = { room?.let { onNavigateToPhoto(photo.id, it.projectId) } }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Open Issues
            val openIssues = issueState.issues.filter { it.status !in listOf("Fixed", "Closed") }
            if (openIssues.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Open Issues",
                        action = "Add Issue",
                        onAction = { room?.let { onNavigateToCreateIssue(it.projectId) } }
                    )
                }
                items(openIssues, key = { "oi_${it.id}" }) { issue ->
                    RoomIssueCard(
                        issue = issue,
                        onClick = { onNavigateToIssue(issue.id) },
                        onDelete = { showDeleteIssue = issue },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // Fixed Issues
            val fixedIssues = issueState.issues.filter { it.status in listOf("Fixed", "Closed") }
            if (fixedIssues.isNotEmpty()) {
                item {
                    SectionHeader(title = "Fixed Issues")
                }
                items(fixedIssues, key = { "fi_${it.id}" }) { issue ->
                    RoomIssueCard(
                        issue = issue,
                        onClick = { onNavigateToIssue(issue.id) },
                        onDelete = { showDeleteIssue = issue },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // Notes
            if (room?.notes?.isNotEmpty() == true) {
                item {
                    SectionHeader(title = "Notes")
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SkyBlueLight)
                    ) {
                        Text(room.notes, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    showDeleteIssue?.let { issue ->
        ConfirmDeleteDialog(
            title = "Delete Issue",
            message = "Delete \"${issue.title}\"?",
            onConfirm = { issueViewModel.deleteIssue(issue); showDeleteIssue = null },
            onDismiss = { showDeleteIssue = null }
        )
    }
}

@Composable
private fun RoomPhotoCard(photo: PhotoEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(100.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = photo.uri,
                contentDescription = photo.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier.align(Alignment.TopStart).padding(4.dp)
            ) {
                LabelTag(photo.label)
            }
        }
    }
}

@Composable
private fun RoomIssueCard(
    issue: IssueEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                    .background(if (issue.status in listOf("Fixed","Closed")) StatusGreen.copy(alpha = 0.15f) else WarmRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (issue.status in listOf("Fixed","Closed")) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (issue.status in listOf("Fixed","Closed")) StatusGreen else WarmRed,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(issue.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = DarkText)
                Text(issue.category, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            }
            PriorityChip(issue.priority)
            Spacer(Modifier.width(4.dp))
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(16.dp))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Delete", color = WarmRed) }, onClick = { menuExpanded = false; onDelete() })
                }
            }
        }
    }
}
