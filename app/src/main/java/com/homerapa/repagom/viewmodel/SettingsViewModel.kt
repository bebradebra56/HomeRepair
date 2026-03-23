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

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val prefsRepo = (app as HomeRepairApplication).preferencesRepository

    val preferences: StateFlow<UserPreferences> = prefsRepo.preferences
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    fun setThemeMode(mode: String) = viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    fun setPhotoQuality(quality: String) = viewModelScope.launch { prefsRepo.setPhotoQuality(quality) }
    fun setDefaultRoomSort(sort: String) = viewModelScope.launch { prefsRepo.setDefaultRoomSort(sort) }
    fun setCloudSync(enabled: Boolean) = viewModelScope.launch { prefsRepo.setCloudSync(enabled) }
    fun updateProfile(name: String, email: String) = viewModelScope.launch { prefsRepo.updateProfile(name, email) }
}
