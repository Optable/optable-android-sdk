package co.optable.android_sdk.core.network

import co.optable.BuildConfig
import co.optable.android_sdk.OptableConfig
import co.optable.android_sdk.core.ConsentsManager
import co.optable.android_sdk.core.LocalStorage
import co.optable.android_sdk.core.UserAgentHolder
import okhttp3.Interceptor
import okhttp3.Response

internal class RequestInterceptor(
    private val config: OptableConfig,
    private val storage: LocalStorage,
    private val userAgentHolder: UserAgentHolder,
    private val consentsManager: ConsentsManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val builder = originalRequest.url.newBuilder()
            .addQueryParameter("osdk", "android-${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}")
            .addQueryParameter("t", config.tenant)
            .addQueryParameter("o", config.originSlug)

        val subjectToGdpr = consentsManager.subjectToGdpr()
        if (subjectToGdpr != null) {
            builder.addQueryParameter("gdpr", if (subjectToGdpr) "1" else "0")
        }

        val gdprConsent = consentsManager.gdprConsent()
        if (gdprConsent != null) {
            builder.addQueryParameter("gdpr_consent", gdprConsent)
        }

        val gppConsent = consentsManager.gppConsent()
        if (gppConsent != null) {
            builder.addQueryParameter("gpp", gppConsent)
        }

        val gppSid = consentsManager.gppSid()
        if (gppSid != null) {
            builder.addQueryParameter("gpp_sid", gppSid)
        }

        val url = builder.build()

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