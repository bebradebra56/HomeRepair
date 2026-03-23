package com.homerapa.repagom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.homerapa.repagom.data.preferences.UserPreferences
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val prefsRepo = (app as HomeRepairApplication).preferencesRepository
    private val dbRepo = (app as HomeRepairApplication).repository

    val userPreferences: StateFlow<UserPreferences> = prefsRepo.preferences
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    fun setOnboardingComplete() = viewModelScope.launch {
        prefsRepo.setOnboardingComplete(true)
    }

    fun setActiveProject(projectId: Long) = viewModelScope.launch {
        prefsRepo.setActiveProject(projectId)
        dbRepo.logActivity(projectId, "Project Activated", "Set as active project")
    }

    fun updateProfile(name: String, email: String) = viewModelScope.launch {
        prefsRepo.updateProfile(name, email)
    }
}
