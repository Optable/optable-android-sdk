package co.optable.android_sdk.core

import co.optable.android_sdk.OptableConfig
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

internal class GoogleAdIdManager(
    val config: OptableConfig,
) {

    companion object {
        private var adId: String? = null
        private var limitAdTracking: Boolean? = null
    }

    fun updateAdvertisingId() {
        GlobalScope.launch {
            var adInfo: AdvertisingIdClient.Info? = null
            try {
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(config.context)
            } catch (_: Exception) {
            }

            MainScope().launch {
                adId = adInfo?.id
                limitAdTracking = adInfo?.isLimitAdTrackingEnabled
            }
        }
    }

    fun getId(): String? {
        if (limitAdTracking == true) {
            return null
        }
        return adId
    }

}
