package com.homerapa.repagom.ui.screens.beforeafter

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.homerapa.repagom.data.db.BeforeAfterEntity
import com.homerapa.repagom.data.db.PhotoEntity
import com.homerapa.repagom.ui.components.*
import com.homerapa.repagom.ui.theme.*
import com.homerapa.repagom.viewmodel.BeforeAfterViewModel
import com.homerapa.repagom.viewmodel.PhotoViewModel

@Composable
fun BeforeAfterScreen(
    projectId: Long,
    onBack: () -> Unit,
    beforeAfterViewModel: BeforeAfterViewModel = viewModel(),
    photoViewModel: PhotoViewModel = viewModel()
) {
    val baState by beforeAfterViewModel.uiState.collectAsStateWithLifecycle()
    val photoState by photoViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(projectId) {
        beforeAfterViewModel.loadForProject(projectId)
        photoViewModel.loadPhotosForProject(projectId)
    }

    var showCreateDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<BeforeAfterEntity?>(null) }

    val photos = photoState.photos
    val beforePhotos = photos.filter { it.label == "Before" }
    val afterPhotos = photos.filter { it.label == "After" }

    Scaffold(
        topBar = { AppTopBar(title = "Before / After", onBack = onBack) },
        floatingActionButton = {
            if (beforePhotos.isNotEmpty() && afterPhotos.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = StatusGreen,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Comparison")
                }
            }
        }
    ) { padding ->
        if (baState.pairs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    icon = Icons.Default.CompareArrows,
                    title = "No Comparisons Yet",
                    subtitle = "Add Before and After photos first, then create comparisons here."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(baState.pairs, key = { it.id }) { pair ->
                    val beforePhoto = photos.find { it.id == pair.beforePhotoId }
                    val afterPhoto = photos.find { it.id == pair.afterPhotoId }
                    BeforeAfterCard(
                        pair = pair,
                        beforePhoto = beforePhoto,
                        afterPhoto = afterPhoto,
                        onDelete = { showDeleteDialog = pair }
                    )
                }
                item { Spacer(Modifier.height(72.dp)) }
            }
        }
    }

    if (showCreateDialog) {
        CreateBeforeAfterDialog(
            beforePhotos = beforePhotos,
            afterPhotos = afterPhotos,
            onDismiss = { showCreateDialog = false },
            onCreate = { beforeId, afterId, note ->
                beforeAfterViewModel.createPair(
                    projectId = projectId,
                    roomId = null,
                    issueId = null,
                    beforePhotoId = beforeId,
                    afterPhotoId = afterId,
                    resultNote = note
                )
                showCreateDialog = false
            }
        )
    }

    showDeleteDialog?.let { pair ->
        ConfirmDeleteDialog(
            title = "Delete Comparison",
            message = "Remove this before/after comparison?",
            onConfirm = { beforeAfterViewModel.deletePair(pair); showDeleteDialog = null },
            onDismiss = { showDeleteDialog = null }
        )
    }
}

@Composable
private fun BeforeAfterCard(
    pair: BeforeAfterEntity,
    beforePhoto: PhotoEntity?,
    afterPhoto: PhotoEntity?,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Slider comparison
            BeforeAfterSlider(
                beforeUri = beforePhoto?.uri,
                afterUri = afterPhoto?.uri,
                modifier = Modifier.fillMaxWidth().height(260.dp)
            )

            // Info bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (pair.resultNote.isNotEmpty()) {
                        Text(pair.resultNote, style = MaterialTheme.typography.bodyMedium, color = DarkText)
                    }
                    Text(pair.createdAt.toFormattedDate(), style = MaterialTheme.typography.labelSmall, color = SecondaryText.copy(alpha = 0.6f))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = WarmRed.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun BeforeAfterSlider(
    beforeUri: String?,
    afterUri: String?,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableFloatStateOf(0.5f) }

    BoxWithConstraints(
        modifier = modifier.clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val sliderXDp = maxWidth * sliderPosition

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(widthPx) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        sliderPosition = (sliderPosition + dragAmount / widthPx.coerceAtLeast(1f)).coerceIn(0.02f, 0.98f)
                    }
                }
        ) {
            // Before image (full width behind)
            if (beforeUri != null) {
                AsyncImage(
                    model = beforeUri,
                    contentDescription = "Before",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(Modifier.fillMaxSize().background(NavyBlue.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Text("Before", color = NavyBlue, fontWeight = FontWeight.Bold)
                }
            }

            // After image (clipped to right portion of slider)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        clip = true
                        shape = object : Shape {
                            override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
                                return Outline.Rectangle(
                                    Rect(left = sliderPosition * size.width, top = 0f, right = size.width, bottom = size.height)
                                )
                            }
                        }
                    }
            ) {
                if (afterUri != null) {
                    AsyncImage(
                        model = afterUri,
                        contentDescription = "After",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(StatusGreen.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                        Text("After", color = StatusGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Divider line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .offset(x = sliderXDp - 1.5.dp)
                    .background(Color.White)
            )

            // Handle circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .offset(x = sliderXDp - 18.dp)
                    .align(Alignment.CenterStart)
                    .background(Color.White, CircleShape)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CompareArrows, contentDescription = null, tint = NavyBlue, modifier = Modifier.size(20.dp))
            }

            // Labels
            Text(
                "Before",
                modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                color = Color.White, style = MaterialTheme.typography.labelSmall
            )
            Text(
                "After",
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                color = Color.White, style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateBeforeAfterDialog(
    beforePhotos: List<PhotoEntity>,
    afterPhotos: List<PhotoEntity>,
    onDismiss: () -> Unit,
    onCreate: (Long, Long, String) -> Unit
) {
    var selectedBefore by remember { mutableStateOf<Long?>(null) }
    var selectedAfter by remember { mutableStateOf<Long?>(null) }
    var note by remember { mutableStateOf("") }
    var beforeMenuExpanded by remember { mutableStateOf(false) }
    var afterMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Comparison", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = beforeMenuExpanded, onExpandedChange = { beforeMenuExpanded = it }) {
                    OutlinedTextField(
                        value = beforePhotos.find { it.id == selectedBefore }?.note?.ifEmpty { "Photo #${selectedBefore}" } ?: "Select Before Photo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Before Photo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = beforeMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = beforeMenuExpanded, onDismissRequest = { beforeMenuExpanded = false }) {
                        beforePhotos.forEach { photo ->
                            DropdownMenuItem(
                                text = { Text(photo.note.ifEmpty { "Photo #${photo.id}" }) },
                                onClick = { selectedBefore = photo.id; beforeMenuExpanded = false }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = afterMenuExpanded, onExpandedChange = { afterMenuExpanded = it }) {
                    OutlinedTextField(
                        value = afterPhotos.find { it.id == selectedAfter }?.note?.ifEmpty { "Photo #${selectedAfter}" } ?: "Select After Photo",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("After Photo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = afterMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = afterMenuExpanded, onDismissRequest = { afterMenuExpanded = false }) {
                        afterPhotos.forEach { photo ->
                            DropdownMenuItem(
                                text = { Text(photo.note.ifEmpty { "Photo #${photo.id}" }) },
                                onClick = { selectedAfter = photo.id; afterMenuExpanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Result Note") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val before = selectedBefore
                    val after = selectedAfter
                    if (before != null && after != null) {
                        onCreate(before, after, note)
                    }
                },
                enabled = selectedBefore != null && selectedAfter != null
            ) { Text("Create", color = StatusGreen) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
