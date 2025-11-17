package co.optable.android_sdk.core

import android.util.Base64

internal class Config(
    val tenant: String,
    val originSlug: String,
    val userAgent: String?,
    val skipAdvertisingIdDetection: Boolean,
    private val host: String,
    private val path: String,
    private val insecure: Boolean,
) {

    fun edgeBaseURL(): String {
        var proto = "https://"
        if (insecure) {
            proto = "http://"
        }
        return "$proto$host/$path/"
    }

    fun passportKey(): String {
        return key("PASS")
    }

    fun targetingKey(): String {
        return key("TGT")
    }

    private fun key(kind: String): String {
        val sfx = host + "/" + tenant + "/" + originSlug
        return "OPTABLE_" + kind + "_" + Base64.encodeToString(sfx.toByteArray(), 0)
    }

}