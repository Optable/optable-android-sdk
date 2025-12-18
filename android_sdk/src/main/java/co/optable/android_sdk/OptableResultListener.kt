package co.optable.android_sdk

fun interface OptableResultListener<T> {

    fun onComplete(response: OptableResult<T>)

}