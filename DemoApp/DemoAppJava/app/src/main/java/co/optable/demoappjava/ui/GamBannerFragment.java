package co.optable.demoappjava.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import co.optable.android_sdk.OptableIdentifiers;
import co.optable.android_sdk.OptableResult;
import co.optable.android_sdk.OptableSDK;
import co.optable.android_sdk.OptableTargeting;
import co.optable.demoappjava.R;
import co.optable.demoappjava.TheApplication;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import kotlin.Unit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamBannerFragment extends Fragment {

    private AdManagerAdView mAdView;
    private TextView statusTextView;

    private OptableSDK optable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gambanner, container, false);
        initUi(root);
        optable = TheApplication.optable;
        return root;
    }

    private void initUi(View root) {
        mAdView = root.findViewById(R.id.publisherAdView);
        statusTextView = root.findViewById(R.id.statusTextView);

        root.findViewById(R.id.btnLoadBanner).setOnClickListener(view -> onClickLoadAd());
        root.findViewById(R.id.btnCachedBanner).setOnClickListener(view -> onClickCachedBanner());
        root.findViewById(R.id.btnClearCache).setOnClickListener(view -> onClickClearCache());
    }

    /**
     * Loads targeting data and then the GAM banner
     */
    private void onClickLoadAd() {
        statusTextView.setText("");

        OptableIdentifiers ids = new OptableIdentifiers.Builder().email("test@test.com").build();
        optable.targeting(ids, result -> {
            AdManagerAdRequest.Builder requestBuilder = new AdManagerAdRequest.Builder();

            if (result instanceof OptableResult.Success<OptableTargeting> success) {
                applyOptableToGam(requestBuilder, success.getData());
                changeStatusText("Targeting success: " + success.getData().getGamTargetingKeywords());
            } else if (result instanceof OptableResult.Error<OptableTargeting> error) {
                changeStatusText("Targeting error: " + error.getMessage());
            }

            mAdView.loadAd(requestBuilder.build());
            profile();
            witness();
        });
    }

    /**
     * Loads targeting data from cache and then the GAM banner
     */
    private void onClickCachedBanner() {
        statusTextView.setText("");

        AdManagerAdRequest.Builder requestBuilder = new AdManagerAdRequest.Builder();

        OptableTargeting optableTargeting = optable.targetingFromCache();
        if (optableTargeting != null) {
            changeStatusText("Targeting from cache: " + optableTargeting.getGamTargetingKeywords());
            applyOptableToGam(requestBuilder, optableTargeting);
        } else {
            changeStatusText("Targeting cache is empty");
        }

        mAdView.loadAd(requestBuilder.build());
        profile();
        witness();
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

    /**
     * Clears targeting data cache.
     */
    private void onClickClearCache() {
        statusTextView.setText("Clearing targeting data cache.");
        optable.targetingClearCache();
    }

    private void profile() {
        HashMap<String, Object> traits = new HashMap<>();
        traits.put("gender", "F");
        traits.put("age", 38);
        traits.put("hasAccount", true);

        optable.profile(traits, result -> {
            if (result instanceof OptableResult.Success) {
                appendStatusText("Profile success");
            } else if (result instanceof OptableResult.Error<Unit> error) {
                appendStatusText("Profile error: " + error.getMessage());
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
                appendStatusText("Witness success");
            } else if (result instanceof OptableResult.Error<Unit> error) {
                appendStatusText("Witness error: " + error.getMessage());
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