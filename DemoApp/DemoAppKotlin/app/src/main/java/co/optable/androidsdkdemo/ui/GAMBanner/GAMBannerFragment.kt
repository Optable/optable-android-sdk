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
import co.optable.android_sdk.OptableSDK
import co.optable.androidsdkdemo.MainActivity
import co.optable.androidsdkdemo.R
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.doubleclick.PublisherAdView

class GAMBannerFragment : Fragment() {

    private lateinit var mPublisherAdView: PublisherAdView
    private lateinit var targetingDataView: TextView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_gambanner, container, false)
        mPublisherAdView = root.findViewById(R.id.publisherAdView)
        targetingDataView = root.findViewById(R.id.targetingDataView)
        targetingDataView.setText("")

        // loadAdButton loads targeting data and then the GAM banner:
        var btn = root.findViewById(R.id.loadAdButton) as Button
        btn.setOnClickListener {
            MainActivity.OPTABLE!!.targeting().observe(viewLifecycleOwner, Observer { result ->
                var msg = ""
                var adRequest = PublisherAdRequest.Builder()

                if (result.status == OptableSDK.Status.SUCCESS) {
                    msg += "Loading GAM ad with targeting data:\n\n"
                    result.data!!.forEach { (key, values) ->
                        adRequest.addCustomTargeting(key, values)
                        msg += "${key} = ${values}\n"
                    }
                } else {
                    msg += "OptableSDK Error: ${result.message}"
                }

                targetingDataView.setText(msg)
                mPublisherAdView.loadAd(adRequest.build())
                witness()
            })
        }

        // loadAdButton2 loads targeting data from cache, and then the GAM banner:
        btn = root.findViewById(R.id.loadAdButton2) as Button
        btn.setOnClickListener {
            var msg = ""
            var adRequest = PublisherAdRequest.Builder()
            var data = MainActivity.OPTABLE!!.targetingFromCache()

            if (data != null) {
                msg += "Loading GAM ad with cached targeting data:\n\n"
                data!!.forEach { (key, values) ->
                    adRequest.addCustomTargeting(key, values)
                    msg += "${key} = ${values}\n"
                }
            } else {
                msg += "Targeting data cache empty."
            }

            targetingDataView.setText(msg)
            mPublisherAdView.loadAd(adRequest.build())
            witness()
        }

        // loadAdButton3 clears targeting data cache:
        btn = root.findViewById(R.id.loadAdButton3) as Button
        btn.setOnClickListener {
            targetingDataView.setText("Clearing targeting data cache.\n\n")
            MainActivity.OPTABLE!!.targetingClearCache()
        }

        return root
    }

    private fun witness() {
        MainActivity.OPTABLE!!
            .witness(
                "GAMBannerFragment.loadAdButtonClicked",
                hashMapOf("exampleKey" to "exampleValue")
            )
            .observe(viewLifecycleOwner, Observer { result ->
                var msg = targetingDataView.text.toString()
                if (result.status == OptableSDK.Status.SUCCESS) {
                    msg += "\n\nSuccess calling witness API to log loadAdButtonClicked event.\n\n"
                } else {
                    msg += "\n\nOptableSDK Error: ${result.message}\n\n"
                }
                targetingDataView.setText(msg)
            })
    }

}