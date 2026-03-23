package com.homerapa.repagom.gefr.data.repo

import android.util.Log
import com.homerapa.repagom.gefr.domain.model.HomeRepairEntity
import com.homerapa.repagom.gefr.domain.model.HomeRepairParam
import com.homerapa.repagom.gefr.presentation.app.HomeRepairApplication.Companion.HOME_REPAIR_MAIN_TAG
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface HomeRepairApi {
    @Headers("Content-Type: application/json")
    @POST("config.php")
    fun homeRepairGetClient(
        @Body jsonString: JsonObject,
    ): Call<HomeRepairEntity>
}


private const val HOME_REPAIR_MAIN = "https://homereppair.com/"
class HomeRepairRepository {

    suspend fun homeRepairGetClient(
        homeRepairParam: HomeRepairParam,
        homeRepairConversion: MutableMap<String, Any>?
    ): HomeRepairEntity? {
        val gson = Gson()
        val api = homeRepairGetApi(HOME_REPAIR_MAIN, null)

        val homeRepairJsonObject = gson.toJsonTree(homeRepairParam).asJsonObject
        homeRepairConversion?.forEach { (key, value) ->
            val element: JsonElement = gson.toJsonTree(value)
            homeRepairJsonObject.add(key, element)
        }
        return try {
            val homeRepairRequest: Call<HomeRepairEntity> = api.homeRepairGetClient(
                jsonString = homeRepairJsonObject,
            )
            val homeRepairResult = homeRepairRequest.awaitResponse()
            Log.d(HOME_REPAIR_MAIN_TAG, "Retrofit: Result code: ${homeRepairResult.code()}")
            if (homeRepairResult.code() == 200) {
                Log.d(HOME_REPAIR_MAIN_TAG, "Retrofit: Get request success")
                Log.d(HOME_REPAIR_MAIN_TAG, "Retrofit: Code = ${homeRepairResult.code()}")
                Log.d(HOME_REPAIR_MAIN_TAG, "Retrofit: ${homeRepairResult.body()}")
                homeRepairResult.body()
            } else {
                null
            }
        } catch (e: java.lang.Exception) {
            Log.d(HOME_REPAIR_MAIN_TAG, "Retrofit: Get request failed")
            Log.d(HOME_REPAIR_MAIN_TAG, "Retrofit: ${e.message}")
            null
        }
    }


    private fun homeRepairGetApi(url: String, client: OkHttpClient?) : HomeRepairApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .client(client ?: OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create()
    }


}
