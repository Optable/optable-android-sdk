package co.optable.android_sdk.core

import android.text.TextUtils
import co.optable.android_sdk.OptableConfig
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

internal class GoogleAdIdManager(
    val config: OptableConfig,
) {

    private var adId: String? = null
    private var limitAdTracking: Boolean? = true

    init {
        if (!config.skipAdvertisingIdDetection) {
            updateAdvertisingId()
        }
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

    fun hasId(): Boolean {
        return ((adId != null) && (limitAdTracking == false) && !TextUtils.isEmpty(adId!!))
    }

    fun getId(): String? {
        return adId
    }

}