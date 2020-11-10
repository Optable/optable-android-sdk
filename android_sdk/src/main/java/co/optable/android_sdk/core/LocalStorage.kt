/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk.core

import android.content.Context
import androidx.preference.PreferenceManager
import co.optable.android_sdk.Config
import co.optable.android_sdk.OptableTargetingResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

internal class LocalStorage(private val config: Config, private val context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(this.context)
    private val passportKey = this.config.passportKey()
    private val targetingKey = this.config.targetingKey()

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