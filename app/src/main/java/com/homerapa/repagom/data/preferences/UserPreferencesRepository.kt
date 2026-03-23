package com.homerapa.repagom.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserPreferences(
    val isOnboardingComplete: Boolean = false,
    val activeProjectId: Long = -1L,
    val userName: String = "",
    val userEmail: String = "",
    val themeMode: String = "system",
    val photoQuality: String = "high",
    val defaultRoomSort: String = "name",
    val cloudSyncEnabled: Boolean = false
)

class UserPreferencesRepository(context: Context) {

    private val dataStore = context.dataStore

    private object Keys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val ACTIVE_PROJECT_ID = longPreferencesKey("active_project_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PHOTO_QUALITY = stringPreferencesKey("photo_quality")
        val DEFAULT_ROOM_SORT = stringPreferencesKey("default_room_sort")
        val CLOUD_SYNC = booleanPreferencesKey("cloud_sync")
    }

    val preferences: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }
        .map { prefs ->
            UserPreferences(
                isOnboardingComplete = prefs[Keys.ONBOARDING_COMPLETE] ?: false,
                activeProjectId = prefs[Keys.ACTIVE_PROJECT_ID] ?: -1L,
                userName = prefs[Keys.USER_NAME] ?: "",
                userEmail = prefs[Keys.USER_EMAIL] ?: "",
                themeMode = prefs[Keys.THEME_MODE] ?: "system",
                photoQuality = prefs[Keys.PHOTO_QUALITY] ?: "high",
                defaultRoomSort = prefs[Keys.DEFAULT_ROOM_SORT] ?: "name",
                cloudSyncEnabled = prefs[Keys.CLOUD_SYNC] ?: false
            )
        }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setActiveProject(projectId: Long) {
        dataStore.edit { it[Keys.ACTIVE_PROJECT_ID] = projectId }
    }

    suspend fun updateProfile(name: String, email: String) {
        dataStore.edit {
            it[Keys.USER_NAME] = name
            it[Keys.USER_EMAIL] = email
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setPhotoQuality(quality: String) {
        dataStore.edit { it[Keys.PHOTO_QUALITY] = quality }
    }

    suspend fun setDefaultRoomSort(sort: String) {
        dataStore.edit { it[Keys.DEFAULT_ROOM_SORT] = sort }
    }

    suspend fun setCloudSync(enabled: Boolean) {
        dataStore.edit { it[Keys.CLOUD_SYNC] = enabled }
    }
}
