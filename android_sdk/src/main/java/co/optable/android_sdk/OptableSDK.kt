/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk

import android.net.Uri
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.optable.android_sdk.core.GoogleAdIdManager
import co.optable.android_sdk.core.LocalStorage
import co.optable.android_sdk.core.TypeHasher
import co.optable.android_sdk.core.UserAgentHolder
import co.optable.android_sdk.core.network.NetworkClient
import co.optable.android_sdk.core.network.NetworkResponse
import co.optable.android_sdk.core.network.RequestInterceptor
import co.optable.android_sdk.core.network.ResponseInterceptor
import co.optable.android_sdk.core.network.edge.EdgeResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/*
 * The following typealiases describe the inputs and successful result types of various
 * OptableSDK APIs:
 */

/**
 * Identify API expects a list of type-prefixed ID string values:
 */
typealias OptableIdentifyInput = List<String>

/**
 * Profile API expects user traits:
 */
typealias OptableProfileTraits = HashMap<String, Any>

/**
 * Witness API expects event properties:
 */
typealias OptableWitnessProperties = HashMap<String, Any>

/**
 * Identify, Profile, and Witness APIs usually just return {}... Void would be better but that
 * results in retrofit2 error when parsing response, even when the API responded successfully,
 * since {} is technically a HashMap:
 */
typealias OptableIdentifyResponse = HashMap<Any, Any>
typealias OptableProfileResponse = HashMap<Any, Any>
typealias OptableWitnessResponse = HashMap<Any, Any>

/**
 * Targeting API responds with a key-values dictionary on success:
 */
typealias OptableTargetingResponse = HashMap<String, List<String>>

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
class OptableSDK @JvmOverloads constructor(
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
    fun identify(idList: OptableIdentifyInput, listener: OptableResultListener<Unit>) {
        GlobalScope.launch {
            val response = networkClient.identify(idList)

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
        var idList: OptableIdentifyInput = listOf()

        if (!TextUtils.isEmpty(email) &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        ) {
            idList += TypeHasher.eid(email)
        }

        if (gaid && adIdManager.hasId()) {
            idList += TypeHasher.gaid(adIdManager.getId()!!)
        }

        if ((ppid != null) && (ppid.length > 0)) {
            idList += TypeHasher.cid(ppid)
        }

        return identify(idList, listener)
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
    fun tryIdentifyFromURI(uri: Uri) {
        val oeid = TypeHasher.eidFromURI(uri)

        if (oeid.length > 0) {
            this.identify(listOf(oeid)) {}
        }
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
    fun profile(traits: OptableProfileTraits): LiveData<OptableResponse<OptableProfileResponse>> {
        val liveData = MutableLiveData<OptableResponse<OptableProfileResponse>>()

        GlobalScope.launch {
            val response = networkClient.profile(traits)
            when (response) {
                is EdgeResponse.Success -> {
                    liveData.postValue(OptableResponse.success(response.body))
                }

                is EdgeResponse.ApiError -> {
                    liveData.postValue(OptableResponse.error(response.body))
                }

                is EdgeResponse.NetworkError -> {
                    liveData.postValue(
                        OptableResponse.error(
                            OptableResponse.Error("NetworkError", "None")
                        )
                    )
                }

                is EdgeResponse.UnknownError -> {
                    liveData.postValue(
                        OptableResponse.error(
                            OptableResponse.Error("UnknownError", "None")
                        )
                    )
                }
            }
        }

        return liveData
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
    fun targeting(): LiveData<OptableResponse<OptableTargetingResponse>> {
        val liveData = MutableLiveData<OptableResponse<OptableTargetingResponse>>()

        GlobalScope.launch {
            val response = networkClient.targeting()
            when (response) {
                is EdgeResponse.Success -> {
                    storage.setTargeting(response.body)
                    liveData.postValue(OptableResponse.success(response.body))
                }

                is EdgeResponse.ApiError -> {
                    liveData.postValue(OptableResponse.error(response.body))
                }

                is EdgeResponse.NetworkError -> {
                    liveData.postValue(
                        OptableResponse.error(
                            OptableResponse.Error("NetworkError", "None")
                        )
                    )
                }

                is EdgeResponse.UnknownError -> {
                    liveData.postValue(
                        OptableResponse.error(
                            OptableResponse.Error("UnknownError", "None")
                        )
                    )
                }
            }
        }

        return liveData
    }

    fun targeting(idList: OptableIdentifyInput, listener: OptableResultListener<OptableTargeting>) {
        GlobalScope.launch {
            val response = networkClient.targeting(idList)

            MainScope().launch {
                val optableResult = when (response) {
                    is NetworkResponse.Success -> {
                        OptableResult.Success(response.result)
                    }

                    is NetworkResponse.Error -> {
                        OptableResult.Error(response.message)
                    }
                }
                listener.onComplete(optableResult)
            }
        }
    }

    fun targetingFromCache(): OptableTargetingResponse? {
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
        properties: OptableWitnessProperties,
    ): LiveData<OptableResponse<OptableWitnessResponse>> {
        val liveData = MutableLiveData<OptableResponse<OptableWitnessResponse>>()
        val client = this.networkClient

        GlobalScope.launch {
            val response = client.witness(event, properties)
            when (response) {
                is EdgeResponse.Success -> {
                    liveData.postValue(OptableResponse.success(response.body))
                }

                is EdgeResponse.ApiError -> {
                    liveData.postValue(OptableResponse.error(response.body))
                }

                is EdgeResponse.NetworkError -> {
                    liveData.postValue(
                        OptableResponse.error(
                            OptableResponse.Error("NetworkError", "None")
                        )
                    )
                }

                is EdgeResponse.UnknownError -> {
                    liveData.postValue(
                        OptableResponse.error(
                            OptableResponse.Error("UnknownError", "None")
                        )
                    )
                }
            }
        }

        return liveData
    }

    private fun createNetworkClient(): NetworkClient {
        val userAgentHolder = UserAgentHolder(config)
        val requestInterceptor = RequestInterceptor(config, storage, userAgentHolder)
        val responseInterceptor = ResponseInterceptor(storage)
        return NetworkClient(config, requestInterceptor, responseInterceptor)
    }

}

