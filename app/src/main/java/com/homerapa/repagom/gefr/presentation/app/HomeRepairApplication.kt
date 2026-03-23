package com.homerapa.repagom.gefr.presentation.app

import android.app.Application
import android.util.Log
import android.view.WindowManager
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.appsflyer.deeplink.DeepLink
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import com.homerapa.repagom.data.db.AppDatabase
import com.homerapa.repagom.data.preferences.UserPreferencesRepository
import com.homerapa.repagom.data.repository.AppRepository
import com.homerapa.repagom.gefr.presentation.di.homeRepairModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query


sealed interface HomeRepairAppsFlyerState {
    data object HomeRepairDefault : HomeRepairAppsFlyerState
    data class HomeRepairSuccess(val homeRepairData: MutableMap<String, Any>?) :
        HomeRepairAppsFlyerState

    data object HomeRepairError : HomeRepairAppsFlyerState
}

interface HomeRepairAppsApi {
    @Headers("Content-Type: application/json")
    @GET(HOME_REPAIR_LIN)
    fun homeRepairGetClient(
        @Query("devkey") devkey: String,
        @Query("device_id") deviceId: String,
    ): Call<MutableMap<String, Any>?>
}

private const val HOME_REPAIR_APP_DEV = "nyx8jdREefX4jaWnVGmdYd"
private const val HOME_REPAIR_LIN = "com.homerapa.repagom"

class HomeRepairApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AppRepository(database) }
    val preferencesRepository by lazy { UserPreferencesRepository(this) }

    private var homeRepairIsResumed = false
    ///////
    private var homeRepairConversionTimeoutJob: Job? = null
    private var homeRepairDeepLinkData: MutableMap<String, Any>? = null

    override fun onCreate() {
        super.onCreate()

        val appsflyer = AppsFlyerLib.getInstance()
        homeRepairSetDebufLogger(appsflyer)
        homeRepairMinTimeBetween(appsflyer)

        AppsFlyerLib.getInstance().subscribeForDeepLink(object : DeepLinkListener {
            override fun onDeepLinking(p0: DeepLinkResult) {
                when (p0.status) {
                    DeepLinkResult.Status.FOUND -> {
                        homeRepairExtractDeepMap(p0.deepLink)
                        Log.d(HOME_REPAIR_MAIN_TAG, "onDeepLinking found: ${p0.deepLink}")

                    }

                    DeepLinkResult.Status.NOT_FOUND -> {
                        Log.d(HOME_REPAIR_MAIN_TAG, "onDeepLinking not found: ${p0.deepLink}")
                    }

                    DeepLinkResult.Status.ERROR -> {
                        Log.d(HOME_REPAIR_MAIN_TAG, "onDeepLinking error: ${p0.error}")
                    }
                }
            }

        })


        appsflyer.init(
            HOME_REPAIR_APP_DEV,
            object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                    //////////
                    homeRepairConversionTimeoutJob?.cancel()
                    Log.d(HOME_REPAIR_MAIN_TAG, "onConversionDataSuccess: $p0")

                    val afStatus = p0?.get("af_status")?.toString() ?: "null"
                    if (afStatus == "Organic") {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                delay(5000)
                                val api = homeRepairGetApi(
                                    "https://gcdsdk.appsflyer.com/install_data/v4.0/",
                                    null
                                )
                                val response = api.homeRepairGetClient(
                                    devkey = HOME_REPAIR_APP_DEV,
                                    deviceId = homeRepairGetAppsflyerId()
                                ).awaitResponse()

                                val resp = response.body()
                                Log.d(HOME_REPAIR_MAIN_TAG, "After 5s: $resp")
                                if (resp?.get("af_status") == "Organic" || resp?.get("af_status") == null) {
                                    homeRepairResume(
                                        HomeRepairAppsFlyerState.HomeRepairError
                                    )
                                } else {
                                    homeRepairResume(
                                        HomeRepairAppsFlyerState.HomeRepairSuccess(
                                            resp
                                        )
                                    )
                                }
                            } catch (d: Exception) {
                                Log.d(HOME_REPAIR_MAIN_TAG, "Error: ${d.message}")
                                homeRepairResume(HomeRepairAppsFlyerState.HomeRepairError)
                            }
                        }
                    } else {
                        homeRepairResume(
                            HomeRepairAppsFlyerState.HomeRepairSuccess(
                                p0
                            )
                        )
                    }
                }

                override fun onConversionDataFail(p0: String?) {
                    /////////
                    homeRepairConversionTimeoutJob?.cancel()
                    Log.d(HOME_REPAIR_MAIN_TAG, "onConversionDataFail: $p0")
                    homeRepairResume(HomeRepairAppsFlyerState.HomeRepairError)
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                    Log.d(HOME_REPAIR_MAIN_TAG, "onAppOpenAttribution")
                }

                override fun onAttributionFailure(p0: String?) {
                    Log.d(HOME_REPAIR_MAIN_TAG, "onAttributionFailure: $p0")
                }
            },
            this
        )

        appsflyer.start(this, HOME_REPAIR_APP_DEV, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d(HOME_REPAIR_MAIN_TAG, "AppsFlyer started")
            }

            override fun onError(p0: Int, p1: String) {
                Log.d(HOME_REPAIR_MAIN_TAG, "AppsFlyer start error: $p0 - $p1")
            }
        })
        ///////////
        homeRepairStartConversionTimeout()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@HomeRepairApplication)
            modules(
                listOf(
                    homeRepairModule
                )
            )
        }
    }

    private fun homeRepairExtractDeepMap(dl: DeepLink) {
        val map = mutableMapOf<String, Any>()
        dl.deepLinkValue?.let { map["deep_link_value"] = it }
        dl.mediaSource?.let { map["media_source"] = it }
        dl.campaign?.let { map["campaign"] = it }
        dl.campaignId?.let { map["campaign_id"] = it }
        dl.afSub1?.let { map["af_sub1"] = it }
        dl.afSub2?.let { map["af_sub2"] = it }
        dl.afSub3?.let { map["af_sub3"] = it }
        dl.afSub4?.let { map["af_sub4"] = it }
        dl.afSub5?.let { map["af_sub5"] = it }
        dl.matchType?.let { map["match_type"] = it }
        dl.clickHttpReferrer?.let { map["click_http_referrer"] = it }
        dl.getStringValue("timestamp")?.let { map["timestamp"] = it }
        dl.isDeferred?.let { map["is_deferred"] = it }
        for (i in 1..10) {
            val key = "deep_link_sub$i"
            dl.getStringValue(key)?.let {
                if (!map.containsKey(key)) {
                    map[key] = it
                }
            }
        }
        Log.d(HOME_REPAIR_MAIN_TAG, "Extracted DeepLink data: $map")
        homeRepairDeepLinkData = map
    }
    /////////////////

    private fun homeRepairStartConversionTimeout() {
        homeRepairConversionTimeoutJob = CoroutineScope(Dispatchers.Main).launch {
            delay(30000)
            if (!homeRepairIsResumed) {
                Log.d(HOME_REPAIR_MAIN_TAG, "TIMEOUT: No conversion data received in 30s")
                homeRepairResume(HomeRepairAppsFlyerState.HomeRepairError)
            }
        }
    }

    private fun homeRepairResume(state: HomeRepairAppsFlyerState) {
        ////////////
        homeRepairConversionTimeoutJob?.cancel()
        if (state is HomeRepairAppsFlyerState.HomeRepairSuccess) {
            val convData = state.homeRepairData ?: mutableMapOf()
            val deepData = homeRepairDeepLinkData ?: mutableMapOf()
            val merged = mutableMapOf<String, Any>().apply {
                putAll(convData)
                for ((key, value) in deepData) {
                    if (!containsKey(key)) {
                        put(key, value)
                    }
                }
            }
            if (!homeRepairIsResumed) {
                homeRepairIsResumed = true
                homeRepairConversionFlow.value =
                    HomeRepairAppsFlyerState.HomeRepairSuccess(merged)
            }
        } else {
            if (!homeRepairIsResumed) {
                homeRepairIsResumed = true
                homeRepairConversionFlow.value = state
            }
        }
    }

    private fun homeRepairGetAppsflyerId(): String {
        val appsflyrid = AppsFlyerLib.getInstance().getAppsFlyerUID(this) ?: ""
        Log.d(HOME_REPAIR_MAIN_TAG, "AppsFlyer: AppsFlyer Id = $appsflyrid")
        return appsflyrid
    }

    private fun homeRepairSetDebufLogger(appsflyer: AppsFlyerLib) {
        appsflyer.setDebugLog(true)
    }

    private fun homeRepairMinTimeBetween(appsflyer: AppsFlyerLib) {
        appsflyer.setMinTimeBetweenSessions(0)
    }

    private fun homeRepairGetApi(url: String, client: OkHttpClient?): HomeRepairAppsApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }

    companion object {
        var homeRepairInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        val homeRepairConversionFlow: MutableStateFlow<HomeRepairAppsFlyerState> = MutableStateFlow(
            HomeRepairAppsFlyerState.HomeRepairDefault
        )
        var HOME_REPAIR_FB_LI: String? = null
        const val HOME_REPAIR_MAIN_TAG = "HomeRepairMainTag"
    }
}