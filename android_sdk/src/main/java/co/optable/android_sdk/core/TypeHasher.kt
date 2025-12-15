package co.optable.android_sdk.core

import android.net.Uri
import android.util.Base64
import co.optable.android_sdk.OptableConfig
import java.security.MessageDigest
import java.util.Locale.getDefault

object TypeHasher {

    /**
     * eid(email) is a helper that returns type-prefixed SHA256(downcase(email))
     */
    fun eid(email: String): String {
        return "e:" + MessageDigest.getInstance("SHA-256")
            .digest(email.lowercase(getDefault()).trim().toByteArray())
            .fold("", { str, it -> str + "%02x".format(it) })
    }

    /**
     * gaid(gaid) is a helper that returns the type-prefixed Google Advertising ID
     */
    fun gaid(gaid: String): String {
        return "g:" + gaid.lowercase(getDefault()).trim()
    }

    /**
     * cid(ppid) is a helper that returns custom type-prefixed origin-provided PPID
     */
    fun cid(ppid: String): String {
        return "c:" + ppid.trim()
    }

    /**
     * eidFromURI(uri) is a helper that returns a type-prefixed ID based on the query string
     * oeid=sha256value parameters in the specified uri, if one is found. Otherwise, it returns
     * an empty string.
     *
     * The use for this is when handling incoming deep links which might contain an "oeid" value
     * with the SHA256(downcase(email)) of a user, such as encoded links in newsletter Emails
     * sent by the application developer. Such hashed Email values can be used in calls to
     * identify()
     */
    fun eidFromURI(uri: Uri): String {
        // We first convert the Uri to a lowercase string then re-parse it so that we are
        // not dependent on case-sensitivity of the "oeid" query parameter:
        var oeid = Uri.parse(uri.toString().lowercase(getDefault())).getQueryParameter("oeid")

        if ((oeid == null) || (oeid.length != 64) ||
            (oeid.matches("^[a-f0-9]$".toRegex(RegexOption.IGNORE_CASE)))
        ) {
            return ""
        }

        return "e:" + oeid.lowercase(getDefault())
    }

    fun passportKey(config: OptableConfig): String {
        return key("PASS", config)
    }

    fun targetingKey(config: OptableConfig): String {
        return key("TGT", config)
    }

    private fun key(kind: String, config: OptableConfig): String {
        val sfx = "${config.host}/${config.tenant}/${config.originSlug}"
        return "OPTABLE_" + kind + "_" + Base64.encodeToString(sfx.toByteArray(), 0)
    }

}