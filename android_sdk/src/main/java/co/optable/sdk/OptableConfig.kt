package co.optable.sdk

import android.content.Context


/**
 * Configuration class for Optable integration.
 *
 * The optional settings can also be set after construction, which is how Java callers set them without passing
 * every preceding parameter. Settings marked "read when `OptableSDK` is created" must be set before the config is
 * passed to `OptableSDK`; changing them later has no effect on that instance. The others apply from the next call.
 *
 * @param tenant The tenant name associated with the configuration. E.g. `acmeco.optable.co` => `acmeco`.
 * @param originSlug The DCN's Source Slug. E.g. `acmeco-sdk`.
 * @param host The hostname of the Optable endpoint. Default value is "na.edge.optable.co".
 *   Read when `OptableSDK` is created.
 * @param path The API path to be appended to the host. Default value is "v2". Read when `OptableSDK` is created.
 * @param insecure Boolean flag that determines if insecure HTTP should be used instead of HTTPS. Default is false.
 *   Read when `OptableSDK` is created.
 * @param apiKey An optional API key for authentication. If the API Endpoint is enabled as private, a Service Account API key will be required.
 * @param customUserAgent An optional custom user agent string for network requests.
 * @param skipAdvertisingIdDetection Boolean flag to skip the detection of advertising IDs. Default is false.
 * @param consents Optional `OptableConsents` object for providing custom consent information. If not provided, default values will be used.
 *   Read when `OptableSDK` is created; use `OptableSDK.setConsents` to change them afterwards.
 * @param origin An optional value for the HTTP `Origin` header sent with Optable API requests. Unrelated to `originSlug`.
 * @param cacheTtl The targeting cache time to live in seconds. Default value is 24 hours.
 * A value of zero or less disables the cache.
 */
class OptableConfig @JvmOverloads constructor(
    providedContext: Context,
    internal val tenant: String,
    internal val originSlug: String,
    var host: String = "na.edge.optable.co",
    var path: String = "v2",
    var insecure: Boolean = false,
    @Volatile var apiKey: String? = null,
    @Volatile var customUserAgent: String? = null,
    internal val skipAdvertisingIdDetection: Boolean = false,
    var consents: OptableConsents = OptableConsents(),
    var origin: String? = null,
    @Volatile var cacheTtl: Long = DEFAULT_TARGETING_CACHE_TTL_SECONDS,
) {

    companion object {
        const val DEFAULT_TARGETING_CACHE_TTL_SECONDS: Long = 24 * 60 * 60L
    }

    internal val context = providedContext.applicationContext

    internal fun getBaseUrl(): String {
        var proto = "https://"
        if (insecure) {
            proto = "http://"
        }
        return "$proto$host/$path/"
    }

}
