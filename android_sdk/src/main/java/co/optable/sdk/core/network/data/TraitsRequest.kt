package co.optable.sdk.core.network.data

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
        fun from(traits: Map<String, Any>, id: String?, neighbors: Set<String>): TraitsRequest {
            val onlyPrimitivesMap = HashMap<String, Any>(traits.size)
            for (trait in traits) {
                val isPrimitive =
                    trait.value is Boolean || trait.value is String || trait.value is Int || trait.value is Long || trait.value is Double || trait.value is Float
                if (isPrimitive) {
                    onlyPrimitivesMap[trait.key] = trait.value
                } else {
                    onlyPrimitivesMap[trait.key] = trait.value.toString()
                }
            }

            return TraitsRequest(
                id = id,
                neighbors = if (neighbors.isEmpty()) null else neighbors.toList(),
                traits = onlyPrimitivesMap
            )
        }
    }

}
