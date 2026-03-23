package com.homerapa.repagom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homerapa.repagom.data.db.BeforeAfterEntity
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BeforeAfterUiState(
    val pairs: List<BeforeAfterEntity> = emptyList(),
    val isLoading: Boolean = true
)

class BeforeAfterViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = (app as HomeRepairApplication).repository

    private val _uiState = MutableStateFlow(BeforeAfterUiState())
    val uiState: StateFlow<BeforeAfterUiState> = _uiState.asStateFlow()

    fun loadAll() = viewModelScope.launch {
        repository.getAllBeforeAfter().collect { pairs ->
            _uiState.update { it.copy(pairs = pairs, isLoading = false) }
        }
    }

    fun loadForProject(projectId: Long) = viewModelScope.launch {
        repository.getBeforeAfterForProject(projectId).collect { pairs ->
            _uiState.update { it.copy(pairs = pairs, isLoading = false) }
        }
    }

    fun loadForRoom(roomId: Long) = viewModelScope.launch {
        repository.getBeforeAfterForRoom(roomId).collect { pairs ->
            _uiState.update { it.copy(pairs = pairs, isLoading = false) }
        }
    }

    fun createPair(
        projectId: Long,
        roomId: Long?,
        issueId: Long?,
        beforePhotoId: Long,
        afterPhotoId: Long,
        resultNote: String
    ) = viewModelScope.launch {
        val pair = BeforeAfterEntity(
            projectId = projectId,
            roomId = roomId,
            issueId = issueId,
            beforePhotoId = beforePhotoId,
            afterPhotoId = afterPhotoId,
            resultNote = resultNote
        )
        repository.insertBeforeAfter(pair)
        repository.logActivity(projectId, "Before/After Added", "Added comparison")
    }

    fun deletePair(pair: BeforeAfterEntity) = viewModelScope.launch {
        repository.deleteBeforeAfter(pair)
    }
}
