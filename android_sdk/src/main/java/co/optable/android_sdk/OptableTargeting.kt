package co.optable.android_sdk

/**
 * @param gamTargetingKeywords Targeting keywords for GAM.
 * Should be applied to `AdManagerAdRequest.Builder.addCustomTargeting`.
 * @param openRtbJson Partial OpenRTB JSON string. Should be merged with OpenRTB request.
 * For Prebid SDK use `TargetingParams.setGlobalOrtbConfig`.
 */
data class OptableTargeting(
    val gamTargetingKeywords: Map<String, List<String>>,
    val openRtbJson: String?,
)
