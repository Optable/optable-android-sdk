package co.optable.sdk

/**
 * Represents the consent information for GDPR and GPP.
 *
 * @param gdprSubject A boolean indicating whether GDPR applies, represented as a integer (0 when it does not apply, 1 when it does). This value should be present when gdpr_consent is supplied.
 * If not set, SDK will try to fetch data from SharedPreferences (key `IABTCF_gdprApplies`), as stated in
 * [standard](https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details)
 * @param gdprConsent TCF EU v2 consent string. If not set, SDK will try to fetch data from SharedPreferences (key `IABTCF_TCString`), as stated in
 * [standard](https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details).
 * @param gpp  GPP privacy string.
 * If not set, SDK will try to fetch data from SharedPreferences (key `IABGPP_2_TCString`), as stated in
 * [standard](https://github.com/InteractiveAdvertisingBureau/GDPR-Transparency-and-Consent-Framework/blob/master/TCFv2/IAB%20Tech%20Lab%20-%20CMP%20API%20v2.md#in-app-details)
 * @param gppSid  A comma-separated list of up to two sections applicable in a given GPP privacy string. This value is required when gpp is present.
 * @param reg Optable privacy regulation override, which can be one of: `gdpr`, `can`, `us`, or `null` and will override all other privacy regulations when present.
 */
data class OptableConsents(
    val gdprSubject: Boolean? = null,
    val gdprConsent: String? = null,
    val gpp: String? = null,
    val gppSid: String? = null,
    val reg: String? = null,
)
