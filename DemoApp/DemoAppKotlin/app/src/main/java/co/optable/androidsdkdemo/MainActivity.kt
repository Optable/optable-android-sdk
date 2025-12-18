/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.androidsdkdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import co.optable.android_sdk.OptableConfig
import co.optable.android_sdk.OptableSDK
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.prebid.mobile.PrebidMobile

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var OPTABLE: OptableSDK

        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val config = OptableConfig(this, "prebidtest", "js-sdk")
        OPTABLE = OptableSDK(config)

        initGoogleAds()
        initPrebidSdk()
        initUi()
    }

    private fun initGoogleAds() {
        MobileAds.initialize(this) {}
    }

    private fun initPrebidSdk() {
        PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d")
        PrebidMobile.initializeSdk(
            applicationContext,
            "https://prebid-server-test-j.prebid.org/openrtb2/auction"
        ) { status: org.prebid.mobile.api.data.InitializationStatus? ->
            if (status == org.prebid.mobile.api.data.InitializationStatus.SUCCEEDED) {
                Log.d(TAG, "SDK initialized successfully!")
            } else {
                Log.e(TAG, "SDK initialization error: ${status?.description}")
            }
        }
    }

    private fun initUi() {
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_identify,
                R.id.navigation_gambanner,
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

}