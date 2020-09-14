package co.optable.android_sdk.edge

import co.optable.android_sdk.OptableIdentifyInput
import co.optable.android_sdk.OptableIdentifyResponse
import co.optable.android_sdk.OptableSDK
import co.optable.android_sdk.OptableTargetingResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EdgeService {

    @POST("/{app}/identify")
    suspend fun Identify(@Path("app") app: String, @Body idList: OptableIdentifyInput):
            EdgeResponse<OptableIdentifyResponse, OptableSDK.Response.Error>

    @GET("/{app}/targeting")
    suspend fun Targeting(@Path("app") app: String):
            EdgeResponse<OptableTargetingResponse, OptableSDK.Response.Error>

}