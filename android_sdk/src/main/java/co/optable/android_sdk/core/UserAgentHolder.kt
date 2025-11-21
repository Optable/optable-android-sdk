package co.optable.android_sdk.core

import android.content.Context
import android.webkit.WebView

internal class UserAgentHolder(
    customUserAgent: String? = null,
    context: Context,
) {

    private val cachedUserAgent: String? = customUserAgent ?: userAgentFromWebView(context)

    fun getUserAgent() = cachedUserAgent


    private fun userAgentFromWebView(context: Context): String? {
        return try {
            WebView(context).settings.userAgentString
        } catch (_: Exception) {
            null
        }
    }

}