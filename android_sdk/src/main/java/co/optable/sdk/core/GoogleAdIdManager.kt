package co.optable.sdk.core

import android.util.Log
import co.optable.sdk.OptableConfig
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeoutOrNull

internal class GoogleAdIdManager(
    val config: OptableConfig,
) {

    companion object {
        const val MIN_TIMEOUT_MS = 1L
    }

    // getAdvertisingIdInfo() blocks and can't be cancelled, so reads run here and a caller stops waiting after
    // config.advertisingIdTimeoutMs (at least MIN_TIMEOUT_MS), leaving a slow read to finish on its own.
    private val readScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Reads the GAID for one API call. Google says not to cache it, since the user can reset it or opt out at any
     * time, so it's read every time. Returns null when skipAdvertisingIdDetection is set, the user has opted out, or
     * the read fails or takes longer than config.advertisingIdTimeoutMs, which is raised to MIN_TIMEOUT_MS when lower:
     * a read always takes some time, so without a wait no GAID could ever be added.
     */
    suspend fun getId(): String? {
        if (config.skipAdvertisingIdDetection) {
            return null
        }

        val read = readScope.async { read() }
        return withTimeoutOrNull(config.advertisingIdTimeoutMs.coerceAtLeast(MIN_TIMEOUT_MS)) {
            read.await()
        }
    }

    private fun read(): String? {
        try {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(config.context)
            if (adInfo.isLimitAdTrackingEnabled) {
                return null
            }
            return adInfo.id
        } catch (exception: Exception) {
            Log.w("OptableGaidManager", "Failed to fetch advertising ID: " + exception.message)
        }
        return null
    }

}
