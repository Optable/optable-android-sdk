package co.optable.androidsdkdemo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import co.optable.android_sdk.*
import co.optable.androidsdkdemo.R
import co.optable.androidsdkdemo.TheApplication
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView

class GamBannerFragment : Fragment() {

    private lateinit var adView: AdManagerAdView
    private lateinit var statusTextView: TextView

    private lateinit var optable: OptableSDK

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_gambanner, container, false)
        initUi(root)
        optable = TheApplication.optable
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
        val ids = listOf(OptableIdentifier.Email("test@test.com"))
        optable.targeting(ids) { result ->
            val requestBuilder = AdManagerAdRequest.Builder()

            when (result) {
                is OptableResult.Success<OptableTargeting> -> {
                    val targeting: OptableTargeting = result.data
                    changeStatusText("Targeting success")
                    appendStatusText("GAM targeting keywords: " + targeting.gamTargetingKeywords)
                    appendStatusText("OpenRTB JSON: " + targeting.openRtbJson)
                    appendStatusText("Targeting data: " + targeting.targetingData)
                    applyOptableToGam(requestBuilder, result.data)
                }

                is OptableResult.Error<OptableTargeting> -> {
                    changeStatusText("Targeting error: ${result.message}")
                }
            }

            adView.loadAd(requestBuilder.build())

            profile()
            witness()
        }
    }

    /**
     * Loads cached targeting and then the GAM banner.
     */
    private fun onClickCachedBanner() {
        val requestBuilder = AdManagerAdRequest.Builder()
        val targeting = optable.targetingFromCache()
        if (targeting != null) {
            changeStatusText("Targeting from cache success")
            appendStatusText("GAM targeting keywords: " + targeting.gamTargetingKeywords)
            appendStatusText("OpenRTB JSON: " + targeting.openRtbJson)
            appendStatusText("Targeting data: " + targeting.targetingData)
            applyOptableToGam(requestBuilder, targeting)
        } else {
            changeStatusText("Targeting cache is empty")
        }

        adView.loadAd(requestBuilder.build())

        profile()
        witness()
    }

    /**
     * Clears the targeting data cache.
     */
    private fun onClickClearCache() {
        optable.targetingClearCache()
        changeStatusText("Cleared targeting data cache.")
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

        val traitsRequest = OptableTraits(traits, "c:12", setOf("c:id1", "c:id2"))
        optable.profile(traitsRequest) { result ->
            when (result) {
                is OptableResult.Success<*> -> {
                    appendStatusText("Profile success")
                }

                is OptableResult.Error -> {
                    appendStatusText("Profile error: ${result.message}")
                }
            }
        }
    }

    private fun witness() {
        optable.witness(
            "GAMBannerFragment.loadAdButtonClicked",
            hashMapOf("exampleKey" to "exampleValue", "anotherExample" to 123, "foo" to false)
        ) { result ->
            when (result) {
                is OptableResult.Success<*> -> {
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