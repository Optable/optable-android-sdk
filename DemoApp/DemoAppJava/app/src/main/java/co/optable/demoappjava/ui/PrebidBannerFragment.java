package co.optable.demoappjava.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import co.optable.android_sdk.OptableResponse;
import co.optable.android_sdk.OptableResult;
import co.optable.android_sdk.OptableTargeting;
import co.optable.demoappjava.MainActivity;
import co.optable.demoappjava.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.common.collect.Lists;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_prebid, container, false);
        initUi(root);
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

        ArrayList<String> ids = Lists.newArrayList("e:5837d278eabede28e37b5766399ed0d1a4cdc36acee8d35710a255032f45beda");
        MainActivity.OPTABLE
                .targeting(ids, result -> {
                    AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();

                    String optableOpenRtbJson = null;
                    if (result instanceof OptableResult.Success<OptableTargeting> success) {
                        changeStatusText("Optable Success", null);
                        optableOpenRtbJson = success.getResult().getOpenRtbJson();
                    } else if (result instanceof OptableResult.Error<OptableTargeting> error) {
                        changeStatusText("Optable Error: " + error.getMessage(), null);
                    }

                    loadPrebidAd(adRequestBuilder, optableOpenRtbJson);
                    profile();
                    witness();
                });
    }

    private void loadPrebidAd(AdManagerAdRequest.Builder adRequestBuilder, @Nullable String optableOpenRtbJson) {
        prebidAdUnit = new BannerAdUnit(PREBID_CONFIG_ID, WIDTH, HEIGHT);
        applyOptableToPrebid(optableOpenRtbJson);
        prebidAdUnit.fetchDemand(adRequestBuilder, resultCode -> {
            appendStatusText("Prebid ads loading status: " + resultCode.toString());
            loadGamAd(adRequestBuilder);
        });
    }

    private void applyOptableToPrebid(String optableOpenRtbJson) {
        if (optableOpenRtbJson != null) {
            TargetingParams.setGlobalOrtbConfig(optableOpenRtbJson);
        }
    }

    private void loadGamAd(AdManagerAdRequest.Builder adRequestBuilder) {
        adContainer.removeAllViews();

        AdManagerAdRequest adRequest = adRequestBuilder.build();

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
        adView.loadAd(adRequest);

        adContainer.addView(adView);
    }

    /**
     * Loads targeting data from cache and then the GAM banner
     */
    private void onClickCachedBanner() {
        statusTextView.setText("");

        AdManagerAdRequest.Builder adRequestBuilder = new AdManagerAdRequest.Builder();
        HashMap<String, List<String>> data = MainActivity.OPTABLE.targetingFromCache();

        if (data != null) {
            changeStatusText("Loaded Optable cached targeting data", data);
            for (String key : data.keySet()) {
                List<String> values = data.get(key);
                if (values == null) continue;
                adRequestBuilder.addCustomTargeting(key, values);
            }
        } else {
            changeStatusText("Targeting data cache empty.", null);
        }

        // TODO:
        loadPrebidAd(adRequestBuilder, null);
        profile();
        witness();
    }

    /**
     * Clears targeting data cache.
     */
    private void onClickClearCache() {
        statusTextView.setText("Clearing targeting data cache.\n\n");
        MainActivity.OPTABLE.targetingClearCache();
    }

    private void profile() {
        HashMap<String, Object> traits = new HashMap<>();
        traits.put("gender", "F");
        traits.put("age", 38);
        traits.put("hasAccount", true);

        MainActivity.OPTABLE
                .profile(traits)
                .observe(getViewLifecycleOwner(), result -> {
                    if (result.getStatus() == OptableResponse.Status.SUCCESS) {
                        appendStatusText("Success calling profile API to set traits on user.");
                    } else {
                        appendStatusText("Error during sending profile: " + result.getMessage());
                    }
                });
    }

    private void witness() {
        HashMap<String, Object> eventProperties = new HashMap<>();
        eventProperties.put("exampleKey", "exampleValue");
        eventProperties.put("exampleKey2", 123);
        eventProperties.put("exampleKey3", false);

        MainActivity.OPTABLE
                .witness("GAMBannerFragment.loadAdButtonClicked", eventProperties)
                .observe(getViewLifecycleOwner(), result -> {
                    if (result.getStatus() == OptableResponse.Status.SUCCESS) {
                        appendStatusText("Success calling witness API to log loadAdButtonClicked event.");
                    } else {
                        appendStatusText("Error during sending witness: " + result.getMessage());
                    }
                });
    }

    private void changeStatusText(@NonNull String message, @Nullable HashMap<String, List<String>> optableResponse) {
        StringBuilder formattedMessage = new StringBuilder(message);
        if (optableResponse != null) {
            formattedMessage.append("\n\nTargeting data: ");
            for (Map.Entry<String, ? extends Collection<?>> entry : optableResponse.entrySet()) {
                formattedMessage.append(entry.getKey())
                        .append(" = ")
                        .append(entry.getValue())
                        .append("\n");
            }
        }
        statusTextView.setText(formattedMessage.toString());
    }

    private void appendStatusText(@NonNull String message) {
        statusTextView.append("\n\n" + message);
    }

}