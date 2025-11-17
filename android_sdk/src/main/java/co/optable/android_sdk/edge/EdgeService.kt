/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk.edge

import co.optable.android_sdk.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface EdgeService {

    @POST("/identify")
    suspend fun identify(@Body idList: OptableIdentifyInput): EdgeResponse<OptableIdentifyResponse, OptableSDK.Response.Error>

    @POST("/profile")
    suspend fun profile(@Body profileBody: HashMap<String, Any>): EdgeResponse<OptableProfileResponse, OptableSDK.Response.Error>

    @GET("/targeting")
    suspend fun targeting(): EdgeResponse<OptableTargetingResponse, OptableSDK.Response.Error>

    @POST("/witness")
    suspend fun witness(@Body witnessBody: HashMap<String, Any>): EdgeResponse<OptableWitnessResponse, OptableSDK.Response.Error>

}