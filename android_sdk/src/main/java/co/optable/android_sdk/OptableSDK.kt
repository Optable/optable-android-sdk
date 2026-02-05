/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk

import android.util.Log
import co.optable.android_sdk.core.*
import co.optable.android_sdk.core.network.NetworkClient
import co.optable.android_sdk.core.network.NetworkResponse
import co.optable.android_sdk.core.network.RequestInterceptor
import co.optable.android_sdk.core.network.ResponseInterceptor
import kotlinx.coroutines.*


/**
 *  OptableSDK provides an API that is used by an Android app developer integrating with an
 *  Optable Sandbox.
 *
 *  An instance of OptableSDK refers to an Optable Sandbox specified by the caller via config provided to the constructor.
 *  The `context` is required in order for the SDK to build a WebView for receiving the user agent and SharedPreferences.
 *
 *  It is possible to create multiple instances of OptableSDK, should the developer want to
 *  integrate with multiple Sandboxes.
 */
class OptableSDK(
    private val config: OptableConfig,
) {

    private val storage = LocalStorage(config)
    private val consentsManager = ConsentsManager(storage)
    private val googleAdIdManager = GoogleAdIdManager(config)
    private val networkClient = createNetworkClient()
    private val useCases = UseCases()
    private val identifiersEncoder = IdentifiersEncoder(googleAdIdManager)

    private val ceh = CoroutineExceptionHandler { _, e -> Log.e("OptableSDK", "Internal exception: $e") }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + ceh)

    init {
        if (!config.skipAdvertisingIdDetection) {
            googleAdIdManager.updateAdvertisingId()
        }
    }

    /**
     * Calls the Optable Sandbox "identify" API, passing it the list of IDs,
     * a list of type-prefixed identifiers.
     */
    fun identify(ids: List<OptableIdentifier>, listener: OptableResultListener<Unit>) {
        scope.launch {
            val encodedIds = identifiersEncoder.encode(ids)
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
     * tryIdentifyFromURI(uri) is a helper that attempts to find a valid-looking "oeid"
     * parameter in the specified uri's query string parameters and, if found, calls
     * this.identify(listOf(oeid)).
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
     * Calls the Optable Sandbox "profile" API to associate the
     * specified key-value traits, which can be subsequently used for
     * audience assembly.
     */
    fun profile(traits: HashMap<String, Any>, listener: OptableResultListener<OptableTargeting>) {
        scope.launch {
            val response = networkClient.profile(traits)

            MainScope().launch {
                val optableResult = when (response) {
                    is NetworkResponse.Success -> {
                        val targeting = useCases.parseTargetingResponse(response.result)
                        OptableResult.Success(targeting)
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
     * Calls the Optable Sandbox "targeting" API, which returns the key-value targeting
     * data matching the user/device/app and part of the OpenRTB code snippet.
     * @see OptableTargeting
     *
     * This type accepts multiple optional identifier values. When EIDs are generated, each supported
     * identifier is encoded according to its identifier type (for example, some values are normalized,
     * whitespace-stripped, or encrypted). Additional identifiers may be supplied through the `Raw` or `Custom` type.
     */
    fun targeting(ids: List<OptableIdentifier>, listener: OptableResultListener<OptableTargeting>) {
        scope.launch {
            val ids = identifiersEncoder.encode(ids)
            val response = networkClient.targeting(ids)

            val optableResult = when (response) {
                is NetworkResponse.Success -> {
                    val targeting = useCases.parseTargetingResponse(response.result)
                    storage.setTargeting(targeting)
                    OptableResult.Success(targeting)
                }

                is NetworkResponse.Error -> {
                    OptableResult.Error(response.message)
                }
            }

            MainScope().launch {
                listener.onComplete(optableResult)
            }
        }
    }

    /**
     * Returns the targeting data from the cache, if available.
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
     * Calls the Optable Sandbox "witness" API to log a
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
        val userAgentHolder = UserAgentHolder(config)
        val requestInterceptor = RequestInterceptor(config, storage, userAgentHolder, consentsManager)
        val responseInterceptor = ResponseInterceptor(storage)
        return NetworkClient(config, requestInterceptor, responseInterceptor)
    }

}

