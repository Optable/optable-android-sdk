package co.optable.sdk.core.network

sealed class NetworkResponse<T> {

    data class Success<T>(
        val result: T,
    ) : NetworkResponse<T>()

    data class Error<T>(
        val message: String,
    ) : NetworkResponse<T>()

}
