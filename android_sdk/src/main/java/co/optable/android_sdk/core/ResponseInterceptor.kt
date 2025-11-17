package co.optable.android_sdk.core

import okhttp3.Interceptor
import okhttp3.Response

internal class ResponseInterceptor(private val storage: LocalStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        val pass = originalResponse.header("X-Optable-Visitor")
        if (pass != null) {
            storage.setPassport(pass)
        }
        return originalResponse.newBuilder().build()
    }
}