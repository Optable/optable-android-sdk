/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk.edge

import co.optable.android_sdk.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface EdgeService {

    @POST("/{app}/init")
    suspend fun Init(@Path("app") app: String):
            EdgeResponse<OptableInitResponse, OptableSDK.Response.Error>

    @POST("/{app}/identify")
    suspend fun Identify(@Path("app") app: String, @Body idList: OptableIdentifyInput):
            EdgeResponse<OptableIdentifyResponse, OptableSDK.Response.Error>

    @POST("/{app}/profile")
    suspend fun Profile(@Path("app") app: String,
                        @Body profileBody: HashMap<String,Any>):
            EdgeResponse<OptableProfileResponse, OptableSDK.Response.Error>

    @GET("/{app}/targeting")
    suspend fun Targeting(@Path("app") app: String):
            EdgeResponse<OptableTargetingResponse, OptableSDK.Response.Error>

    @POST("/{app}/witness")
    suspend fun Witness(@Path("app") app: String,
                        @Body witnessBody: HashMap<String,Any>):
            EdgeResponse<OptableWitnessResponse, OptableSDK.Response.Error>

}