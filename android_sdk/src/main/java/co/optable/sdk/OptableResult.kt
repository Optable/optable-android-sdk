package co.optable.sdk

/**
 *  A generic wrapper for various OptableSDK result types.
 *  `Success` represents a successful API call with a result.
 *  `Error` represents an error result with a message.
 */
sealed class OptableResult<T> {

    data class Success<T>(
        val data: T,
    ) : OptableResult<T>()

    data class Error<T>(
        val message: String,
    ) : OptableResult<T>()

}
