package com.homerapa.repagom.gefr.data.utils

import android.util.Log
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class HomeRepairPushToken {

    suspend fun homeRepairGetToken(
        homeRepairMaxAttempts: Int = 3,
        homeRepairDelayMs: Long = 1500
    ): String {

        repeat(homeRepairMaxAttempts - 1) {
            try {
                val homeRepairToken = FirebaseMessaging.getInstance().token.await()
                return homeRepairToken
            } catch (e: Exception) {
                Log.e(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "Token error (attempt ${it + 1}): ${e.message}")
                delay(homeRepairDelayMs)
            }
        }

        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "Token error final: ${e.message}")
            "null"
        }
    }


}