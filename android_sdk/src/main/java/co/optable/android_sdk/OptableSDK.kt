/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk

import co.optable.android_sdk.core.*
import co.optable.android_sdk.core.network.NetworkClient
import co.optable.android_sdk.core.network.NetworkResponse
import co.optable.android_sdk.core.network.RequestInterceptor
import co.optable.android_sdk.core.network.ResponseInterceptor
import co.optable.android_sdk.core.network.edge.TargetingResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


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
    private val adIdManager = GoogleAdIdManager(config)
    private val networkClient = createNetworkClient()

    /**
     * Calls the Optable Sandbox "identify" API, passing it the list of IDs,
     * a list of type-prefixed identifiers.
     */
    fun identify(ids: OptableIdentifiers, listener: OptableResultListener<Unit>) {
        GlobalScope.launch {
            val response = networkClient.identify(ids)

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
     *  Calls the Optable Sandbox "identify" API, passing only email, GAID and PPID.
     *
     *  @param email email address to identify (encrypted with SHA256)
     *  @param gaid Should receive Google Advertising ID of the device
     *  @param ppid Should receive PPID of the device
     */
    @JvmOverloads
    fun identify(email: String, gaid: Boolean = false, ppid: String? = null, listener: OptableResultListener<Unit>) {
        val emailValue = if (email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email
        } else null

        val gaid = if (gaid) {
            adIdManager.getId()
        } else null

        val ppid = if (ppid != null) {
            listOf("c:$ppid")
        } else null

        val ids = OptableIdentifiers(
            email = emailValue,
            googleGaid = gaid,
            raw = ppid
        )

        return identify(ids, listener)
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
        val id = IdentifiersEncoder.eidFromUrl(url)

        if (id == null) {
            listener.onComplete(OptableResult.Error("Can't find `oeid` in url: $url"))
            return
        }

        val ids = OptableIdentifiers.Builder().raw(listOf(id)).build()
        this.identify(ids, listener)
    }

    /**
     * Calls the Optable Sandbox "profile" API to associate the
     * specified key-value traits, which can be subsequently used for
     * audience assembly.
     */
    fun profile(traits: HashMap<String, Any>, listener: OptableResultListener<Unit>) {
        GlobalScope.launch {
            val response = networkClient.profile(traits)

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
     * Calls the Optable Sandbox "targeting" API, which returns the key-value targeting
     * data matching the user/device/app and part of the OpenRTB code snippet.
     * @see OptableTargeting
     */
    fun targeting(ids: OptableIdentifiers, listener: OptableResultListener<OptableTargeting>) {
        GlobalScope.launch {
            val response = networkClient.targeting(ids)

            MainScope().launch {
                val optableResult = when (response) {
                    is NetworkResponse.Success -> {
                        val targeting = OptableTargeting(
                            parseAudiences(response),
                            response.result.ortb2.toString()
                        )
                        storage.setTargeting(targeting)
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
        GlobalScope.launch {
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
        val consentsManager = ConsentsManager(storage)
        val requestInterceptor = RequestInterceptor(config, storage, userAgentHolder, consentsManager)
        val responseInterceptor = ResponseInterceptor(storage)
        return NetworkClient(config, requestInterceptor, responseInterceptor)
    }


    private fun parseAudiences(response: NetworkResponse.Success<TargetingResponse>): Map<String, List<String>> {
        val audiences = response.result.audience

        if (audiences.isNullOrEmpty()) return emptyMap()

        val result = mutableMapOf<String, List<String>>()
        for (audience in audiences) {
            if (audience.keyspace.isNullOrBlank()) continue

            if (audience.ids.isNullOrEmpty()) continue

            result[audience.keyspace] = audience.ids.mapNotNull { it.id }
        }
        return result
    }

}

