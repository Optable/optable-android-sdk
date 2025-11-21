package co.optable.android_sdk.core

import co.optable.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

internal class RequestInterceptor(
    private val config: Config,
    private val storage: LocalStorage,
    private val userAgentHolder: UserAgentHolder,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val url = originalRequest.url.newBuilder()
            .addQueryParameter("osdk", "android-${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}")
            .addQueryParameter("t", config.tenant)
            .addQueryParameter("o", config.originSlug)
            .build()

        val modifiedRequest = originalRequest.newBuilder().url(url)
        modifiedRequest.addHeader("Accept", "application/json")

        val apiKey = config.apiKey
        if (apiKey != null) {
            modifiedRequest.addHeader("Authorization", "Bearer $apiKey")
        }

        val userAgent = userAgentHolder.getUserAgent()
        if (userAgent != null) {
            modifiedRequest.addHeader("User-Agent", userAgent)
        }

        val pass = storage.getPassport()
        if (pass != null) {
            modifiedRequest.addHeader("X-Optable-Visitor", pass)
        }

        return chain.proceed(modifiedRequest.build())
    }
}