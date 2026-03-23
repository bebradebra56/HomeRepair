package com.homerapa.repagom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homerapa.repagom.data.db.RoomEntity
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RoomUiState(
    val rooms: List<RoomEntity> = emptyList(),
    val selectedRoom: RoomEntity? = null,
    val isLoading: Boolean = true
)

class RoomViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = (app as HomeRepairApplication).repository

    private val _uiState = MutableStateFlow(RoomUiState())
    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()

    fun loadRoomsForProject(projectId: Long) = viewModelScope.launch {
        repository.getRoomsForProject(projectId).collect { rooms ->
            _uiState.update { it.copy(rooms = rooms, isLoading = false) }
        }
    }

    fun loadRoom(id: Long) = viewModelScope.launch {
        repository.getRoom(id).collect { room ->
            _uiState.update { it.copy(selectedRoom = room) }
        }
    }

    fun createRoom(
        projectId: Long,
        name: String,
        category: String,
        area: Float,
        floor: Int,
        notes: String,
        coverPhotoUri: String?
    ) = viewModelScope.launch {
        val room = RoomEntity(
            projectId = projectId,
            name = name,
            category = category,
            area = area,
            floor = floor,
            notes = notes,
            coverPhotoUri = coverPhotoUri
        )
        repository.insertRoom(room)
        repository.logActivity(projectId, "Room Added", "Added room: $name")
    }

    fun updateRoom(room: RoomEntity) = viewModelScope.launch {
        repository.updateRoom(room.copy(updatedAt = System.currentTimeMillis()))
        repository.logActivity(room.projectId, "Room Updated", "Updated: ${room.name}")
    }

    fun deleteRoom(room: RoomEntity) = viewModelScope.launch {
        repository.deleteRoom(room)
        repository.logActivity(room.projectId, "Room Deleted", "Deleted: ${room.name}")
    }
}
