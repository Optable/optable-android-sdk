/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk.core

import androidx.preference.PreferenceManager
import co.optable.android_sdk.OptableConfig
import co.optable.android_sdk.OptableTargetingResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class LocalStorage(
    config: OptableConfig,
) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(config.context)
    private val passportKey = TypeHasher.passportKey(config)
    private val targetingKey = TypeHasher.targetingKey(config)

    fun getPassport(): String? {
        return prefs.getString(passportKey, null)
    }

    fun setPassport(passport: String) {
        val editor = prefs.edit()
        editor.putString(passportKey, passport)
        editor.apply()
    }

    fun getTargeting(): OptableTargetingResponse? {
        val response = prefs.getString(targetingKey, null)
        if (response == null) {
            return null
        }

        val ttype = object : TypeToken<OptableTargetingResponse>() {}.type
        return Gson().fromJson(response, ttype)
    }

    fun setTargeting(keyvalues: OptableTargetingResponse) {
        val editor = prefs.edit()
        editor.putString(targetingKey, Gson().toJson(keyvalues))
        editor.apply()
    }

    fun clearTargeting() {
        val editor = prefs.edit()
        editor.remove(targetingKey)
        editor.apply()
    }
}