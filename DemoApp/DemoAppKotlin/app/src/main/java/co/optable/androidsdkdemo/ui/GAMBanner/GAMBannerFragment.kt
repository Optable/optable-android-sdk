/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.androidsdkdemo.ui.GAMBanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import co.optable.android_sdk.OptableResponse
import co.optable.android_sdk.OptableTargetingResponse
import co.optable.androidsdkdemo.MainActivity
import co.optable.androidsdkdemo.R
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView

class GAMBannerFragment : Fragment() {

    private lateinit var adView: AdManagerAdView
    private lateinit var statusTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_gambanner, container, false)
        initUi(root)
        return root
    }

    private fun initUi(root: View) {
        adView = root.findViewById(R.id.publisherAdView)
        statusTextView = root.findViewById(R.id.statusTextView)
        statusTextView.text = ""

        root.findViewById<Button>(R.id.btnLoadBanner).setOnClickListener {
            onClickLoadAd()
        }
        root.findViewById<Button>(R.id.btnCachedBanner).setOnClickListener {
            onClickCachedBanner()
        }
        root.findViewById<Button>(R.id.btnClearCache).setOnClickListener {
            onClickClearCache()
        }
    }

    /**
     * Loads targeting data and then the GAM banner.
     */
    private fun onClickLoadAd() {

        MainActivity.OPTABLE
            .targeting()
            .observe(viewLifecycleOwner, Observer { result ->
                val adRequest = AdManagerAdRequest.Builder()

                if (result.status == OptableResponse.Status.SUCCESS) {
                    result.data?.forEach { (key, values) ->
                        adRequest.addCustomTargeting(key, values)
                    }
                    changeStatusText("Loading GAM ad with targeting data.", result.data)
                } else {
                    changeStatusText("Error getting targeting data: ${result.message}")
                }

                adView.loadAd(adRequest.build())

                profile()
                witness()
            })
    }

    /**
     * Loads cached targeting and then the GAM banner.
     */
    private fun onClickCachedBanner() {
        val adRequest = AdManagerAdRequest.Builder()
        val cachedData = MainActivity.OPTABLE.targetingFromCache()
        if (cachedData != null) {
            cachedData.forEach { (key, values) ->
                adRequest.addCustomTargeting(key, values)
            }
            changeStatusText("Loading GAM ad with cached targeting data.", cachedData)
        } else {
            changeStatusText("Targeting data cache empty.")
        }

        adView.loadAd(adRequest.build())

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