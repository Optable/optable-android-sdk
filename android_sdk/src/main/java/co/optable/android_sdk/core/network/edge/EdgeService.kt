/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk.core.network.edge

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface EdgeService {

    @GET("targeting")
    suspend fun targeting(@Query("id") idList: List<String>): Response<JsonObject>

    @POST("identify")
    suspend fun identify(@Body ids: List<String>): Response<Unit>

    @POST("profile")
    suspend fun profile(@Body profileBody: HashMap<String, Any>): Response<JsonObject>

    @POST("witness")
    suspend fun witness(@Body witnessBody: HashMap<String, Any>): Response<Unit>

}