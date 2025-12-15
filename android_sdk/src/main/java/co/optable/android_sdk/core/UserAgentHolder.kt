package co.optable.android_sdk.core

import android.content.Context
import android.webkit.WebView
import co.optable.android_sdk.OptableConfig

internal class UserAgentHolder(
    config: OptableConfig,
) {

    private val cachedUserAgent: String? = config.customUserAgent ?: userAgentFromWebView(config.context)

    fun getUserAgent() = cachedUserAgent


    private fun userAgentFromWebView(context: Context): String? {
        return try {
            WebView(context).settings.userAgentString
        } catch (_: Exception) {
            null
        }
    }

}