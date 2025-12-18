package co.optable.demoappjava.ui.GAMBanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import co.optable.android_sdk.OptableResponse;
import co.optable.demoappjava.MainActivity;
import co.optable.demoappjava.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GAMBannerFragment extends Fragment {

    private AdManagerAdView mAdView;
    private TextView statusTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gambanner, container, false);
        initUi(root);
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

        MainActivity.OPTABLE
                .targeting()
                .observe(getViewLifecycleOwner(), result -> {
                    AdManagerAdRequest.Builder adRequest = new AdManagerAdRequest.Builder();

                    if (result.getStatus() == OptableResponse.Status.SUCCESS) {
                        HashMap<String, List<String>> data = result.getData();
                        changeStatusText("Loading GAM ad with targeting data", data);

                        if (data != null) {
                            for (String key : data.keySet()) {
                                List<String> values = data.get(key);
                                if (values == null) continue;
                                adRequest.addCustomTargeting(key, values);
                            }
                        }
                    } else {
                        changeStatusText("Error getting targeting data: " + result.getMessage(), null);
                    }

                    mAdView.loadAd(adRequest.build());
                    profile();
                    witness();
                });
    }

    /**
     * Loads targeting data from cache and then the GAM banner
     */
    private void onClickCachedBanner() {
        statusTextView.setText("");

        AdRequest.Builder adRequest = new AdRequest.Builder();
        HashMap<String, List<String>> data = MainActivity.OPTABLE.targetingFromCache();

        if (data != null) {
            changeStatusText("Loading GAM ad with cached targeting data", data);
            for (String key : data.keySet()) {
                List<String> values = data.get(key);
                if (values == null) continue;
                adRequest.addCustomTargeting(key, values);
            }
        } else {
            changeStatusText("Targeting data cache empty.", null);
        }

        mAdView.loadAd(adRequest.build());
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