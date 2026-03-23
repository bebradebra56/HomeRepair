package com.homerapa.repagom.gefr.presentation.pushhandler

import android.os.Bundle
import android.util.Log
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication

class HomeRepairPushHandler {
    fun homeRepairHandlePush(extras: Bundle?) {
        Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "Extras from Push = ${extras?.keySet()}")
        if (extras != null) {
            val map = homeRepairBundleToMap(extras)
            Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "Map from Push = $map")
            map?.let {
                if (map.containsKey("url")) {
                    HomeRepairApplication.HOME_REPAIR_FB_LI = map["url"]
                    Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "UrlFromActivity = $map")
                }
            }
        } else {
            Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "Push data no!")
        }
    }

    private fun homeRepairBundleToMap(extras: Bundle): Map<String, String?>? {
        val map: MutableMap<String, String?> = HashMap()
        val ks = extras.keySet()
        val iterator: Iterator<String> = ks.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            map[key] = extras.getString(key)
        }
        return map
    }

}