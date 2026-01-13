package co.optable.androidsdkdemo

import android.app.Application
import co.optable.android_sdk.OptableConfig
import co.optable.android_sdk.OptableSDK

class TheApplication : Application() {

    companion object {
        lateinit var optable: OptableSDK
    }

    override fun onCreate() {
        super.onCreate()

        val config = OptableConfig(this, "prebidtest", "js-sdk")
        optable = OptableSDK(config)
    }

}