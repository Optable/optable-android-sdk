package co.optable.android_sdk.core.network.edge

import com.google.gson.JsonElement

data class TargetingResponse(
    val audience: List<Audience>?,
    val ortb2: JsonElement?,
) {

    data class Audience(
        val provider: String?,
        val ids: List<AudienceId>?,
    )

    data class AudienceId(
        val id: String?,
    )

}