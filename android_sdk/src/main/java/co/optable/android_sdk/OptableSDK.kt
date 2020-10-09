/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk

import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.optable.android_sdk.core.Client
import co.optable.android_sdk.edge.EdgeResponse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.MessageDigest

/*
 * The following typealiases describe the inputs and successful result types of various
 * OptableSDK APIs:
 */

/*
 * Identify API expects a list of type-prefixed ID string values:
 */
typealias OptableIdentifyInput = List<String>

/*
 * Witness API expects event properties:
 */
typealias OptableWitnessProperties = HashMap<String, String>

/*
 * Identify and Witness APIs usually just return {}... Void would be better but that results in
 * retrofit2 error when parsing response, even when the API responded successfully, since {} is
 * technically a HashMap:
 */
typealias OptableIdentifyResponse = HashMap<Any,Any>
typealias OptableWitnessResponse = HashMap<Any,Any>

/*
 * Targeting API responds with a key-values dictionary on success:
 */
typealias OptableTargetingResponse = HashMap<String, List<String>>

/*
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

class OptableSDK @JvmOverloads constructor(context: Context, host: String, app: String, insecure: Boolean = false) {
    val config = Config(host, app, insecure)
    val client = Client(config, context)

    /*
     *  OptableSDK.Status lists all of the possible OptableSDK API result statuses.
     */
    enum class Status {
        SUCCESS,
        ERROR
    }

    /*
     *  OptableSDK.Response is a generic wrapper for various OptableSDK API result types.
     *  It also holds the API result status (OptableSDK.Status) to indicate success or error
     *  resulting from an API call. On success, the response `data` member will hold an instance
     *  of the API response object. On error, the response `message` string provides a description
     *  of the error and related debug information.
     */
    data class Response<out T>(val status: Status, val data: T?, val message: String?) {
        data class Error(val error: String, val trace: String) {}

        companion object {
            fun <T> success(data: T?): Response<T> {
                return Response(Status.SUCCESS, data, null)
            }
            fun <T> error(err: Error): Response<T> {
                return Response(Status.ERROR, null,
                    err.error + " (trace: " + err.trace + ")")
            }
        }
    }

    /*
     *  identify(idList) calls the Optable Sandbox "identify" API, passing it the list of IDs
     *  in idList, a list of type-prefixed identifiers.
     *
     *  It is asynchronous, so the caller may call observe() on the returned LiveData and expect
     *  an instance of Response<OptableIdentifyResponse> in the result. Success can be checked by
     *  comparing result.status to OptableSDK.Status.SUCCESS. Note that result.data!! will point
     *  to an empty HashMap on success, and can therefore be ignored.
     */
    fun identify(idList: OptableIdentifyInput): LiveData<Response<OptableIdentifyResponse>> {
        val liveData = MutableLiveData<Response<OptableIdentifyResponse>>()
        val client = this.client

        GlobalScope.launch {
            val response = client.Identify(idList)
            when (response) {
                is EdgeResponse.Success -> {
                    liveData.postValue(Response.success(response.body))
                }
                is EdgeResponse.ApiError -> {
                    liveData.postValue(Response.error(response.body))
                }
                is EdgeResponse.NetworkError -> {
                    liveData.postValue(Response.error(
                        Response.Error("NetworkError", "None")))
                }
                is EdgeResponse.UnknownError -> {
                    liveData.postValue(Response.error(
                        Response.Error("UnknownError", "None")))
                }
            }
        }

        return liveData
    }

    /*
     *  identify(email, gaid?, ppid?) calls the Optable Sandbox "identify" API, passing it the
     *  SHA-256 of the caller-provided 'email' and, when specified via the 'gaid' Boolean, the
     *  Google Advertising ID of the device. If the 'ppid' String is specified, it will also be
     *  sent for identity resolution.
     *
     *  The function is async, so the caller may call observe() on the returned LiveData and expect
     *  an instance of Response<OptableIdentifyResponse> in the result. Success can be checked by
     *  comparing result.status to OptableSDK.Status.SUCCESS. Note that result.data!! will point
     *  to an empty HashMap on success, and can therefore be ignored.
     */
    @JvmOverloads
    fun identify(email: String, gaid: Boolean? = false, ppid: String? = null):
            LiveData<Response<OptableIdentifyResponse>>
    {
        var idList: OptableIdentifyInput = listOf()

        if (!TextUtils.isEmpty(email) &&
            android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            idList += Companion.eid(email)
        }

        if (gaid!! && this.client.hasGAID()) {
            idList += Companion.gaid(this.client.GAID()!!)
        }

        if ((ppid != null) && (ppid.length > 0)) {
            idList += Companion.cid(ppid)
        }

        return this.identify(idList)
    }

    /*
     *  targeting() calls the Optable Sandbox "targeting" API, which returns the key-value targeting
     *  data matching the user/device/app.
     *
     *  It is asynchronous, so the caller should call observe() on the returned LiveData and expect
     *  an instance of Response<OptableTargetingResponse> in the result. Success can be checked by
     *  comparing result.status to OptableSDK.Status.SUCCESS, and when successful, result.data!! is
     *  of type OptableTargetingResponse.
     */
    fun targeting(): LiveData<Response<OptableTargetingResponse>> {
        val liveData = MutableLiveData<Response<OptableTargetingResponse>>()
        val client = this.client

        GlobalScope.launch {
            val response = client.Targeting()
            when (response) {
                is EdgeResponse.Success -> {
                    liveData.postValue(Response.success(response.body))
                }
                is EdgeResponse.ApiError -> {
                    liveData.postValue(Response.error(response.body))
                }
                is EdgeResponse.NetworkError -> {
                    liveData.postValue(Response.error(
                        Response.Error("NetworkError", "None")))
                }
                is EdgeResponse.UnknownError -> {
                    liveData.postValue(Response.error(
                        Response.Error("UnknownError", "None")))
                }
            }
        }

        return liveData
    }

    /*
     *  witness(event, properties) calls the Optable Sandbox "witness" API in order to log a
     *  specified 'event' (e.g., "app.screenView", "ui.buttonPressed"), with the specified keyvalue
     *  OptableWitnessProperties 'properties', which can be subsequently used for audience assembly.
     *
     *  It is asynchronous, so the caller may call observe() on the returned LiveData and expect
     *  an instance of Response<OptableWitnessResponse> in the result. Success can be checked by
     *  comparing result.status to OptableSDK.Status.SUCCESS. Note that result.data!! will point
     *  to an empty HashMap on success, and can therefore be ignored.
     */
    fun witness(event: String, properties: OptableWitnessProperties):
            LiveData<Response<OptableWitnessResponse>> {
        val liveData = MutableLiveData<Response<OptableWitnessResponse>>()
        val client = this.client

        GlobalScope.launch {
            val response = client.Witness(event, properties)
            when (response) {
                is EdgeResponse.Success -> {
                    liveData.postValue(Response.success(response.body))
                }
                is EdgeResponse.ApiError -> {
                    liveData.postValue(Response.error(response.body))
                }
                is EdgeResponse.NetworkError -> {
                    liveData.postValue(Response.error(
                        Response.Error("NetworkError", "None")))
                }
                is EdgeResponse.UnknownError -> {
                    liveData.postValue(Response.error(
                        Response.Error("UnknownError", "None")))
                }
            }
        }

        return liveData
    }

    companion object {
        /*
         * eid(email) is a helper that returns type-prefixed SHA256(downcase(email))
         */
        fun eid(email: String): String {
            return "e:" + MessageDigest.getInstance("SHA-256")
                .digest(email.toLowerCase().trim().toByteArray())
                .fold("", { str, it -> str + "%02x".format(it) })
        }

        /*
         * gaid(gaid) is a helper that returns the type-prefixed Google Advertising ID
         */
        fun gaid(gaid: String): String {
            return "g:" + gaid.toLowerCase().trim()
        }

        /*
         * cid(ppid) is a helper that returns custom type-prefixed origin-provided PPID
         */
        fun cid(ppid: String): String {
            return "c:" + ppid.trim()
        }
    }
}