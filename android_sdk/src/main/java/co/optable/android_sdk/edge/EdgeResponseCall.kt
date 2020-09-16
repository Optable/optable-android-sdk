/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk.edge

import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException

internal class EdgeResponseCall<S : Any, E : Any>(
    private val delegate: Call<S>,
    private val errorConverter: Converter<ResponseBody, E>
) : Call<EdgeResponse<S, E>> {

    override fun enqueue(callback: Callback<EdgeResponse<S, E>>) {
        return delegate.enqueue(object : Callback<S> {
            override fun onResponse(call: Call<S>, response: Response<S>) {
                val body = response.body()
                val code = response.code()
                val error = response.errorBody()

                if (response.isSuccessful) {
                    if (body != null) {
                        callback.onResponse(
                            this@EdgeResponseCall,
                            Response.success(EdgeResponse.Success(body))
                        )
                    } else {
                        // Response is successful but the body is null
                        callback.onResponse(
                            this@EdgeResponseCall,
                            Response.success(EdgeResponse.UnknownError(null))
                        )
                    }
                } else {
                    val errorBody = when {
                        error == null -> null
                        error.contentLength() == 0L -> null
                        else -> try {
                            errorConverter.convert(error)
                        } catch (ex: Exception) {
                            null
                        }
                    }
                    if (errorBody != null) {
                        callback.onResponse(
                            this@EdgeResponseCall,
                            Response.success(EdgeResponse.ApiError(errorBody, code))
                        )
                    } else {
                        callback.onResponse(
                            this@EdgeResponseCall,
                            Response.success(EdgeResponse.UnknownError(null))
                        )
                    }
                }
            }

            override fun onFailure(call: Call<S>, throwable: Throwable) {
                val edgeResponse = when (throwable) {
                    is IOException -> EdgeResponse.NetworkError(throwable)
                    else -> EdgeResponse.UnknownError(throwable)
                }
                callback.onResponse(this@EdgeResponseCall, Response.success(edgeResponse))
            }
        })
    }

    override fun isExecuted() = delegate.isExecuted

    override fun clone() = EdgeResponseCall(delegate.clone(), errorConverter)

    override fun isCanceled() = delegate.isCanceled

    override fun cancel() = delegate.cancel()

    override fun execute(): Response<EdgeResponse<S, E>> {
        throw UnsupportedOperationException("EdgeResponseCall doesn't support execute")
    }

    override fun request(): Request = delegate.request()
}