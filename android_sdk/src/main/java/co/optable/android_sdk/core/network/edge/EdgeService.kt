/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk.core.network.edge

import co.optable.android_sdk.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface EdgeService {

    @POST("/identify")
    fun identify(@Body idList: OptableIdentifyInput): Call<Unit>

    @POST("/profile")
    suspend fun profile(@Body profileBody: HashMap<String, Any>): EdgeResponse<OptableProfileResponse, OptableResponse.Error>

    @GET("targeting")
    suspend fun targeting(): EdgeResponse<OptableTargetingResponse, OptableResponse.Error>

    @GET("targeting")
    fun targeting(@Query("id") idList: List<String>): Call<TargetingResponse>

    @POST("/witness")
    suspend fun witness(@Body witnessBody: HashMap<String, Any>): EdgeResponse<OptableWitnessResponse, OptableResponse.Error>

}