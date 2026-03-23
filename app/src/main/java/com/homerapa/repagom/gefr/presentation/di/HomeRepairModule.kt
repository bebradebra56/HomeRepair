package com.homerapa.repagom.gefr.presentation.di

import com.homerapa.repagom.gefr.data.repo.HomeRepairRepository
import com.homerapa.repagom.gefr.data.shar.HomeRepairSharedPreference
import com.homerapa.repagom.gefr.data.utils.HomeRepairPushToken
import com.homerapa.repagom.gefr.data.utils.HomeRepairSystemService
import com.homerapa.repagom.gefr.domain.usecases.HomeRepairGetAllUseCase
import com.homerapa.repagom.gefr.presentation.pushhandler.HomeRepairPushHandler
import com.homerapa.repagom.gefr.presentation.ui.load.HomeRepairLoadViewModel
import com.homerapa.repagom.gefr.presentation.ui.view.HomeRepairViFun
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeRepairModule = module {
    factory {
        HomeRepairPushHandler()
    }
    single {
        HomeRepairRepository()
    }
    single {
        HomeRepairSharedPreference(get())
    }
    factory {
        HomeRepairPushToken()
    }
    factory {
        HomeRepairSystemService(get())
    }
    factory {
        HomeRepairGetAllUseCase(
            get(), get(), get()
        )
    }
    factory {
        HomeRepairViFun(get())
    }
    viewModel {
        HomeRepairLoadViewModel(get(), get(), get())
    }
}