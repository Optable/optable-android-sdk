package co.optable.android_sdk

sealed class OptableResult<T> {

    data class Success<T>(
        val result: T,
    ) : OptableResult<T>()

    data class Error<T>(
        val message: String,
    ) : OptableResult<T>()

}