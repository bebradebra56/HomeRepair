package com.homerapa.repagom.gefr.domain.model

import com.google.gson.annotations.SerializedName


private const val HOME_REPAIR_A = "com.homerapa.repagom"
private const val HOME_REPAIR_B = "homerepair-5c646"
data class HomeRepairParam (
    @SerializedName("af_id")
    val homeRepairAfId: String,
    @SerializedName("bundle_id")
    val homeRepairBundleId: String = HOME_REPAIR_A,
    @SerializedName("os")
    val homeRepairOs: String = "Android",
    @SerializedName("store_id")
    val homeRepairStoreId: String = HOME_REPAIR_A,
    @SerializedName("locale")
    val homeRepairLocale: String,
    @SerializedName("push_token")
    val homeRepairPushToken: String,
    @SerializedName("firebase_project_id")
    val homeRepairFirebaseProjectId: String = HOME_REPAIR_B,

    )