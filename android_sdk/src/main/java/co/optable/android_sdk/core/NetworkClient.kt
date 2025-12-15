/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk.core

import co.optable.android_sdk.*
import co.optable.android_sdk.edge.EdgeResponse
import co.optable.android_sdk.edge.EdgeResponseAdapterFactory
import co.optable.android_sdk.edge.EdgeService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class NetworkClient(
    config: OptableConfig,
    requestInterceptor: RequestInterceptor,
    responseInterceptor: ResponseInterceptor,
) {

    private val edgeService: EdgeService

    init {
        val client = OkHttpClient.Builder()
            .addInterceptor(requestInterceptor)
            .addInterceptor(responseInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(config.getBaseUrl())
            .addCallAdapterFactory(EdgeResponseAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        edgeService = retrofit.create(EdgeService::class.java)
    }

    suspend fun identify(idList: OptableIdentifyInput): EdgeResponse<OptableIdentifyResponse, OptableSDK.Response.Error> {
        return edgeService.identify(idList)
    }

    suspend fun profile(traits: OptableProfileTraits): EdgeResponse<OptableProfileResponse, OptableSDK.Response.Error> {
        val profileBody = HashMap<String, Any>()
        profileBody.put("traits", traits)
        return edgeService.profile(profileBody)
    }

    suspend fun targeting(): EdgeResponse<OptableTargetingResponse, OptableSDK.Response.Error> {
        return edgeService.targeting()
    }

    suspend fun witness(
        event: String,
        properties: OptableWitnessProperties,
    ): EdgeResponse<OptableWitnessResponse, OptableSDK.Response.Error> {
        val evtBody = HashMap<String, Any>()
        evtBody.put("event", event)
        evtBody.put("properties", properties)
        return edgeService.witness(evtBody)
    }

}