package co.optable.sdk.core

import android.content.Context
import android.util.Log
import co.optable.sdk.OptableConfig

/**
 * Resolves and caches platform-specific application information used by resolvers such as
 * ID5 Mobile In-App and Audigent Hadron:
 * - [bundle]: the application's package name (the mobile app's unique identifier).
 * - [appVersion]: the application's version name.
 *
 * Both values are resolved once, at construction, and fall back to `null` (never an empty
 * string) when they cannot be determined.
 */
internal class AppInfoHolder(
    config: OptableConfig,
) {

    val bundle: String? = resolveBundle(config.context)
    val appVersion: String? = resolveAppVersion(config.context)

    private fun resolveBundle(context: Context): String? {
        return try {
            context.packageName?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.w("OptableSDK", "Can't resolve app bundle: ${e.message}")
            null
        }
    }

    private fun resolveAppVersion(context: Context): String? {
        return try {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
                ?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            Log.w("OptableSDK", "Can't resolve app version: ${e.message}")
            null
        }
    }

}
