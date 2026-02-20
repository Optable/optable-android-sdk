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
