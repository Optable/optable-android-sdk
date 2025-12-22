package co.optable.android_sdk.core

import android.util.Log
import androidx.core.net.toUri
import co.optable.android_sdk.OptableIdentifiers
import java.security.MessageDigest
import java.util.*

/**
 * Utility object responsible for encoding raw identifiers into Optable Enriched Identifiers (EIDs).
 */
object IdentifiersEncoder {

    private const val EMAIL = "e"
    private const val PHONE = "p"
    private const val POSTAL = "z"
    private const val IPV4 = "i4"
    private const val IPV6 = "i6"
    private const val IDFA = "a"
    private const val GAID = "g"
    private const val RIDA = "r"
    private const val TIFA = "s"
    private const val AFAI = "f"
    private const val NETID = "n"
    private const val ID5 = "id5"
    private const val UTIQ = "utiq"
    private const val VID = "v"

    fun encode(ids: OptableIdentifiers): List<String> {
        val result = mutableListOf<String>()

        result.addIfNotNull(EMAIL, ids.email, ::encrypt)
        result.addIfNotNull(PHONE, ids.phoneNumber, ::encrypt)
        result.addIfNotNull(POSTAL, ids.postalCode, ::normalize)
        result.addIfNotNull(IPV4, ids.ipv4Address, ::removeWhitespaces)
        result.addIfNotNull(IPV6, ids.ipv6Address, ::normalize)
        result.addIfNotNull(IDFA, ids.appleIdfa, ::normalize)
        result.addIfNotNull(GAID, ids.googleGaid, ::normalize)
        result.addIfNotNull(RIDA, ids.rokuRida, ::normalize)
        result.addIfNotNull(TIFA, ids.samsungTifa, ::normalize)
        result.addIfNotNull(AFAI, ids.amazonFireAfai, ::normalize)
        result.addIfNotNull(NETID, ids.netId, ::removeWhitespaces)
        result.addIfNotNull(ID5, ids.id5, ::removeWhitespaces)
        result.addIfNotNull(UTIQ, ids.utiq, ::normalize)

        for ((key, value) in ids.custom ?: emptyMap()) {
            when {
                key == VID -> {
                    result.addIfNotNull(key, value, ::removeWhitespaces)
                }

                else -> {
                    result.addIfNotNull(key, value, ::trim)
                }
            }
        }

        for (rawValue in ids.raw ?: emptyList()) {
            result.add(rawValue)
        }

        return result
    }

    private fun MutableList<String>.addIfNotNull(key: String, value: String?, encoder: (String) -> String) {
        if (value == null) return

        val encodedValue = encoder(value)
        val result = "$key:$encodedValue"
        this.add(result)
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
     *
     * Algorithm.
     * We first convert the Uri to a lowercase string then re-parse it so that we are
     * not dependent on case-sensitivity of the "oeid" query parameter
     */
    fun eidFromUrl(urlString: String): String? {
        try {
            val uri = urlString.lowercase(Locale.ROOT).toUri()
            val id = uri.getQueryParameter("oeid")

            val wrongId = (id == null) || (id.length != 64) || (id.matches("^[a-f0-9]$".toRegex()))
            if (!wrongId) {
                return "e:$id"
            }
        } catch (e: Exception) {
            Log.e("OptableSDK", "Can't get eid from $urlString", e)
        }
        return null
    }


    /**
     * Computes SHA-256 hash of the input string.
     */
    private fun encrypt(value: String): String {
        val normalizedBytes = normalize(value).toByteArray(Charsets.UTF_8)
        val encryptedBytes = MessageDigest.getInstance("SHA-256").digest(normalizedBytes)
        val encryptedValue = encryptedBytes.joinToString("") { "%02x".format(it) }
        return encryptedValue
    }

    /**
     * Normalizes an identifier string by removing all whitespace and lowercasing the result.
     * This is used for hash-based EIDs (email, phone).
     */
    private fun normalize(value: String): String {
        return value.replace("\\s+".toRegex(), "").lowercase(Locale.ROOT)
    }

    /**
     * Normalizes an identifier string by removing all whitespace.
     * This is used for raw EIDs (like IP addresses).
     */
    private fun removeWhitespaces(value: String): String {
        return value.replace("\\s+".toRegex(), "")
    }

    /**
     * Simple trim.
     */
    private fun trim(value: String): String {
        return value.trim()
    }
}