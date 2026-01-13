package co.optable.android_sdk

import co.optable.android_sdk.core.IdentifiersEncoder

/**
 * Immutable container for a set of user and device identifiers that can be transformed into
 * Optable EIDs.
 *
 * This type accepts multiple optional identifier values. When EIDs are generated, each supported
 * identifier is encoded according to its identifier type (for example, some values are normalized,
 * whitespace-stripped, or encrypted). Additional identifiers may be supplied through the [custom]
 * map, and pre-encoded values may be provided through [raw].
 *
 * EID generation is cached per instance: the first call computes the encoded list and subsequent
 * calls return the same list for the lifetime of this object.
 *
 * @param email Email address. Encoding: normalized (whitespace removed, lowercased) and SHA-256 hashed.
 * @param phoneNumber Phone number. Encoding: normalized (whitespace removed, lowercased) and SHA-256 hashed.
 * @param postalCode Postal/ZIP code. Encoding: normalized (whitespace removed, lowercased).
 * @param ipv4Address IPv4 address. Encoding: whitespace removed.
 * @param ipv6Address IPv6 address. Encoding: normalized (whitespace removed, lowercased).
 * @param appleIdfa Apple IDFA (Identifier for Advertisers). Encoding: normalized (whitespace removed, lowercased).
 * @param googleGaid Google GAID / AAID (Advertising ID). Encoding: normalized (whitespace removed, lowercased).
 * @param rokuRida Roku RIDA. Encoding: normalized (whitespace removed, lowercased).
 * @param samsungTifa Samsung TIFA. Encoding: normalized (whitespace removed, lowercased).
 * @param amazonFireAfai Amazon Fire AFAI. Encoding: normalized (whitespace removed, lowercased).
 * @param netId NetID identifier. Encoding: whitespace removed.
 * @param id5 ID5 identifier. Encoding: whitespace removed.
 * @param utiq Utiq identifier. Encoding: normalized (whitespace removed, lowercased).
 * @param custom Additional identifier map (type -> value). Encoding: for key "v" whitespace removed; otherwise trimmed.
 * @param raw Pre-encoded EID strings to include as-is (no encoding applied). Required format: "type:value" (f.e. "c1:value")
 */
data class OptableIdentifiers @JvmOverloads constructor(
    val email: String? = null,
    val phoneNumber: String? = null,
    val postalCode: String? = null,
    val ipv4Address: String? = null,
    val ipv6Address: String? = null,
    val appleIdfa: String? = null,
    val googleGaid: String? = null,
    val rokuRida: String? = null,
    val samsungTifa: String? = null,
    val amazonFireAfai: String? = null,
    val netId: String? = null,
    val id5: String? = null,
    val utiq: String? = null,
    val custom: Map<String, String>? = null,
    val raw: List<String>? = null,
) {

    companion object {
        /**
         * Whether to automatically retrieve GAID (Google Advertising ID). Will be ignored if the `googleGaid` is set.
         */
        internal var receiveGaidAutomatically: Boolean = true
    }

    private val cachedEIDs: List<String> by lazy { IdentifiersEncoder.encode(this) }


    /**
     * Generates a list of EIDs based on the current contents of the dictionary.
     */
    internal fun generateEIDs(): List<String> {
        return cachedEIDs
    }

    class Builder {

        private var email: String? = null
        private var phoneNumber: String? = null
        private var postalCode: String? = null
        private var ipv4Address: String? = null
        private var ipv6Address: String? = null
        private var appleIdfa: String? = null
        private var googleGaid: String? = null
        private var rokuRida: String? = null
        private var samsungTifa: String? = null
        private var amazonFireAfai: String? = null
        private var netId: String? = null
        private var id5: String? = null
        private var utiq: String? = null
        private var custom: Map<String, String>? = null
        private var raw: List<String>? = null
        private var receiveGaidAutomatically: Boolean = false

        /**
         * Sets the email address.
         *
         * @param email Email address. Encoding: normalized (whitespace removed, lowercased) and SHA-256 hashed.
         */
        fun email(email: String?) = apply {
            this.email = email
        }

        /**
         * Sets the phone number.
         *
         * @param phoneNumber Phone number. Encoding: normalized (whitespace removed, lowercased) and SHA-256 hashed.
         */
        fun phoneNumber(phoneNumber: String?) = apply {
            this.phoneNumber = phoneNumber
        }

        /**
         * Sets the postal/ZIP code.
         *
         * @param postalCode Postal/ZIP code. Encoding: normalized (whitespace removed, lowercased).
         */
        fun postalCode(postalCode: String?) = apply {
            this.postalCode = postalCode
        }

        /**
         * Sets the IPv4 address.
         *
         * @param ipv4Address IPv4 address. Encoding: whitespace removed.
         */
        fun ipv4Address(ipv4Address: String?) = apply {
            this.ipv4Address = ipv4Address
        }

        /**
         * Sets the IPv6 address.
         *
         * @param ipv6Address IPv6 address. Encoding: normalized (whitespace removed, lowercased).
         */
        fun ipv6Address(ipv6Address: String?) = apply {
            this.ipv6Address = ipv6Address
        }

        /**
         * Sets the Apple IDFA (Identifier for Advertisers).
         *
         * @param appleIdfa Apple IDFA (Identifier for Advertisers). Encoding: normalized (whitespace removed, lowercased).
         */
        fun appleIdfa(appleIdfa: String?) = apply {
            this.appleIdfa = appleIdfa
        }

        /**
         * Sets the Google GAID / AAID (Advertising ID).
         *
         * @param googleGaid Google GAID / AAID (Advertising ID). Encoding: normalized (whitespace removed, lowercased).
         */
        fun googleGaid(googleGaid: String?) = apply {
            this.googleGaid = googleGaid
        }

        /**
         * Sets the Roku RIDA.
         *
         * @param rokuRida Roku RIDA. Encoding: normalized (whitespace removed, lowercased).
         */
        fun rokuRida(rokuRida: String?) = apply {
            this.rokuRida = rokuRida
        }

        /**
         * Sets the Samsung TIFA.
         *
         * @param samsungTifa Samsung TIFA. Encoding: normalized (whitespace removed, lowercased).
         */
        fun samsungTifa(samsungTifa: String?) = apply {
            this.samsungTifa = samsungTifa
        }

        /**
         * Sets the Amazon Fire AFAI.
         *
         * @param amazonFireAfai Amazon Fire AFAI. Encoding: normalized (whitespace removed, lowercased).
         */
        fun amazonFireAfai(amazonFireAfai: String?) = apply {
            this.amazonFireAfai = amazonFireAfai
        }

        /**
         * Sets the NetID identifier.
         *
         * @param netId NetID identifier. Encoding: whitespace removed.
         */
        fun netId(netId: String?) = apply {
            this.netId = netId
        }

        /**
         * Sets the ID5 identifier.
         *
         * @param id5 ID5 identifier. Encoding: whitespace removed.
         */
        fun id5(id5: String?) = apply {
            this.id5 = id5
        }

        /**
         * Sets the Utiq identifier.
         *
         * @param utiq Utiq identifier. Encoding: normalized (whitespace removed, lowercased).
         */
        fun utiq(utiq: String?) = apply {
            this.utiq = utiq
        }

        /**
         * Sets additional identifiers (type -> value).
         *
         * @param custom Additional identifier map (type -> value). Encoding: for key "v" whitespace removed; otherwise trimmed.
         */
        fun custom(custom: Map<String, String>?) = apply {
            this.custom = custom
        }

        /**
         * Sets pre-encoded EID strings to include as-is.
         *
         * @param raw Pre-encoded EID strings to include as-is (no encoding applied).
         */
        fun raw(raw: List<String>?) = apply {
            this.raw = raw
        }

        /**
         * Constructs the final immutable OptableIdentifiers object.
         */
        fun build(): OptableIdentifiers {
            return OptableIdentifiers(
                email = email,
                phoneNumber = phoneNumber,
                postalCode = postalCode,
                ipv4Address = ipv4Address,
                ipv6Address = ipv6Address,
                appleIdfa = appleIdfa,
                googleGaid = googleGaid,
                rokuRida = rokuRida,
                samsungTifa = samsungTifa,
                amazonFireAfai = amazonFireAfai,
                netId = netId,
                id5 = id5,
                utiq = utiq,
                custom = custom,
                raw = raw,
            )
        }
    }
}