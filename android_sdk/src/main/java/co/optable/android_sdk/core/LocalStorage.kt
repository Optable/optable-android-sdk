/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk.core

import android.content.Context
import androidx.preference.PreferenceManager
import co.optable.android_sdk.Config

internal class LocalStorage(private val config: Config, private val context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(this.context)
    private val passportKey = this.config.passportKey()

    fun getPassport(): String? {
        return prefs.getString(passportKey, null)
    }

    fun setPassport(passport: String) {
        val editor = prefs.edit()
        editor.putString(passportKey, passport)
        editor.apply()
    }
}