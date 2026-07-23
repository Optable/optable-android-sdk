package co.optable.sdk.core

import android.util.Log
import co.optable.sdk.OptableTargeting
import com.google.gson.JsonObject
import org.json.JSONObject

class UseCases {

    fun parseTargetingResponse(responseJson: JsonObject): OptableTargeting {
        val gamTargetingKeywords = parseTargetingKeywords(responseJson)
        val openRtbJson = parseOpenRtbJson(responseJson)
        val targetingData = parseTargetingData(responseJson)
        return OptableTargeting(gamTargetingKeywords, openRtbJson, targetingData)
    }

    fun parseId5Signature(responseJson: JsonObject): String? {
        try {
            val eids = responseJson
                .getAsJsonObject("ortb2")
                ?.getAsJsonObject("user")
                ?.getAsJsonArray("eids") ?: return null

            val refs = responseJson.getAsJsonObject("refs")

            for (eid in eids) {
                // A malformed entry must not abort the scan - a valid ID5 entry may follow it.
                try {
                    val eidObj = eid.asJsonObject
                    val source = eidObj.get("source")?.asString
                    if (source == null || !source.contains("id5", ignoreCase = true)) continue

                    val uids = eidObj.getAsJsonArray("uids") ?: continue
                    for (uid in uids) {
                        val ref = uid.asJsonObject
                            .getAsJsonObject("ext")
                            ?.getAsJsonObject("optable")
                            ?.get("ref")
                            ?.asString ?: continue

                        val signature = refs
                            ?.getAsJsonObject(ref)
                            ?.get("signature")
                            ?.asString
                        if (!signature.isNullOrBlank()) return signature
                    }
                } catch (e: Exception) {
                    Log.d("OptableSDK", "Skipping malformed eid entry: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.d("OptableSDK", "Can't parse ID5 signature: ${e.message}")
        }
        return null
    }

    private fun parseTargetingData(responseJson: JsonObject): JSONObject {
        var targetingData = JSONObject()
        try {
            targetingData = JSONObject(responseJson.toString())
        } catch (e: Exception) {
        }
        return targetingData
    }

    private fun parseOpenRtbJson(responseJson: JsonObject): String? {
        var openRtbJson: String? = null
        try {
            openRtbJson = responseJson.getAsJsonObject("ortb2").toString()
        } catch (e: Exception) {
            Log.d("OptableSDK", "Can't parse OpenRTB: ${e.message}")
        }
        return openRtbJson
    }

    private fun parseTargetingKeywords(responseJson: JsonObject): MutableMap<String, List<String>> {
        val gamTargetingKeywords = mutableMapOf<String, List<String>>()
        try {
            val audienceJsonArray = responseJson.getAsJsonArray("audience")
            for (audience in audienceJsonArray) {
                val keyspace = audience.asJsonObject.get("keyspace").asString

                if (keyspace == null || keyspace.isBlank()) continue

                val ids = audience.asJsonObject.get("ids").asJsonArray
                val gamIds = mutableListOf<String>()
                for (id in ids) {
                    gamIds.add(id.asJsonObject.get("id").asString)
                }

                gamTargetingKeywords[keyspace] = gamIds
            }
        } catch (e: Exception) {
            Log.d("OptableSDK", "Can't parse GAM targeting keywords: ${e.message}")
        }
        return gamTargetingKeywords
    }

}
