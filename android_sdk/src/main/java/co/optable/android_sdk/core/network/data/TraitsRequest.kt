package co.optable.android_sdk.core.network.data

import co.optable.android_sdk.OptableTraits
import com.google.gson.annotations.SerializedName

data class TraitsRequest(
    @SerializedName("id")
    val id: String?,
    @SerializedName("neighbors")
    val neighbors: List<String>?,
    @SerializedName("traits")
    val traits: Map<String, Any>,
) {

    companion object {
        fun from(domain: OptableTraits): TraitsRequest {
            val onlyPrimitivesMap = HashMap<String, Any>(domain.traits.size)
            for (trait in domain.traits) {
                val isPrimitive = trait.value is Boolean || trait.value is String || trait.value is Int || trait.value is Long || trait.value is Double || trait.value is Float
                if (isPrimitive) {
                    onlyPrimitivesMap[trait.key] = trait.value
                } else {
                    onlyPrimitivesMap[trait.key] = trait.value.toString()
                }
            }

            return TraitsRequest(
                id = domain.id,
                neighbors = if (domain.neighbors.isEmpty()) null else domain.neighbors.toList(),
                traits = onlyPrimitivesMap
            )
        }
    }

}