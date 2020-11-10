/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk.core

import android.content.Context
import android.text.TextUtils
import android.webkit.WebView
import co.optable.android_sdk.*
import co.optable.android_sdk.edge.EdgeResponse
import co.optable.android_sdk.edge.EdgeResponseAdapterFactory
import co.optable.android_sdk.edge.EdgeService
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Client(private val config: Config, private val context: Context) {
    var gaid: String? = null
    var gaidLAT: Boolean? = true

    private val edgeService: EdgeService?
    private val userAgent = this.userAgent()
    private val storage = LocalStorage(this.config, this.context)

    private class RequestInterceptor(private val userAgent: String, private val storage: LocalStorage): Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var oldRequest = chain.request()
            var newURL = oldRequest.url.newBuilder()
                .addQueryParameter("osdk",
                    "android-" +
                            BuildConfig.VERSION_NAME + "-" +
                            BuildConfig.VERSION_CODE.toString()
                ).build()
            var newRequest = oldRequest.newBuilder()
                .url(newURL)
                .addHeader("User-Agent", userAgent)

            val pass = storage.getPassport()
            if (pass != null) {
                newRequest = newRequest.addHeader("X-Optable-Visitor", pass)
            }
            return chain.proceed(newRequest.build())
        }
    }

    private class ResponseInterceptor(private val storage: LocalStorage): Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalResponse = chain.proceed(chain.request())
            val pass = originalResponse.header("X-Optable-Visitor")
            if (pass != null) {
                storage.setPassport(pass)
            }
            return originalResponse.newBuilder().build()
        }
    }

    init {
        this.determineAdvertisingInfo()

        val client = OkHttpClient.Builder()
            .addInterceptor(RequestInterceptor(userAgent, storage))
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

    suspend fun Identify(idList: OptableIdentifyInput):
            EdgeResponse<OptableIdentifyResponse, OptableSDK.Response.Error>
    {
        return edgeService!!.Identify(this.config.app, idList)
    }

    suspend fun Targeting():
            EdgeResponse<OptableTargetingResponse, OptableSDK.Response.Error>
    {
        return edgeService!!.Targeting(this.config.app)
    }

    suspend fun Witness(event: String, properties: OptableWitnessProperties):
            EdgeResponse<OptableWitnessResponse, OptableSDK.Response.Error>
    {
        val evtBody = HashMap<String,Any>()
        evtBody.put("event", event)
        evtBody.put("properties", properties)
        return edgeService!!.Witness(this.config.app, evtBody)
    }

    fun TargetingSetCache(keyvalues: OptableTargetingResponse) {
        storage.setTargeting(keyvalues)
    }

    fun TargetingFromCache(): OptableTargetingResponse? {
        return storage.getTargeting()
    }

    fun TargetingClearCache() {
        storage.clearTargeting()
    }

    fun hasGAID(): Boolean {
        return ((gaid != null) && (gaidLAT == false) && !TextUtils.isEmpty(gaid!!))
    }

    fun GAID(): String? {
        return gaid!!
    }

    private fun userAgent(): String {
        return WebView(this.context).settings.userAgentString
    }

    private fun determineAdvertisingInfo() {
        val context = this.context
        GlobalScope.launch {
            var adInfo: AdvertisingIdClient.Info? = null
            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
            } catch(e: Exception) {}

            gaid = adInfo?.id
            gaidLAT = adInfo?.isLimitAdTrackingEnabled
        }
    }
}