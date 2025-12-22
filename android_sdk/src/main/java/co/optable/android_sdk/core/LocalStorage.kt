/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk.core

import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import co.optable.android_sdk.OptableConfig
import co.optable.android_sdk.OptableTargeting
import com.google.gson.Gson

internal class LocalStorage(
    config: OptableConfig,
) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(config.context)
    private val passportKey = generateUniqueKey("PASS", config)
    private val targetingKey = generateUniqueKey("TGT", config)

    fun getPassport(): String? {
        return prefs.getString(passportKey, null)
    }

    fun setPassport(passport: String) {
        prefs.edit(commit = true) {
            putString(passportKey, passport)
        }
    }

    fun getTargeting(): OptableTargeting? {
        try {
            val targeting = prefs.getString(targetingKey, null) ?: return null
            return Gson().fromJson(targeting, OptableTargeting::class.java)
        } catch (e: Exception) {
            Log.e("OptableSDK", "Can't parse cached targeting: ${e.message}. Clearing...")
            prefs.edit(commit = true) { remove(targetingKey) }
        }
        return null
    }

    fun setTargeting(targeting: OptableTargeting) {
        try {
            prefs.edit(commit = true) {
                putString(targetingKey, Gson().toJson(targeting))
            }
        } catch (e: Exception) {
            Log.e("OptableSDK", "Can't set targeting: ${e.message}")
        }
    }

    fun clearTargeting() {
        prefs.edit(commit = true) {
            remove(targetingKey)
        }
    }

    private fun generateUniqueKey(kind: String, config: OptableConfig): String {
        val sfx = "${config.host}/${config.tenant}/${config.originSlug}"
        return "OPTABLE_" + kind + "_" + Base64.encodeToString(sfx.toByteArray(), 0)
    }

}