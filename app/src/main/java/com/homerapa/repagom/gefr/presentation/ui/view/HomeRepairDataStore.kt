package com.homerapa.repagom.gefr.presentation.ui.view

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel

class HomeRepairDataStore : ViewModel(){
    val homeRepairViList: MutableList<HomeRepairVi> = mutableListOf()
    var homeRepairIsFirstCreate = true
    @SuppressLint("StaticFieldLeak")
    lateinit var homeRepairContainerView: FrameLayout
    @SuppressLint("StaticFieldLeak")
    lateinit var homeRepairView: HomeRepairVi

}