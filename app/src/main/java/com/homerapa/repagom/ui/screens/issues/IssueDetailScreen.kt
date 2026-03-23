package com.homerapa.repagom.ui.screens.issues

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homerapa.repagom.util.copyImageUriToAppStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import coil.compose.AsyncImage
import com.homerapa.repagom.ui.components.*
import com.homerapa.repagom.ui.theme.*
import com.homerapa.repagom.viewmodel.IssueViewModel
import java.io.File

@Composable
fun IssueDetailScreen(
    issueId: Long,
    onBack: () -> Unit,
    onNavigateToPhoto: (Long, Long) -> Unit,
    onNavigateToCreateTask: (Long, Long) -> Unit,
    viewModel: IssueViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(issueId) {
        viewModel.loadIssue(issueId)
    }

    val issue = uiState.selectedIssue
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMarkFixedDialog by remember { mutableStateOf(false) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var afterPhotoUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(issue) {
        afterPhotoUri = issue?.afterPhotoUri
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { u ->
            val iss = issue
            scope.launch {
                val persistent = withContext(Dispatchers.IO) {
                    context.copyImageUriToAppStorage(u, "after")
                }
                persistent?.let { newUri ->
                    afterPhotoUri = newUri
                    iss?.let { viewModel.updateIssue(it.copy(afterPhotoUri = newUri)) }
                }
            }
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            afterPhotoUri = cameraUri?.toString()
            issue?.let { iss -> viewModel.updateIssue(iss.copy(afterPhotoUri = cameraUri?.toString())) }
        }
    }

    fun launchCamera() {
        val file = File.createTempFile("after_", ".jpg", context.getExternalFilesDir("Pictures"))
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraUri = uri
        cameraLauncher.launch(uri)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Issue Detail",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.7f))
                    }
                }
            )
        }
    ) { padding ->
        if (issue == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CategoryIcon(issue.category, modifier = Modifier.size(20.dp))
                        Text(issue.category, style = MaterialTheme.typography.labelMedium, color = SecondaryText)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(issue.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DarkText)
                    if (issue.description.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(issue.description, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PriorityChip(issue.priority)
                        StatusChip(issue.status)
                    }
                }
            }

            // Details
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow(Icons.Default.CalendarToday, "Created", issue.createdAt.toFormattedDate())
                    DetailRow(Icons.Default.Update, "Updated", issue.updatedAt.toFormattedDate())
                    if (issue.assignedTo.isNotEmpty()) {
                        DetailRow(Icons.Default.Person, "Assigned To", issue.assignedTo)
                    }
                    if (issue.deadline != null) {
                        DetailRow(Icons.Default.Schedule, "Deadline", issue.deadline.toFormattedDate())
                    }
                    if (issue.notes.isNotEmpty()) {
                        DetailRow(Icons.Default.Notes, "Notes", issue.notes)
                    }
                }
            }

            // Linked photo
            if (issue.photoId != null) {
                SectionHeader(title = "Linked Photo")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 16.dp)
                        .clickable { onNavigateToPhoto(issue.photoId, issue.projectId) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Photo, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(48.dp))
                        Text(
                            "View Photo #${issue.photoId}",
                            color = NavyBlue,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // After Photo
            SectionHeader(title = "After Photo")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (afterPhotoUri != null) Color.Transparent else StatusGreen.copy(alpha = 0.05f)
                )
            ) {
                if (afterPhotoUri != null) {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        AsyncImage(
                            model = afterPhotoUri,
                            contentDescription = "After photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))
                        )
                        Surface(
                            shape = RoundedCornerShape(topEnd = 16.dp, bottomStart = 8.dp),
                            color = StatusGreen,
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text("After", color = Color.White, style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = StatusGreen.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No after photo yet", color = SecondaryText, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Gallery")
                }
                OutlinedButton(
                    onClick = { launchCamera() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Camera")
                }
            }

            // Actions
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (issue.status !in listOf("Fixed", "Closed")) {
                    Button(
                        onClick = { showMarkFixedDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Mark as Fixed", fontWeight = FontWeight.Bold)
                    }
                }
                OutlinedButton(
                    onClick = { onNavigateToCreateTask(issue.projectId, issue.id) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Task, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Create Task for This Issue")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            title = "Delete Issue",
            message = "Delete \"${issue?.title}\"?",
            onConfirm = { issue?.let { viewModel.deleteIssue(it) }; onBack() },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showMarkFixedDialog) {
        AlertDialog(
            onDismissRequest = { showMarkFixedDialog = false },
            title = { Text("Mark as Fixed") },
            text = { Text("Mark \"${issue?.title}\" as fixed?") },
            confirmButton = {
                TextButton(onClick = {
                    issue?.let { viewModel.markFixed(it, afterPhotoUri) }
                    showMarkFixedDialog = false
                }) { Text("Mark Fixed", color = StatusGreen) }
            },
            dismissButton = { TextButton(onClick = { showMarkFixedDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SecondaryText.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = DarkText)
        }
    }
}
