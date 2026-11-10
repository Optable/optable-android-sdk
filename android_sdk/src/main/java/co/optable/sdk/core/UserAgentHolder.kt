package co.optable.sdk.core

import android.content.Context
import android.webkit.WebSettings
import co.optable.sdk.OptableConfig

internal class UserAgentHolder(
    private val config: OptableConfig,
) {

    private val cachedDefaultUserAgent: String? by lazy { defaultUserAgent(config.context) }

    fun getUserAgent() = config.customUserAgent ?: cachedDefaultUserAgent


    private fun defaultUserAgent(context: Context): String? {
        return try {
            // Unlike WebView(context).settings, this does not construct a WebView and
            // works on any thread, so the UA survives off-main-thread SDK construction.
            WebSettings.getDefaultUserAgent(context)
        } catch (_: Exception) {
            null
        }
    }

}
