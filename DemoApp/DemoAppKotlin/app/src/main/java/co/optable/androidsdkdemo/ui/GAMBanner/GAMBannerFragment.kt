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
import co.optable.android_sdk.OptableWitnessProperties
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

        var btn = root.findViewById(R.id.loadAdButton) as Button
        btn.setOnClickListener {
            MainActivity.OPTABLE!!.targeting().observe(viewLifecycleOwner, Observer { result ->
                var msg = "Loading GAM ad with targeting data:\n\n"
                var adRequest = PublisherAdRequest.Builder()

                if (result.status == OptableSDK.Status.SUCCESS) {
                    result.data!!.forEach { (key, values) ->
                        adRequest.addCustomTargeting(key, values)
                        msg += "${key} = ${values}\n"
                    }
                } else {
                    msg += "OptableSDK Error: ${result.message}"
                }

                targetingDataView.setText(msg)
                mPublisherAdView.loadAd(adRequest.build())
            })

            MainActivity.OPTABLE!!
                .witness(
                    "GAMBannerFragment.loadAdButtonClicked",
                    hashMapOf("exampleKey" to "exampleValue")
                )
                .observe(viewLifecycleOwner, Observer { result ->
                var msg = targetingDataView.text.toString()
                if (result.status == OptableSDK.Status.SUCCESS) {
                    msg += "\n\nSuccess calling witness API to log loadAdButtonClicked event.\n"
                } else {
                    msg += "\n\nOptableSDK Error: ${result.message}\n"
                }
                targetingDataView.setText(msg)
            })
        }

        return root
    }

}