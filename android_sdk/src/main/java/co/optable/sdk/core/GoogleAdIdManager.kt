package co.optable.sdk.core

import android.util.Log
import co.optable.sdk.OptableConfig
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

internal class GoogleAdIdManager(
    val config: OptableConfig,
) {

    companion object {
        private var adId: String? = null
        private var limitAdTracking: Boolean? = null
    }

    val deferredTask = CompletableDeferred<String?>()

    fun getId(): String? {
        if (limitAdTracking == true) {
            return null
        }
        return adId
    }

    suspend fun fetchAdvertisingId() {
        if (config.skipAdvertisingIdDetection) {
            deferredTask.complete(null)
            return
        }

        val id = withContext(Dispatchers.IO) {
            withTimeoutOrNull(3_000) {
                fetch()
            }
        }
        deferredTask.complete(id)
    }

    private fun fetch(): String? {
        try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(config.context)
            adId = adInfo.id
            limitAdTracking = adInfo.isLimitAdTrackingEnabled
            return adInfo.id
        } catch (exception: Exception) {
            Log.w("OptableGaidManager", "Failed to fetch advertising ID: " + exception.message)
        }
        return null
    }

}
