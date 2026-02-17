package co.optable.demoappjava.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import co.optable.android_sdk.OptableIdentifier;
import co.optable.android_sdk.OptableResult;
import co.optable.android_sdk.OptableSDK;
import co.optable.android_sdk.OptableTargeting;
import co.optable.demoappjava.R;
import co.optable.demoappjava.TheApplication;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.common.collect.Lists;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.TargetingParams;

import java.util.*;

public class PrebidBannerFragment extends Fragment {

    private static final String GAM_AD_UNIT_ID = "/21808260008/prebid_demo_app_original_api_banner";
    private static final String PREBID_CONFIG_ID = "prebid-demo-banner-320-50";
    private static final int WIDTH = 320;
    private static final int HEIGHT = 50;

    private AdManagerAdView adView;
    private BannerAdUnit prebidAdUnit;

    private ViewGroup adContainer;
    private TextView statusTextView;

    private OptableSDK optable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_prebid, container, false);
        initUi(root);
        optable = TheApplication.optable;
        return root;
    }

    private void initUi(View root) {
        statusTextView = root.findViewById(R.id.statusTextView);
        adContainer = root.findViewById(R.id.adContainer);

        root.findViewById(R.id.btnLoadBanner).setOnClickListener(view -> onClickLoadAd());
        root.findViewById(R.id.btnCachedBanner).setOnClickListener(view -> onClickCachedBanner());
        root.findViewById(R.id.btnClearCache).setOnClickListener(view -> onClickClearCache());
    }

    /**
     * Loads targeting data and then the GAM banner
     */
    private void onClickLoadAd() {
        statusTextView.setText("");

        ArrayList<OptableIdentifier.Email> ids = Lists.newArrayList(
                new OptableIdentifier.Email("test@test.com")
        );
        optable.targeting(ids, optableResult -> {
            OptableTargeting optableTargeting = null;
            if (optableResult instanceof OptableResult.Success<OptableTargeting> success) {
                optableTargeting = success.getData();
                changeStatusText("Targeting success");
                appendStatusText("GAM targeting keywords: " + optableTargeting.getGamTargetingKeywords());
                appendStatusText("OpenRTB JSON: " + optableTargeting.getOpenRtbJson());
                appendStatusText("Targeting data: " + optableTargeting.getTargetingData());
            } else if (optableResult instanceof OptableResult.Error<OptableTargeting> error) {
                changeStatusText("Targeting error: " + error.getMessage());
            }

            loadPrebidAd(optableTargeting);
            profile();
            witness();
        });
    }

    private void loadPrebidAd(@Nullable OptableTargeting optableTargeting) {
        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

        prebidAdUnit = new BannerAdUnit(PREBID_CONFIG_ID, WIDTH, HEIGHT);
        applyOptableToPrebid(optableTargeting);
        prebidAdUnit.fetchDemand(adRequestBuilder, resultCode -> {
            appendStatusText("Prebid ads loading status: " + resultCode.toString());
            loadGamAd(adRequestBuilder, optableTargeting);
        });
    }

    private void loadGamAd(AdManagerAdRequest.Builder requestBuilder, @Nullable OptableTargeting optableTargeting) {
        applyOptableToGam(requestBuilder, optableTargeting);

        adContainer.removeAllViews();

        adView = new AdManagerAdView(requireContext());
        adView.setAdUnitId(GAM_AD_UNIT_ID);
        adView.setAdSizes(new com.google.android.gms.ads.AdSize(WIDTH, HEIGHT));
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                appendStatusText("Google ad loaded");
            }

            @Override
            public void onAdFailedToLoad(@NonNull @NotNull LoadAdError loadAdError) {
                appendStatusText("Google ad failed to load: " + loadAdError.getMessage());
            }
        });
        adView.loadAd(requestBuilder.build());

        adContainer.addView(adView);
    }

    /**
     * Loads targeting data from cache and then the GAM banner
     */
    private void onClickCachedBanner() {
        OptableTargeting targeting = optable.targetingFromCache();
        if (targeting != null) {
            changeStatusText("Targeting success");
            appendStatusText("GAM targeting keywords: " + targeting.getGamTargetingKeywords());
            appendStatusText("OpenRTB JSON: " + targeting.getOpenRtbJson());
            appendStatusText("Targeting data: " + targeting.getTargetingData());
        } else {
            changeStatusText("Targeting data cache empty.");
        }

        loadPrebidAd(targeting);
        profile();
        witness();
    }

    /**
     * Clears targeting data cache.
     */
    private void onClickClearCache() {
        statusTextView.setText("Clearing targeting data cache.\n\n");
        optable.targetingClearCache();
    }

    private void applyOptableToPrebid(@Nullable OptableTargeting optableResult) {
        if (optableResult == null) {
            TargetingParams.setGlobalOrtbConfig(null);
            return;
        }

        String openRtbJson = optableResult.getOpenRtbJson();
        if (openRtbJson != null) {
            TargetingParams.setGlobalOrtbConfig(openRtbJson);
        }
    }

    private void applyOptableToGam(AdManagerAdRequest.Builder builder, @Nullable OptableTargeting targeting) {
        if (targeting == null) return;

        Map<String, List<String>> audiences = targeting.getGamTargetingKeywords();
        if (audiences != null) {
            for (Map.Entry<String, List<String>> entry : audiences.entrySet()) {
                builder.addCustomTargeting(entry.getKey(), entry.getValue());
            }
        }
    }

    private void profile() {
        HashMap<String, Object> traits = new HashMap<>();
        traits.put("gender", "F");
        traits.put("age", 38);
        traits.put("hasAccount", true);
        traits.put("sampleFloat", 0.75);

        optable.profile(traits, "c:12", Set.of("c:id1", "c:id2"), result -> {
            if (result instanceof OptableResult.Success) {
                appendStatusText("Profile Success");
            } else if (result instanceof OptableResult.Error<OptableTargeting> error) {
                appendStatusText("Profile Error: " + error.getMessage());
            }
        });
    }

    private void witness() {
        HashMap<String, Object> eventProperties = new HashMap<>();
        eventProperties.put("exampleKey", "exampleValue");
        eventProperties.put("exampleKey2", 123);
        eventProperties.put("exampleKey3", false);

        optable.witness("GAMBannerFragment.loadAdButtonClicked", eventProperties, result -> {
            if (result instanceof OptableResult.Success) {
                appendStatusText("Witness Success");
            } else if (result instanceof OptableResult.Error<Unit> error) {
                appendStatusText("Witness Error: " + error.getMessage());
            }
        });
    }

    private void changeStatusText(@NonNull String message) {
        statusTextView.setText(message);
    }

    private void appendStatusText(@NonNull String message) {
        statusTextView.append("\n\n" + message);
    }

}
