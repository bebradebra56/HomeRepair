package com.homerapa.repagom.ui.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import com.homerapa.repagom.ui.components.*
import com.homerapa.repagom.ui.theme.*
import com.homerapa.repagom.viewmodel.AppViewModel
import com.homerapa.repagom.viewmodel.ReportsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    appViewModel: AppViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToActivityHistory: () -> Unit,
    reportsViewModel: ReportsViewModel = viewModel()
) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as HomeRepairApplication }
    val scope = rememberCoroutineScope()

    val prefs by appViewModel.userPreferences.collectAsStateWithLifecycle()
    val stats by reportsViewModel.uiState.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var exportSnackbar by remember { mutableStateOf<String?>(null) }

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val csv = buildString {
                        appendLine("# Home Repair Export")
                        appendLine("# Generated: ${dateFormat.format(Date())}")
                        appendLine()

                        appendLine("=== PROJECTS ===")
                        appendLine("ID,Name,Type,Address,Notes,Created")
                        app.repository.getAllProjects().first().forEach { p ->
                            appendLine("${p.id},\"${p.name}\",\"${p.propertyType}\",\"${p.address.replace("\"", "'")}\",\"${p.notes.replace("\"", "'")}\",${dateFormat.format(Date(p.createdAt))}")
                        }
                        appendLine()

                        appendLine("=== ISSUES ===")
                        appendLine("ID,Title,Category,Priority,Status,Description,Created")
                        app.repository.getAllIssues().first().forEach { i ->
                            appendLine("${i.id},\"${i.title.replace("\"", "'")}\",\"${i.category}\",\"${i.priority}\",\"${i.status}\",\"${i.description.replace("\"", "'")}\",${dateFormat.format(Date(i.createdAt))}")
                        }
                        appendLine()

                        appendLine("=== TASKS ===")
                        appendLine("ID,Title,Priority,Status,AssignedTo,CostEstimate,Created")
                        app.repository.getAllTasks().first().forEach { t ->
                            appendLine("${t.id},\"${t.title.replace("\"", "'")}\",\"${t.priority}\",\"${t.status}\",\"${t.assignedTo.replace("\"", "'")}\",${t.costEstimate},${dateFormat.format(Date(t.createdAt))}")
                        }
                    }
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(csv.toByteArray(Charsets.UTF_8))
                    }
                    exportSnackbar = "Exported successfully"
                } catch (e: Exception) {
                    exportSnackbar = "Export failed: ${e.message}"
                }
            }
        }
    }

    val initials = prefs.userName.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2).joinToString("")
        .ifEmpty { "U" }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(exportSnackbar) {
        exportSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            exportSnackbar = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(NavyBlue, NavyBlueMid)))
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(ConstructionYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initials.uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = DarkText,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    prefs.userName.ifEmpty { "Your Name" },
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    prefs.userEmail.ifEmpty { "your@email.com" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = SkyBlue.copy(alpha = 0.8f)
                )

                Spacer(Modifier.height(20.dp))

                OutlinedButton(
                    onClick = {
                        editName = prefs.userName
                        editEmail = prefs.userEmail
                        showEditDialog = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Edit Profile")
                }
            }
        }

        // Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                value = stats.totalPhotos.toString(),
                label = "Photos",
                icon = Icons.Default.Photo,
                backgroundColor = NavyBlue,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = stats.totalIssues.toString(),
                label = "Issues",
                icon = Icons.Default.ReportProblem,
                backgroundColor = WarmRed,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                value = stats.fixedIssues.toString(),
                label = "Fixed",
                icon = Icons.Default.CheckCircle,
                backgroundColor = StatusGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Action Menu
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "Settings",
                    subtitle = "Theme, preferences & more",
                    onClick = onNavigateToSettings
                )
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp),
                    color = MaterialTheme.colorScheme.outlineVariant)
                ProfileMenuItem(
                    icon = Icons.Default.History,
                    title = "Activity History",
                    subtitle = "View all your recent actions",
                    onClick = onNavigateToActivityHistory
                )
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp),
                    color = MaterialTheme.colorScheme.outlineVariant)
                ProfileMenuItem(
                    icon = Icons.Default.FileDownload,
                    title = "Export Data",
                    subtitle = "Save all data as CSV file",
                    onClick = {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                        csvLauncher.launch("home_repair_$timestamp.csv")
                    }
                )
            }
        }

        // App info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                ProfileMenuItem(
                    icon = Icons.Default.Info,
                    title = "About Home Repair",
                    subtitle = "Version 1.0 — Renovation Inspector",
                    onClick = { showAboutDialog = true }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 64.dp),
                    color = MaterialTheme.colorScheme.outlineVariant)
                ProfileMenuItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "Tap to read",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://homereppair.com/privacy-policy.html"))
                        context.startActivity(intent)
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    } // end Column
    } // end Scaffold

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    appViewModel.updateProfile(editName, editEmail)
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel") } }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Default.Construction, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = {
                Text("Home Repair", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Version 1.0 — Renovation Inspector",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Home Repair is a photo-centric inspection utility for renovations, apartment acceptance, and repair tracking.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Create projects for apartments, houses or rooms. Upload photos, mark defects, track issues with priorities, store before/after comparisons, and manage tasks — all in one place.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.PhotoCamera, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Text("Photo-first defect tracking", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CompareArrows, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Text("Before / After comparisons", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Task, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Text("Tasks, issues & activity history", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        "All data is stored locally on your device — no account required.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }
}
