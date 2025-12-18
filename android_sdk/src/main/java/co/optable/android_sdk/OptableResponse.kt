package co.optable.android_sdk

/**
 *  OptableSDK.Response is a generic wrapper for various OptableSDK API result types.
 *  It also holds the API result status (OptableSDK.Status) to indicate success or error
 *  resulting from an API call. On success, the response `data` member will hold an instance
 *  of the API response object. On error, the response `message` string provides a description
 *  of the error and related debug information.
 */
@Deprecated("Will be replaced with OptableResult")
data class OptableResponse<out T>(val status: Status, val data: T?, val message: String?) {

    /**
     * Represents an error object returned from the Optable SDK API responses.
     */
    data class Error(val error: String, val trace: String)

    /**
     *  OptableSDK.Status lists all of the possible OptableSDK API result statuses.
     */
    enum class Status {
        SUCCESS,
        ERROR
    }

    companion object {
        fun <T> success(data: T?): OptableResponse<T> {
            return OptableResponse(Status.SUCCESS, data, null)
        }

        fun <T> error(err: Error): OptableResponse<T> {
            return OptableResponse(Status.ERROR, null, err.error + " (trace: " + err.trace + ")")
        }
    }
}