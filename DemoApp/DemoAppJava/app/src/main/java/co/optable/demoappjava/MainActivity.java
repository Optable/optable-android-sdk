package co.optable.demoappjava;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import co.optable.android_sdk.OptableConfig;
import co.optable.android_sdk.OptableSDK;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.InitializationStatus;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static OptableSDK OPTABLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OptableConfig config = new OptableConfig(this, "prebidtest", "js-sdk");
        MainActivity.OPTABLE = new OptableSDK(config);

        initGoogleAds();
        initPrebidSdk();
        initUi();
    }

    private void initGoogleAds() {
        MobileAds.initialize(this, initializationStatus -> {
        });
    }

    private void initPrebidSdk() {
        PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d");
        PrebidMobile.initializeSdk(getApplicationContext(), "https://prebid-server-test-j.prebid.org/openrtb2/auction", status -> {
            if (status == InitializationStatus.SUCCEEDED) {
                Log.d(TAG, "SDK initialized successfully!");
            } else {
                Log.e(TAG, "SDK initialization error: " + status.getDescription());
            }
        });
    }

    private void initUi() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_identify,
                R.id.navigation_gambanner,
                R.id.navigation_prebid
        ).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

}