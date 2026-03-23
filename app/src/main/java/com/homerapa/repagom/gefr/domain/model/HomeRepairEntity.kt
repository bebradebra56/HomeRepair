package com.homerapa.repagom.gefr.domain.model

import com.google.gson.annotations.SerializedName


data class HomeRepairEntity (
    @SerializedName("ok")
    val homeRepairOk: String,
    @SerializedName("url")
    val homeRepairUrl: String,
    @SerializedName("expires")
    val homeRepairExpires: Long,
)