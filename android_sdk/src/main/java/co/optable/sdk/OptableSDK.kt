/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.sdk

import android.util.Log
import co.optable.sdk.core.*
import co.optable.sdk.core.network.NetworkClient
import co.optable.sdk.core.network.NetworkResponse
import co.optable.sdk.core.network.RequestInterceptor
import co.optable.sdk.core.network.ResponseInterceptor
import com.google.gson.JsonObject
import kotlinx.coroutines.*


/**
 *  OptableSDK provides an API that is used by an Android app developer integrating with an
 *  Optable Data Connectivity Node (DCN).
 *
 *  An instance of OptableSDK refers to an Optable DCN specified by the caller via config provided to the constructor.
 *  The `context` in the config is used to read the Google Advertising ID, the default user agent, SharedPreferences
 *  and the app's package information.
 *
 *  It is possible to create multiple instances of OptableSDK, should the developer want to
 *  integrate with multiple DCNs.
 */
class OptableSDK(
    private val config: OptableConfig,
) {

    companion object {
        /** The most neighbors the DCN accepts in one profile call; a call with more fails. */
        const val MAX_PROFILE_NEIGHBORS = 64
    }

    private val storage = LocalStorage(config)
    private val consentsManager = ConsentsManager(storage, config.consents)
    private val googleAdIdManager = GoogleAdIdManager(config)
    private val userAgentHolder = UserAgentHolder(config)
    private val appInfoHolder = AppInfoHolder(config)
    private val networkClient = createNetworkClient()
    private val useCases = UseCases()
    private val identifiersEncoder = IdentifiersEncoder()

    private val ceh = CoroutineExceptionHandler { _, e -> Log.e("OptableSDK", "Internal exception: $e") }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + ceh)

    /**
     * Calls the Optable DCN "identify" API, passing it the list of IDs,
     * a list of type-prefixed identifiers.
     */
    fun identify(ids: List<OptableIdentifier>, listener: OptableResultListener<Unit>) {
        scope.launch {
            val googleAdId = googleAdIdManager.getId()
            val encodedIds = identifiersEncoder.encode(ids, googleAdId)
            val response = networkClient.identify(encodedIds)

            MainScope().launch {
                val optableResult = when (response) {
                    is NetworkResponse.Success -> {
                        OptableResult.Success(Unit)
                    }

                    is NetworkResponse.Error -> {
                        OptableResult.Error(response.message)
                    }
                }
                listener.onComplete(optableResult)
            }
        }
    }

    /**
     * tryIdentifyFromUrl(url) is a helper that attempts to find a valid-looking "oeid"
     * parameter in the specified url's query string parameters and, if found, calls
     * this.identify() with it.
     *
     * The use for this is when handling incoming app universal/deep links which might
     * contain an "oeid" value with the SHA256(downcase(email)) of an incoming user, such
     * as encoded links in newsletter Emails sent by the application developer.
     */
    fun tryIdentifyFromUrl(url: String, listener: OptableResultListener<Unit>) {
        val id = identifiersEncoder.prefixedIdFromUrl(url)

        if (id == null) {
            listener.onComplete(OptableResult.Error("Can't find `oeid` in url: $url"))
            return
        }

        this.identify(listOf(OptableIdentifier.Raw(id)), listener)
    }

    /**
     * Calls the Optable DCN "profile" API to associate the
     * specified key-value traits, which can be subsequently used for
     * audience assembly, and returns the targeting data for the profile.
     *
     * On the DCN, profile works like an identify call followed by a targeting call: [neighbors] are linked to the
     * profile, then targeting is resolved for it. The identifiers are encoded like `identify`, the Google Advertising
     * ID is added to [neighbors] like it is to `identify`, and the request carries the same context as `targeting`.
     * The result is cached like `targeting`, so `targetingFromCache()` returns it.
     *
     * @param traits Contains an object composed of key / values to be associated with the profile.
     * Values must be of simple types (String, Int, Boolean, Double) or they will be stringified with `toString`.
     * Keys can be at most 64 characters.
     * @param id The identifier that traits and neighbors will be linked to. This value takes precedence over the
     * passport if provided. If it is dropped when encoded (for example an Hem that isn't a SHA-256 hash), the call fails
     * without being sent, rather than linking the traits to the passport instead.
     * @param neighbors Identifiers to link to the id or passport. At most [MAX_PROFILE_NEIGHBORS] are sent (the
     * Google Advertising ID first, when added); the rest are dropped.
     * @param hids Hint identifiers for third-party identity resolvers, as in `targeting`.
     */
    @JvmOverloads
    fun profile(
        traits: Map<String, Any>,
        id: OptableIdentifier? = null,
        neighbors: List<OptableIdentifier> = emptyList(),
        hids: List<OptableIdentifier> = emptyList(),
        listener: OptableResultListener<OptableTargeting>,
    ) {
        scope.launch {
            val encodedId = id?.let { identifiersEncoder.encode(listOf(it)).firstOrNull() }
            if (id != null && encodedId == null) {
                MainScope().launch {
                    listener.onComplete(OptableResult.Error("Profile id is not a valid ${id::class.simpleName} identifier"))
                }
                return@launch
            }

            val googleAdId = googleAdIdManager.getId()
            var encodedNeighbors = identifiersEncoder.encode(neighbors, googleAdId)
            if (encodedNeighbors.size > MAX_PROFILE_NEIGHBORS) {
                val dropped = encodedNeighbors.size - MAX_PROFILE_NEIGHBORS
                Log.w("OptableSDK", "Profile accepts at most $MAX_PROFILE_NEIGHBORS neighbors, dropping $dropped")
                encodedNeighbors = encodedNeighbors.take(MAX_PROFILE_NEIGHBORS)
            }
            val response = networkClient.profile(
                traits = traits,
                id = encodedId,
                neighbors = encodedNeighbors,
                hids = identifiersEncoder.encode(hids, googleAdId),
                bundle = appInfoHolder.bundle,
                version = appInfoHolder.appVersion,
                userAgent = userAgentHolder.getUserAgent(),
                id5Signature = storage.getId5Signature(),
            )

            val optableResult = toTargetingResult(response)

            MainScope().launch {
                listener.onComplete(optableResult)
            }
        }
    }

    /**
     * Calls the Optable DCN "targeting" API, which returns the key-value targeting
     * data matching the user/device/app and part of the OpenRTB code snippet.
     * @see OptableTargeting
     *
     * This type accepts multiple optional identifier values. When EIDs are generated, each supported
     * identifier is encoded according to its identifier type (for example, some values are normalized,
     * whitespace-stripped, or hashed). Additional identifiers may be supplied through the `Raw` or `Custom` type.
     */
    fun targeting(ids: List<OptableIdentifier>, listener: OptableResultListener<OptableTargeting>) {
        targeting(ids, emptyList(), listener)
    }

    /**
     * Calls the Optable DCN "targeting" API, which returns the key-value targeting
     * data matching the user/device/app and part of the OpenRTB code snippet,
     * additionally forwarding [hids] (hint identifiers) for third-party identity resolvers.
     * @see OptableTargeting
     *
     * This type accepts multiple optional identifier values. When EIDs are generated, each supported
     * identifier is encoded according to its identifier type (for example, some values are normalized,
     * whitespace-stripped, or hashed). Additional identifiers may be supplied through the `Raw` or `Custom` type.
     */
    fun targeting(
        ids: List<OptableIdentifier>,
        hids: List<OptableIdentifier>,
        listener: OptableResultListener<OptableTargeting>,
    ) {
        scope.launch {
            val googleAdId = googleAdIdManager.getId()
            val encodedIds = identifiersEncoder.encode(ids, googleAdId)
            val encodedHids = identifiersEncoder.encode(hids, googleAdId)
            val response = networkClient.targeting(
                ids = encodedIds,
                hids = encodedHids,
                bundle = appInfoHolder.bundle,
                version = appInfoHolder.appVersion,
                userAgent = userAgentHolder.getUserAgent(),
                id5Signature = storage.getId5Signature(),
            )

            val optableResult = toTargetingResult(response)

            MainScope().launch {
                listener.onComplete(optableResult)
            }
        }
    }

    /**
     * Parses a targeting or profile response, which have the same shape, caching the targeting data and the ID5
     * signature on success.
     */
    private fun toTargetingResult(response: NetworkResponse<JsonObject>): OptableResult<OptableTargeting> {
        return when (response) {
            is NetworkResponse.Success -> {
                val targeting = useCases.parseTargetingResponse(response.result)
                storage.setTargeting(targeting)
                useCases.parseId5Signature(response.result)?.let { storage.setId5Signature(it) }
                OptableResult.Success(targeting)
            }

            is NetworkResponse.Error -> {
                OptableResult.Error(response.message)
            }
        }
    }

    /**
     * Returns the targeting data from the cache, if available.
     *
     * Cached data older than `OptableConfig.cacheTtl` seconds (24 hours by default) is considered
     * absent: null is returned and the expired data is cleared from the cache.
     */
    fun targetingFromCache(): OptableTargeting? {
        return storage.getTargeting()
    }

    /**
     * Clears the targeting data from the cache.
     */
    fun targetingClearCache() {
        storage.clearTargeting()
    }

    /**
     * Calls the Optable DCN "witness" API to log a
     * specified 'event' (e.g., "app.screenView", "ui.buttonPressed"), with the specified key-value
     * 'properties', which can be subsequently used for audience assembly.
     */
    fun witness(
        event: String,
        properties: HashMap<String, Any>,
        listener: OptableResultListener<Unit>,
    ) {
        scope.launch {
            val response = networkClient.witness(event, properties)
            MainScope().launch {
                val optableResult = when (response) {
                    is NetworkResponse.Success -> {
                        OptableResult.Success(Unit)
                    }

                    is NetworkResponse.Error -> {
                        OptableResult.Error(response.message)
                    }
                }
                listener.onComplete(optableResult)
            }
        }
    }

    /**
     * Sets custom consents for the Optable SDK.
     * @see OptableConsents
     */
    fun setConsents(consents: OptableConsents) {
        consentsManager.customConsents = consents
    }

    private fun createNetworkClient(): NetworkClient {
        val requestInterceptor = RequestInterceptor(config, storage, userAgentHolder, consentsManager)
        val responseInterceptor = ResponseInterceptor(storage)
        return NetworkClient(config, requestInterceptor, responseInterceptor)
    }

}

