package co.optable.android_sdk

import android.content.Context


/**
 * Configuration class for Optable integration.
 *
 * @param tenant The tenant name associated with the configuration. E.g. `acmeco.optable.co` => `acmeco`.
 * @param originSlug The DCN's Source Slug. E.g. `acmeco-sdk`.
 * @param host The hostname of the Optable endpoint. Default value is "na.edge.optable.co".
 * @param path The API path to be appended to the host. Default value is "v2".
 * @param insecure Boolean flag that determines if insecure HTTP should be used instead of HTTPS. Default is false.
 * @param apiKey An optional API key for authentication. If the API Endpoint is enabled as private, a Service Account API key will be required.
 * @param customUserAgent An optional custom user agent string for network requests.
 * @param skipAdvertisingIdDetection Boolean flag to skip the detection of advertising IDs. Default is false.
 * @param consents Optional `OptableConsents` object for providing custom consent information. If not provided, default values will be used.
 */
class OptableConfig @JvmOverloads constructor(
    providedContext: Context,
    internal val tenant: String,
    internal val originSlug: String,
    internal val host: String = "na.edge.optable.co",
    internal val path: String = "v2",
    internal val insecure: Boolean = false,
    internal val apiKey: String? = null,
    internal val customUserAgent: String? = null,
    internal val skipAdvertisingIdDetection: Boolean = false,
    var consents: OptableConsents = OptableConsents(),
) {

    init {
        OptableIdentifiers.receiveGaidAutomatically = !skipAdvertisingIdDetection
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