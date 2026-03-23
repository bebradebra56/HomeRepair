package com.homerapa.repagom.ui.screens.projects

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.homerapa.repagom.ui.components.AppTopBar
import com.homerapa.repagom.ui.components.PropertyTypeIcon
import com.homerapa.repagom.ui.theme.*
import com.homerapa.repagom.viewmodel.ProjectViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val propertyTypes = listOf("Apartment", "House", "Office", "Single Room", "Custom")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    projectId: Long? = null,
    onBack: () -> Unit,
    onProjectCreated: () -> Unit,
    viewModel: ProjectViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditing = projectId != null

    var name by remember { mutableStateOf("") }
    var propertyType by remember { mutableStateOf("Apartment") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var coverPhotoUri by remember { mutableStateOf<String?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var nameError by remember { mutableStateOf(false) }
    var typeMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(projectId, uiState.selectedProject) {
        if (isEditing && projectId != null) {
            viewModel.loadProject(projectId)
        }
    }

    LaunchedEffect(uiState.selectedProject) {
        if (isEditing) {
            uiState.selectedProject?.let { p ->
                name = p.name
                propertyType = p.propertyType
                address = p.address
                notes = p.notes
                coverPhotoUri = p.coverPhotoUri
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { u ->
            scope.launch {
                val persistent = withContext(Dispatchers.IO) {
                    context.copyImageUriToAppStorage(u, "proj")
                }
                persistent?.let { coverPhotoUri = it }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) coverPhotoUri = cameraUri?.toString()
    }

    fun launchCamera() {
        val file = File.createTempFile("proj_", ".jpg", context.getExternalFilesDir("Pictures"))
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraUri = uri
        cameraLauncher.launch(uri)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (isEditing) "Edit Project" else "New Project",
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cover Photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SkyBlue.copy(alpha = 0.2f))
                    .border(2.dp, NavyBlue.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (coverPhotoUri != null) {
                    AsyncImage(
                        model = coverPhotoUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = NavyBlue.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Add Cover Photo", color = SecondaryText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Gallery")
                }
                OutlinedButton(onClick = { launchCamera() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Camera")
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("Project Name *") },
                isError = nameError,
                supportingText = if (nameError) {{ Text("Required") }} else null,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Property Type
            ExposedDropdownMenuBox(
                expanded = typeMenuExpanded,
                onExpandedChange = { typeMenuExpanded = it }
            ) {
                OutlinedTextField(
                    value = propertyType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Property Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    leadingIcon = { PropertyTypeIcon(propertyType, modifier = Modifier.size(20.dp)) },
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                    propertyTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = { propertyType = type; typeMenuExpanded = false }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address or Label") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    if (isEditing && uiState.selectedProject != null) {
                        viewModel.updateProject(
                            uiState.selectedProject!!.copy(
                                name = name,
                                propertyType = propertyType,
                                address = address,
                                notes = notes,
                                coverPhotoUri = coverPhotoUri
                            )
                        )
                    } else {
                        viewModel.createProject(name, propertyType, address, System.currentTimeMillis(), notes, coverPhotoUri)
                    }
                    onProjectCreated()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Icon(
                    if (isEditing) Icons.Default.Save else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isEditing) "Save Changes" else "Create Project",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
