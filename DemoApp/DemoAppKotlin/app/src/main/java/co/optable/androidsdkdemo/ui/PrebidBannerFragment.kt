/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.androidsdkdemo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import co.optable.android_sdk.OptableIdentifier
import co.optable.android_sdk.OptableResult
import co.optable.android_sdk.OptableSDK
import co.optable.android_sdk.OptableTargeting
import co.optable.androidsdkdemo.R
import co.optable.androidsdkdemo.TheApplication
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.prebid.mobile.BannerAdUnit
import org.prebid.mobile.ResultCode
import org.prebid.mobile.TargetingParams

class PrebidBannerFragment : Fragment() {

    companion object {
        private const val GAM_AD_UNIT_ID = "/21808260008/prebid_demo_app_original_api_banner"
        private const val PREBID_CONFIG_ID = "prebid-demo-banner-320-50"
        private const val WIDTH = 320
        private const val HEIGHT = 50
    }

    private lateinit var adView: AdManagerAdView
    private lateinit var prebidAdUnit: BannerAdUnit

    private lateinit var optable: OptableSDK

    private lateinit var adContainer: ViewGroup
    private lateinit var statusTextView: TextView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_prebid, container, false)
        initUi(root)
        optable = TheApplication.optable
        return root
    }

    private fun initUi(root: View) {
        statusTextView = root.findViewById(R.id.statusTextView)
        adContainer = root.findViewById(R.id.adContainer)

        root.findViewById<Button>(R.id.btnLoadBanner).setOnClickListener { onClickLoadAd() }
        root.findViewById<Button>(R.id.btnCachedBanner).setOnClickListener { onClickCachedBanner() }
        root.findViewById<Button>(R.id.btnClearCache).setOnClickListener { onClickClearCache() }
    }

    /**
     * Loads targeting data and then the GAM banner.
     */
    private fun onClickLoadAd() {
        statusTextView.text = ""

        val ids = listOf(OptableIdentifier.Email("test@test.com"))
        optable.targeting(ids) { result ->
            val optableTargeting = when (result) {
                is OptableResult.Success<OptableTargeting> -> {
                    val targeting: OptableTargeting = result.data
                    changeStatusText("Targeting success")
                    appendStatusText("GAM targeting keywords: " + targeting.gamTargetingKeywords)
                    appendStatusText("OpenRTB JSON: " + targeting.openRtbJson)
                    appendStatusText("Targeting data: " + targeting.targetingData)
                    result.data
                }

                is OptableResult.Error -> {
                    changeStatusText("Targeting error: ${result.message}")
                    null
                }
            }

            loadPrebidAd(optableTargeting)

            profile()
            witness()
        }
    }

    private fun loadPrebidAd(optableTargeting: OptableTargeting?) {
        prebidAdUnit = BannerAdUnit(PREBID_CONFIG_ID, WIDTH, HEIGHT)
        applyOptableToPrebid(optableTargeting)

        val requestBuilder = AdManagerAdRequest.Builder()
        prebidAdUnit.fetchDemand(requestBuilder) { resultCode: ResultCode? ->
            appendStatusText("Prebid ads loading status: $resultCode")
            loadGamAd(requestBuilder, optableTargeting)
        }
    }

    private fun loadGamAd(requestBuilder: AdManagerAdRequest.Builder, optableTargeting: OptableTargeting?) {
        adContainer.removeAllViews()

        applyOptableToGam(requestBuilder, optableTargeting)

        adView = AdManagerAdView(requireContext())
        adView.setAdSizes(AdSize(WIDTH, HEIGHT))
        adView.adUnitId = GAM_AD_UNIT_ID
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                appendStatusText("Google ad loaded")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                appendStatusText("Google ad failed to load: " + loadAdError.message)
            }
        }
        adView.loadAd(requestBuilder.build())

        adContainer.addView(adView)
    }


    /**
     * Loads cached targeting and then the GAM banner.
     */
    private fun onClickCachedBanner() {
        val targeting = optable.targetingFromCache()
        if (targeting != null) {
            changeStatusText("Targeting from cache success")
            appendStatusText("GAM targeting keywords: " + targeting.gamTargetingKeywords)
            appendStatusText("OpenRTB JSON: " + targeting.openRtbJson)
            appendStatusText("Targeting data: " + targeting.targetingData)
        } else {
            changeStatusText("Targeting data cache empty.")
        }

        loadPrebidAd(targeting)

        profile()
        witness()
    }

    /**
     * Clears the targeting data cache.
     */
    private fun onClickClearCache() {
        changeStatusText("Cleared targeting data cache.")
        optable.targetingClearCache()
    }


    private fun applyOptableToPrebid(targeting: OptableTargeting?) {
        if (targeting == null) {
            TargetingParams.setGlobalOrtbConfig(null)
            return
        }

        val openRtbJson = targeting.openRtbJson
        if (openRtbJson?.isNotBlank() == true) {
            TargetingParams.setGlobalOrtbConfig(openRtbJson)
        }
    }

    private fun applyOptableToGam(builder: AdManagerAdRequest.Builder, targeting: OptableTargeting?) {
        if (targeting == null) return

        val audiences = targeting.gamTargetingKeywords
        if (audiences != null) {
            for (entry in audiences.entries) {
                builder.addCustomTargeting(entry.key, entry.value)
            }
        }
    }

    private fun profile() {
        val traits = hashMapOf<String, Any>(
            "gender" to "F",
            "age" to 38,
            "hasAccount" to true,
            "sampleFloat" to 0.75,
        )

        optable.profile(traits, "c:12", setOf("c:id1", "c:id2")) { result ->
            when (result) {
                is OptableResult.Success -> {
                    appendStatusText("Profile success")
                }

                is OptableResult.Error -> {
                    appendStatusText("Profile error: ${result.message}")
                }
            }
        }
    }

    private fun witness() {
        val properties = hashMapOf<String, Any>("exampleKey" to "exampleValue", "anotherExample" to 123, "foo" to false)
        optable.witness("GAMBannerFragment.loadAdButtonClicked", properties) { result ->
            when (result) {
                is OptableResult.Success -> {
                    appendStatusText("Witness success")
                }

                is OptableResult.Error -> {
                    appendStatusText("Witness error: ${result.message}")
                }
            }
        }
    }


    private fun changeStatusText(message: String) {
        statusTextView.text = message
    }

    private fun appendStatusText(message: String) {
        statusTextView.append("\n\n$message")
    }

}
