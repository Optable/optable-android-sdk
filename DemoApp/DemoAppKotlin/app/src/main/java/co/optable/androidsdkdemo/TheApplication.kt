package co.optable.androidsdkdemo

import android.app.Application
import co.optable.sdk.OptableConfig
import co.optable.sdk.OptableSDK

class TheApplication : Application() {

    companion object {
        lateinit var optable: OptableSDK
    }

    override fun onCreate() {
        super.onCreate()

        val config = OptableConfig(this, "prebidtest", "android-sdk")
        optable = OptableSDK(config)
    }

}
