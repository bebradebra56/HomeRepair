package com.homerapa.repagom.gefr.presentation.ui.load

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homerapa.repagom.gefr.data.shar.HomeRepairSharedPreference
import com.homerapa.repagom.gefr.data.utils.HomeRepairSystemService
import com.homerapa.repagom.gefr.domain.usecases.HomeRepairGetAllUseCase
import com.homerapa.repagom.gefr.presentation.app.HomeRepairAppsFlyerState
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeRepairLoadViewModel(
    private val homeRepairGetAllUseCase: HomeRepairGetAllUseCase,
    private val homeRepairSharedPreference: HomeRepairSharedPreference,
    private val homeRepairSystemService: HomeRepairSystemService
) : ViewModel() {

    private val _homeRepairHomeScreenState: MutableStateFlow<HomeRepairHomeScreenState> =
        MutableStateFlow(HomeRepairHomeScreenState.HomeRepairLoading)
    val homeRepairHomeScreenState = _homeRepairHomeScreenState.asStateFlow()

    private var homeRepairGetApps = false


    init {
        viewModelScope.launch {
            when (homeRepairSharedPreference.homeRepairAppState) {
                0 -> {
                    if (homeRepairSystemService.homeRepairIsOnline()) {
                        HomeRepairApplication.homeRepairConversionFlow.collect {
                            when(it) {
                                HomeRepairAppsFlyerState.HomeRepairDefault -> {}
                                HomeRepairAppsFlyerState.HomeRepairError -> {
                                    homeRepairSharedPreference.homeRepairAppState = 2
                                    _homeRepairHomeScreenState.value =
                                        HomeRepairHomeScreenState.HomeRepairError
                                    homeRepairGetApps = true
                                }
                                is HomeRepairAppsFlyerState.HomeRepairSuccess -> {
                                    if (!homeRepairGetApps) {
                                        homeRepairGetData(it.homeRepairData)
                                        homeRepairGetApps = true
                                    }
                                }
                            }
                        }
                    } else {
                        _homeRepairHomeScreenState.value =
                            HomeRepairHomeScreenState.HomeRepairNotInternet
                    }
                }
                1 -> {
                    if (homeRepairSystemService.homeRepairIsOnline()) {
                        if (HomeRepairApplication.HOME_REPAIR_FB_LI != null) {
                            _homeRepairHomeScreenState.value =
                                HomeRepairHomeScreenState.HomeRepairSuccess(
                                    HomeRepairApplication.HOME_REPAIR_FB_LI.toString()
                                )
                        } else if (System.currentTimeMillis() / 1000 > homeRepairSharedPreference.homeRepairExpired) {
                            Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "Current time more then expired, repeat request")
                            HomeRepairApplication.homeRepairConversionFlow.collect {
                                when(it) {
                                    HomeRepairAppsFlyerState.HomeRepairDefault -> {}
                                    HomeRepairAppsFlyerState.HomeRepairError -> {
                                        _homeRepairHomeScreenState.value =
                                            HomeRepairHomeScreenState.HomeRepairSuccess(
                                                homeRepairSharedPreference.homeRepairSavedUrl
                                            )
                                        homeRepairGetApps = true
                                    }
                                    is HomeRepairAppsFlyerState.HomeRepairSuccess -> {
                                        if (!homeRepairGetApps) {
                                            homeRepairGetData(it.homeRepairData)
                                            homeRepairGetApps = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "Current time less then expired, use saved url")
                            _homeRepairHomeScreenState.value =
                                HomeRepairHomeScreenState.HomeRepairSuccess(
                                    homeRepairSharedPreference.homeRepairSavedUrl
                                )
                        }
                    } else {
                        _homeRepairHomeScreenState.value =
                            HomeRepairHomeScreenState.HomeRepairNotInternet
                    }
                }
                2 -> {
                    _homeRepairHomeScreenState.value =
                        HomeRepairHomeScreenState.HomeRepairError
                }
            }
        }
    }


    private suspend fun homeRepairGetData(conversation: MutableMap<String, Any>?) {
        val homeRepairData = homeRepairGetAllUseCase.invoke(conversation)
        if (homeRepairSharedPreference.homeRepairAppState == 0) {
            if (homeRepairData == null) {
                homeRepairSharedPreference.homeRepairAppState = 2
                _homeRepairHomeScreenState.value =
                    HomeRepairHomeScreenState.HomeRepairError
            } else {
                homeRepairSharedPreference.homeRepairAppState = 1
                homeRepairSharedPreference.apply {
                    homeRepairExpired = homeRepairData.homeRepairExpires
                    homeRepairSavedUrl = homeRepairData.homeRepairUrl
                }
                _homeRepairHomeScreenState.value =
                    HomeRepairHomeScreenState.HomeRepairSuccess(homeRepairData.homeRepairUrl)
            }
        } else  {
            if (homeRepairData == null) {
                _homeRepairHomeScreenState.value =
                    HomeRepairHomeScreenState.HomeRepairSuccess(
                        homeRepairSharedPreference.homeRepairSavedUrl
                    )
            } else {
                homeRepairSharedPreference.apply {
                    homeRepairExpired = homeRepairData.homeRepairExpires
                    homeRepairSavedUrl = homeRepairData.homeRepairUrl
                }
                _homeRepairHomeScreenState.value =
                    HomeRepairHomeScreenState.HomeRepairSuccess(homeRepairData.homeRepairUrl)
            }
        }
    }


    sealed class HomeRepairHomeScreenState {
        data object HomeRepairLoading : HomeRepairHomeScreenState()
        data object HomeRepairError : HomeRepairHomeScreenState()
        data class HomeRepairSuccess(val data: String) : HomeRepairHomeScreenState()
        data object HomeRepairNotInternet: HomeRepairHomeScreenState()
    }
}