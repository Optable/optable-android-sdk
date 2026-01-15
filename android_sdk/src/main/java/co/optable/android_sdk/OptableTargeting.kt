package co.optable.android_sdk

import org.json.JSONObject


/**
 * @param gamTargetingKeywords Targeting keywords for GAM.
 * Should be applied to `AdManagerAdRequest.Builder.addCustomTargeting`.
 * @param openRtbJson Partial OpenRTB JSON string. Should be merged with OpenRTB request.
 * For Prebid SDK use `TargetingParams.setGlobalOrtbConfig`.
 * @param targetingData Complete JSON response. Allows dynamically parsing custom fields.
 */
data class OptableTargeting(
    val gamTargetingKeywords: Map<String, List<String>>,
    val openRtbJson: String?,
    val targetingData: JSONObject,
)
