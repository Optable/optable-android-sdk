package co.optable.sdk

fun interface OptableResultListener<T> {

    fun onComplete(response: OptableResult<T>)

}
