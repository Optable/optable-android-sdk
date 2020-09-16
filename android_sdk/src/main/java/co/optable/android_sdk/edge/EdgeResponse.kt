/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk.edge

import java.io.IOException

sealed class EdgeResponse<out T: Any, out U: Any> {
    /**
     * Success response with body
     */
    data class Success<T : Any>(val body: T) : EdgeResponse<T, Nothing>()

    /**
     * Failure response with body
     */
    data class ApiError<U: Any>(val body: U, val code: Int): EdgeResponse<Nothing, U>()

    /**
     * Network error
     */
    data class NetworkError(val error: IOException) : EdgeResponse<Nothing, Nothing>()

    /**
     * For example, json parsing error
     */
    data class UnknownError(val error: Throwable?) : EdgeResponse<Nothing, Nothing>()
}