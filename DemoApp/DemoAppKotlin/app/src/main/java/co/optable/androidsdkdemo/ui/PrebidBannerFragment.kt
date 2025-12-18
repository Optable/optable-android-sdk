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
import androidx.lifecycle.Observer
import co.optable.android_sdk.OptableResponse
import co.optable.android_sdk.OptableResult
import co.optable.android_sdk.OptableTargetingResponse
import co.optable.androidsdkdemo.MainActivity
import co.optable.androidsdkdemo.R
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

    private lateinit var adContainer: ViewGroup
    private lateinit var statusTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_prebid, container, false)
        initUi(root)
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


        MainActivity.OPTABLE
            .targeting(listOf("e:5837d278eabede28e37b5766399ed0d1a4cdc36acee8d35710a255032f45beda")) { result ->
                val adRequestBuilder = AdManagerAdRequest.Builder()

                val optableOpenRtbString: String? = when (result) {
                    is OptableResult.Success -> {
                        changeStatusText("Optable Success")
                        result.result.openRtbJson
                    }

                    is OptableResult.Error -> {
                        changeStatusText("Optable Error: ${result.message}")
                        null
                    }
                }

                loadPrebidAd(adRequestBuilder, optableOpenRtbString)

                profile()
                witness()
            }
    }

    private fun loadPrebidAd(
        adRequestBuilder: AdManagerAdRequest.Builder,
        optableOpenRtbJson: String?,
    ) {
        prebidAdUnit = BannerAdUnit(PREBID_CONFIG_ID, WIDTH, HEIGHT)

        applyOptableToPrebid(optableOpenRtbJson)

        prebidAdUnit.fetchDemand(adRequestBuilder) { resultCode: ResultCode? ->
            appendStatusText("Prebid ads loading status: $resultCode")
            loadGamAd(adRequestBuilder)
        }
    }

    private fun applyOptableToPrebid(optableOpenRtbJson: String?) {
        if (optableOpenRtbJson?.isNotBlank() == true) {
            TargetingParams.setGlobalOrtbConfig(optableOpenRtbJson)
        }
    }

    private fun loadGamAd(adRequestBuilder: AdManagerAdRequest.Builder) {
        adContainer.removeAllViews()

        val adRequest = adRequestBuilder.build()

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
        adView.loadAd(adRequest)

        adContainer.addView(adView)
    }


    /**
     * Loads cached targeting and then the GAM banner.
     */
    private fun onClickCachedBanner() {
        val adRequestBuilder = AdManagerAdRequest.Builder()
        val cachedData = MainActivity.OPTABLE.targetingFromCache()
        if (cachedData != null) {
            cachedData.forEach { (key, values) ->
                adRequestBuilder.addCustomTargeting(key, values)
            }
            changeStatusText("Loading GAM ad with cached targeting data.", cachedData)
        } else {
            changeStatusText("Targeting data cache empty.")
        }

        // TODO:
        loadPrebidAd(adRequestBuilder, null)

        profile()
        witness()
    }

    /**
     * Clears the targeting data cache.
     */
    private fun onClickClearCache() {
        MainActivity.OPTABLE.targetingClearCache()
        changeStatusText("Cleared targeting data cache.")
    }

    private fun profile() {
        MainActivity.OPTABLE
            .profile(hashMapOf("gender" to "F", "age" to 38, "hasAccount" to true))
            .observe(viewLifecycleOwner, Observer { result ->
                if (result.status == OptableResponse.Status.SUCCESS) {
                    appendStatusText("Success calling profile API to set traits on user.")
                } else {
                    appendStatusText("Error during sending profile: ${result.message}")
                }
            })
    }

    private fun witness() {
        MainActivity.OPTABLE
            .witness(
                "GAMBannerFragment.loadAdButtonClicked",
                hashMapOf("exampleKey" to "exampleValue", "anotherExample" to 123, "foo" to false)
            )
            .observe(viewLifecycleOwner, Observer { result ->
                if (result.status == OptableResponse.Status.SUCCESS) {
                    appendStatusText("Success calling witness API to log loadAdButtonClicked event.")
                } else {
                    appendStatusText("Error during sending witness: ${result.message}")
                }
            })
    }


    private fun changeStatusText(message: String, optableResponse: OptableTargetingResponse? = null) {
        var formattedMessage = message
        if (optableResponse != null) {
            formattedMessage += "\n\nTargeting data: "
            formattedMessage += optableResponse.map { (key, values) -> "$key = ${values}\n" }
        }
        statusTextView.text = formattedMessage
    }

    private fun appendStatusText(message: String) {
        statusTextView.append("\n\n$message")
    }

}