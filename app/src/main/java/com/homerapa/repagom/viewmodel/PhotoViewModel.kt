package com.homerapa.repagom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homerapa.repagom.data.db.PhotoEntity
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PhotoUiState(
    val photos: List<PhotoEntity> = emptyList(),
    val recentPhotos: List<PhotoEntity> = emptyList(),
    val selectedPhoto: PhotoEntity? = null,
    val isLoading: Boolean = true
)

class PhotoViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = (app as HomeRepairApplication).repository

    private val _uiState = MutableStateFlow(PhotoUiState())
    val uiState: StateFlow<PhotoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getRecentPhotos().collect { photos ->
                _uiState.update { it.copy(recentPhotos = photos, isLoading = false) }
            }
        }
    }

    fun loadPhotosForProject(projectId: Long) = viewModelScope.launch {
        repository.getPhotosForProject(projectId).collect { photos ->
            _uiState.update { it.copy(photos = photos, isLoading = false) }
        }
    }

    fun loadPhotosForRoom(roomId: Long) = viewModelScope.launch {
        repository.getPhotosForRoom(roomId).collect { photos ->
            _uiState.update { it.copy(photos = photos, isLoading = false) }
        }
    }

    fun loadPhoto(id: Long) = viewModelScope.launch {
        val photo = repository.getPhoto(id)
        _uiState.update { it.copy(selectedPhoto = photo) }
    }

    fun addPhoto(
        projectId: Long,
        roomId: Long?,
        uri: String,
        label: String,
        note: String,
        takenAt: Long
    ) = viewModelScope.launch {
        val photo = PhotoEntity(
            projectId = projectId,
            roomId = roomId,
            uri = uri,
            label = label,
            note = note,
            takenAt = takenAt
        )
        repository.insertPhoto(photo)
        repository.logActivity(projectId, "Photo Added", "Added $label photo")
    }

    fun updatePhoto(photo: PhotoEntity) = viewModelScope.launch {
        repository.updatePhoto(photo)
    }

    fun deletePhoto(photo: PhotoEntity) = viewModelScope.launch {
        repository.deletePhoto(photo)
        repository.logActivity(photo.projectId, "Photo Deleted", "Deleted photo")
    }
}
