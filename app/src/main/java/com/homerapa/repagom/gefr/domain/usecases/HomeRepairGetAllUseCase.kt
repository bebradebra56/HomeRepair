package com.homerapa.repagom.gefr.domain.usecases

import android.util.Log
import com.homerapa.repagom.gefr.data.repo.HomeRepairRepository
import com.homerapa.repagom.gefr.data.utils.HomeRepairPushToken
import com.homerapa.repagom.gefr.data.utils.HomeRepairSystemService
import com.homerapa.repagom.gefr.domain.model.HomeRepairEntity
import com.homerapa.repagom.gefr.domain.model.HomeRepairParam
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication

class HomeRepairGetAllUseCase(
    private val homeRepairRepository: HomeRepairRepository,
    private val homeRepairSystemService: HomeRepairSystemService,
    private val homeRepairPushToken: HomeRepairPushToken,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?) : HomeRepairEntity?{
        val params = HomeRepairParam(
            homeRepairLocale = homeRepairSystemService.homeRepairGetLocale(),
            homeRepairPushToken = homeRepairPushToken.homeRepairGetToken(),
            homeRepairAfId = homeRepairSystemService.homeRepairGetAppsflyerId()
        )
        Log.d(HomeRepairApplication.HOME_REPAIR_MAIN_TAG, "Params for request: $params")
        return homeRepairRepository.homeRepairGetClient(params, conversion)
    }



}