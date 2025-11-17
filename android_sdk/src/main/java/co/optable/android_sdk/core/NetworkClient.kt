/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk.core

import android.content.Context
import android.webkit.WebView
import co.optable.android_sdk.*
import co.optable.android_sdk.edge.EdgeResponse
import co.optable.android_sdk.edge.EdgeResponseAdapterFactory
import co.optable.android_sdk.edge.EdgeService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class NetworkClient(
    config: Config,
    storage: LocalStorage,
    private val context: Context,
) {

    private val edgeService: EdgeService

    init {
        var userAgent = config.userAgent
        if (userAgent == null) {
            userAgent = this.userAgentFromWebView()
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(RequestInterceptor(userAgent, config, storage))
            .addInterceptor(ResponseInterceptor(storage))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(config.edgeBaseURL())
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

    private fun userAgentFromWebView(): String {
        return WebView(this.context).settings.userAgentString
    }

}