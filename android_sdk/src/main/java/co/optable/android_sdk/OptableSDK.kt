/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk

import co.optable.android_sdk.core.GoogleAdIdManager
import co.optable.android_sdk.core.IdentifiersEncoder
import co.optable.android_sdk.core.LocalStorage
import co.optable.android_sdk.core.UserAgentHolder
import co.optable.android_sdk.core.network.NetworkClient
import co.optable.android_sdk.core.network.NetworkResponse
import co.optable.android_sdk.core.network.RequestInterceptor
import co.optable.android_sdk.core.network.ResponseInterceptor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/**
 *  OptableSDK provides an API that is used by an Android app developer integrating with an
 *  Optable Sandbox.
 *
 *  An instance of OptableSDK refers to an Optable Sandbox specified by the caller via `host` and
 *  `app` arguments provided to the constructor. The `context` is required in order for the SDK to
 *  build a WebView() used to read the mobile browser's user-agent string value, which is passed
 *  to the Sandbox.
 *
 *  It is possible to create multiple instances of OptableSDK, should the developer want to
 *  integrate with multiple Sandboxes.
 *
 *  The OptableSDK keeps some state in SharedPreferences
 *  (https://developer.android.com/training/data-storage/shared-preferences), a key/value store
 *  persisted across launches of the app. The state is unique to the app+device, and not globally
 *  unique to the app across devices.
 */
class OptableSDK(
    private val config: OptableConfig,
) {

    private val storage = LocalStorage(config)
    private val adIdManager = GoogleAdIdManager(config)
    private val networkClient = createNetworkClient()

    /**
     *  identify(idList) calls the Optable Sandbox "identify" API, passing it the list of IDs
     *  in idList, a list of type-prefixed identifiers.
     *
     *  It is asynchronous, so the caller may call observe() on the returned LiveData and expect
     *  an instance of Response<OptableIdentifyResponse> in the result. Success can be checked by
     *  comparing result.status to OptableSDK.Status.SUCCESS.
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
     *  identify(email, gaid?, ppid?) calls the Optable Sandbox "identify" API, passing it the
     *  SHA-256 of the caller-provided 'email' and, when specified via the 'gaid' Boolean, the
     *  Google Advertising ID of the device. If the 'ppid' String is specified, it will also be
     *  sent for identity resolution.
     *
     *  The function is async, so the caller may call observe() on the returned LiveData and expect
     *  an instance of Response<OptableIdentifyResponse> in the result. Success can be checked by
     *  comparing result.status to OptableSDK.Status.SUCCESS.
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
     *  profile(traits) calls the Optable Sandbox "profile" API in order to associate the
     *  specified keyvalue OptableProfileTraits 'traits', which can be subsequently used for
     *  audience assembly.
     *
     *  It is asynchronous, so the caller may call observe() on the returned LiveData and expect
     *  an instance of Response<OptableProfileResponse> in the result. Success can be checked by
     *  comparing result.status to OptableSDK.Status.SUCCESS. Note that result.data!! will point
     *  to an empty HashMap on success, and can therefore be ignored.
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
     *  targeting() calls the Optable Sandbox "targeting" API, which returns the key-value targeting
     *  data matching the user/device/app.
     *
     *  It is asynchronous, so the caller should call observe() on the returned LiveData and expect
     *  an instance of Response<OptableTargetingResponse> in the result. Success can be checked by
     *  comparing result.status to OptableSDK.Status.SUCCESS, and when successful, result.data!! is
     *  of type OptableTargetingResponse.
     */
    fun targeting(ids: OptableIdentifiers, listener: OptableResultListener<OptableTargeting>) {
        GlobalScope.launch {
            val response = networkClient.targeting(ids)

            MainScope().launch {
                val optableResult = when (response) {
                    is NetworkResponse.Success -> {
                        val targeting = OptableTargeting(response.result.ortb2.toString())
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

    fun targetingFromCache(): OptableTargeting? {
        return storage.getTargeting()
    }

    fun targetingClearCache() {
        storage.clearTargeting()
    }

    /**
     *  witness(event, properties) calls the Optable Sandbox "witness" API in order to log a
     *  specified 'event' (e.g., "app.screenView", "ui.buttonPressed"), with the specified keyvalue
     *  OptableWitnessProperties 'properties', which can be subsequently used for audience assembly.
     *
     *  It is asynchronous, so the caller may call observe() on the returned LiveData and expect
     *  an instance of Response<OptableWitnessResponse> in the result. Success can be checked by
     *  comparing result.status to OptableSDK.Status.SUCCESS. Note that result.data!! will point
     *  to an empty HashMap on success, and can therefore be ignored.
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

    private fun createNetworkClient(): NetworkClient {
        val userAgentHolder = UserAgentHolder(config)
        val requestInterceptor = RequestInterceptor(config, storage, userAgentHolder)
        val responseInterceptor = ResponseInterceptor(storage)
        return NetworkClient(config, requestInterceptor, responseInterceptor)
    }

}

