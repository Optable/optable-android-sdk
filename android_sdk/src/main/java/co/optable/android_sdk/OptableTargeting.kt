package co.optable.android_sdk

data class OptableTargeting(
    val audiences: Map<String, List<String>>?,
    val openRtbJson: String?,
)
