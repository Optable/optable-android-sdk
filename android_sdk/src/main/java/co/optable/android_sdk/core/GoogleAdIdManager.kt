package co.optable.android_sdk.core

import android.content.Context
import android.text.TextUtils
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

internal class GoogleAdIdManager(
    config: Config,
    private val context: Context,
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
                adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
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