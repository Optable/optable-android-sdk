package co.optable.android_sdk

/**
 * @param traits Contains an object composed of key / values to be associated with the profile.
 * Values must be of simple types (String, Int, Boolean, Double) or they will be stringified with `toString`.
 * @param id The identifier that traits and neighbors will be linked to. This value takes precedence over the passport if provided.
 * @param neighbors An array of identifiers to link to the id or passport
 */
data class OptableTraits @JvmOverloads constructor (
    val traits: Map<String, Any>,
    val id: String? = null,
    val neighbors: Set<String> = emptySet(),
)