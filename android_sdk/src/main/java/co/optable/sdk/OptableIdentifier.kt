package co.optable.sdk


/**
 * Parent class for a set of user and device identifiers that can be transformed into Optable EIDs.
 */
sealed class OptableIdentifier {

    /**
     * Email address.
     * Encoding: normalized (whitespace removed, lowercased) and SHA-256 hashed.
     */
    data class Email(val value: String) : OptableIdentifier()

    /**
     * Phone number.
     * Encoding: normalized (whitespace removed, lowercased) and SHA-256 hashed.
     */
    data class PhoneNumber(val value: String) : OptableIdentifier()

    /**
     * Already-hashed Email address (HEM).
     * Encoding: normalized (whitespace removed, lowercased), never hashed again.
     * Dropped if the value is not a SHA-256 digest, so a plaintext Email is never sent.
     */
    data class Hem(val value: String) : OptableIdentifier()

    /**
     * Already-hashed Phone number.
     * Encoding: normalized (whitespace removed, lowercased), never hashed again.
     * Dropped if the value is not a SHA-256 digest.
     */
    data class HashedPhoneNumber(val value: String) : OptableIdentifier()

    /**
     * Postal/ZIP code.
     * Encoding: normalized (whitespace removed, lowercased).
     */
    data class PostalCode(val value: String) : OptableIdentifier()

    /**
     * IPv4 address.
     * Encoding: whitespace removed.
     */
    data class IPv4(val value: String) : OptableIdentifier()

    /**
     * IPv6 address.
     * Encoding: normalized (whitespace removed, lowercased).
     */
    data class IPv6(val value: String) : OptableIdentifier()

    /**
     * Apple IDFA (Identifier for Advertisers).
     * Encoding: normalized (whitespace removed, lowercased).
     */
    data class AppleIdfa(val value: String) : OptableIdentifier()

    /**
     * Google GAID / AAID (Advertising ID).
     * Encoding: normalized (whitespace removed, lowercased).
     */
    data class GoogleGaid(val value: String) : OptableIdentifier()

    /**
     * Roku RIDA.
     * Encoding: normalized (whitespace removed, lowercased).
     */
    data class RokuRida(val value: String) : OptableIdentifier()

    /**
     * Samsung TIFA.
     * Encoding: normalized (whitespace removed, lowercased).
     */
    data class SamsungTifa(val value: String) : OptableIdentifier()

    /**
     * Amazon Fire AFAI.
     * Encoding: normalized (whitespace removed, lowercased).
     */
    data class AmazonFireAfai(val value: String) : OptableIdentifier()

    /**
     * NetID identifier.
     * Encoding: whitespace removed.
     */
    data class NetId(val value: String) : OptableIdentifier()

    /**
     * ID5 identifier.
     * Encoding: whitespace removed.
     */
    data class ID5(val value: String) : OptableIdentifier()

    /**
     * Utiq identifier.
     * Encoding: normalized (whitespace removed, lowercased).
     */
    data class Utiq(val value: String) : OptableIdentifier()

    /**
     * Optable visitor ID.
     * Encoding: whitespace removed.
     */
    data class OptableVID(val value: String) : OptableIdentifier()

    /**
     * Publisher-provided custom identifier, sent with type "c" when `index` is null or 0, or "c1".."cN" otherwise
     * (the absolute value of `index` is used).
     * Encoding: trimmed.
     */
    data class Custom(val index: Int?, val value: String) : OptableIdentifier() {
        constructor(value: String) : this(null, value)
    }

    /**
     * Pre-encoded EID string to include as-is.
     * No encoding applied. Required format: "type:value" (e.g. "c1:value").
     */
    data class Raw(val value: String) : OptableIdentifier()

}
