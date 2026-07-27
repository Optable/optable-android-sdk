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

        val config = OptableConfig(
            providedContext = this,
            tenant = "prebidtest",
            originSlug = "android-sdk",
            host = "ca.edge.optable.co",
            origin = "https://www.optable.co",
        )
        optable = OptableSDK(config)
    }

}
