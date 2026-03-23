package com.homerapa.repagom.gefr.data.shar

import android.content.Context
import androidx.core.content.edit

class HomeRepairSharedPreference(context: Context) {
    private val homeRepairPrefs = context.getSharedPreferences("homeRepairSharedPrefsAb", Context.MODE_PRIVATE)

    var homeRepairSavedUrl: String
        get() = homeRepairPrefs.getString(HOME_REPAIR_SAVED_URL, "") ?: ""
        set(value) = homeRepairPrefs.edit { putString(HOME_REPAIR_SAVED_URL, value) }

    var homeRepairExpired : Long
        get() = homeRepairPrefs.getLong(HOME_REPAIR_EXPIRED, 0L)
        set(value) = homeRepairPrefs.edit { putLong(HOME_REPAIR_EXPIRED, value) }

    var homeRepairAppState: Int
        get() = homeRepairPrefs.getInt(HOME_REPAIR_APPLICATION_STATE, 0)
        set(value) = homeRepairPrefs.edit { putInt(HOME_REPAIR_APPLICATION_STATE, value) }

    var homeRepairNotificationRequest: Long
        get() = homeRepairPrefs.getLong(HOME_REPAIR_NOTIFICAITON_REQUEST, 0L)
        set(value) = homeRepairPrefs.edit { putLong(HOME_REPAIR_NOTIFICAITON_REQUEST, value) }


    var homeRepairNotificationState:Int
        get() = homeRepairPrefs.getInt(HOME_REPAIR_NOTIFICATION_STATE, 0)
        set(value) = homeRepairPrefs.edit { putInt(HOME_REPAIR_NOTIFICATION_STATE, value) }

    companion object {
        private const val HOME_REPAIR_NOTIFICATION_STATE = "homeRepairNotificationState"
        private const val HOME_REPAIR_SAVED_URL = "homeRepairSavedUrl"
        private const val HOME_REPAIR_EXPIRED = "homeRepairExpired"
        private const val HOME_REPAIR_APPLICATION_STATE = "homeRepairApplicationState"
        private const val HOME_REPAIR_NOTIFICAITON_REQUEST = "homeRepairNotificationRequest"
    }
}