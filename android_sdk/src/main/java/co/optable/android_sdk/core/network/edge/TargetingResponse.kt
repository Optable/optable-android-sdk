package co.optable.android_sdk.core.network.edge

import com.google.gson.JsonArray
import com.google.gson.JsonElement

data class TargetingResponse(
    val audience: JsonArray?,
    val ortb2: JsonElement?,
)