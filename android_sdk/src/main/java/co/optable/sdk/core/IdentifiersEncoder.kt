package co.optable.sdk.core

import android.util.Log
import androidx.core.net.toUri
import co.optable.sdk.OptableIdentifier
import java.security.MessageDigest
import java.util.*

/**
 * Utility object responsible for encoding raw identifiers into Optable EIDs.
 */
internal class IdentifiersEncoder(
    private val googleAdIdManager: GoogleAdIdManager,
) {

    companion object {
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
    }


    fun encode(identifiers: List<OptableIdentifier>): List<String> {
        val result = mutableListOf<String>()

        val googleAdId = googleAdIdManager.getId()
        if (googleAdId != null) {
            result.addIfNotNull(GAID, googleAdId, ::trim)
        }

        for (identifier in identifiers) {
            when (identifier) {
                is OptableIdentifier.Email -> result.addIfNotNull(EMAIL, identifier.value, ::encrypt)
                is OptableIdentifier.PhoneNumber -> result.addIfNotNull(PHONE, identifier.value, ::encrypt)
                is OptableIdentifier.PostalCode -> result.addIfNotNull(POSTAL, identifier.value, ::normalize)
                is OptableIdentifier.IPv4 -> result.addIfNotNull(IPV4, identifier.value, ::removeWhitespaces)
                is OptableIdentifier.IPv6 -> result.addIfNotNull(IPV6, identifier.value, ::normalize)
                is OptableIdentifier.AppleIdfa -> result.addIfNotNull(IDFA, identifier.value, ::normalize)
                is OptableIdentifier.RokuRida -> result.addIfNotNull(RIDA, identifier.value, ::normalize)
                is OptableIdentifier.SamsungTifa -> result.addIfNotNull(TIFA, identifier.value, ::normalize)
                is OptableIdentifier.AmazonFireAfai -> result.addIfNotNull(AFAI, identifier.value, ::normalize)
                is OptableIdentifier.NetId -> result.addIfNotNull(NETID, identifier.value, ::removeWhitespaces)
                is OptableIdentifier.ID5 -> result.addIfNotNull(ID5, identifier.value, ::removeWhitespaces)
                is OptableIdentifier.Utiq -> result.addIfNotNull(UTIQ, identifier.value, ::normalize)

                is OptableIdentifier.GoogleGaid -> {
                    if (googleAdId == null) {
                        result.addIfNotNull(GAID, identifier.value, ::normalize)
                    }
                }

                is OptableIdentifier.Custom -> {
                    if (identifier.key == VID) {
                        result.addIfNotNull(identifier.key, identifier.value, ::removeWhitespaces)
                    } else {
                        result.addIfNotNull(identifier.key, identifier.value, ::trim)
                    }
                }

                is OptableIdentifier.Raw -> result.add(identifier.value)
            }
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
     * Returns a type-prefixed ID based on the query string
     * oeid=sha256value parameters in the specified uri, if one is found.
     * Otherwise, it returns an empty string.
     *
     * The use for this is when handling incoming deep links which might contain an "oeid" value
     * with the SHA256(downcase(email)) of a user, such as encoded links in newsletter Emails
     * sent by the application developer. Such hashed Email values can be used in calls to
     * identify().
     */
    fun prefixedIdFromUrl(urlString: String): String? {
        try {
            val uri = urlString.lowercase(Locale.ROOT).toUri()
            val id = uri.getQueryParameter("oeid")

            val wrongId = (id == null) || (id.length != 64) || (!id.matches("^[a-f0-9]+$".toRegex()))
            if (wrongId) return null

            return "e:$id"
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
