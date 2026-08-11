/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.sdk.core.network.edge

import co.optable.sdk.core.network.data.TraitsRequest
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface EdgeService {

    @GET("targeting")
    suspend fun targeting(
        @Query("id") idList: List<String>,
        @Query("hid") hidList: List<String>,
        @Query("bundle") bundle: String?,
        @Query("ver") version: String?,
        @Query("ua") userAgent: String?,
        @Query("id5_signature") id5Signature: String?,
    ): Response<JsonObject>

    @POST("identify")
    suspend fun identify(@Body ids: List<String>): Response<Unit>

    @POST("profile")
    suspend fun profile(@Body body: TraitsRequest): Response<JsonObject>

    @POST("witness")
    suspend fun witness(@Body witnessBody: HashMap<String, Any>): Response<Unit>

}
