/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.sdk.core

import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import co.optable.sdk.OptableConfig
import co.optable.sdk.OptableTargeting
import com.google.gson.Gson

/**
 * Manages SharedPreferences for Optable SDK.
 */
internal class LocalStorage(
    config: OptableConfig,
) {

    companion object {
        private const val KEY_SUBJECT_TO_GDPR = "IABTCF_gdprApplies"
        private const val KEY_GDPR_CONSENT = "IABTCF_TCString"
        private const val KEY_GPP_CONSENT = "IABGPP_2_TCString"
    }

    private val prefs = PreferenceManager.getDefaultSharedPreferences(config.context)
    private val passportKey = generateUniqueKey("PASS", config)
    private val targetingKey = generateUniqueKey("TGT", config)
    private val id5SignatureKey = generateUniqueKey("ID5SIG", config)

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
            remove(id5SignatureKey)
        }
    }

    fun getId5Signature(): String? {
        return prefs.getString(id5SignatureKey, null)
    }

    fun setId5Signature(signature: String) {
        prefs.edit(commit = true) {
            putString(id5SignatureKey, signature)
        }
    }

    fun getSubjectToGdpr(): Int? {
        try {
            val result = prefs.getInt(KEY_SUBJECT_TO_GDPR, -1)
            if (result == -1) {
                return null
            }
            return result
        } catch (e: Exception) {
            Log.e("OptableSDK", "Can't get subject to GDPR: ${e.message}")
        }
        return null
    }

    fun getGdprConsent(): String? {
        try {
            return prefs.getString(KEY_GDPR_CONSENT, null)
        } catch (e: Exception) {
            Log.e("OptableSDK", "Can't get GDPR consent: ${e.message}")
        }
        return null
    }

    fun getGppConsent(): String? {
        try {
            return prefs.getString(KEY_GPP_CONSENT, null)
        } catch (e: Exception) {
            Log.e("OptableSDK", "Can't get GPP consent: ${e.message}")
        }
        return null
    }

    private fun generateUniqueKey(kind: String, config: OptableConfig): String {
        val sfx = "${config.host}/${config.tenant}/${config.originSlug}"
        return "OPTABLE_" + kind + "_" + Base64.encodeToString(sfx.toByteArray(), 0)
    }

}
