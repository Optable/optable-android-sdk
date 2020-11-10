package co.optable.demoappjava.ui.GAMBanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import java.util.HashMap;
import java.util.List;

import co.optable.android_sdk.OptableSDK;
import co.optable.demoappjava.MainActivity;
import co.optable.demoappjava.R;

public class GAMBannerFragment extends Fragment {
    private PublisherAdView mPublisherAdView;
    private TextView targetingDataView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gambanner, container, false);
        mPublisherAdView = root.findViewById(R.id.publisherAdView);
        targetingDataView = root.findViewById(R.id.targetingDataView);

        // loadAdButton loads targeting data and then the GAM banner:
        Button btn = root.findViewById(R.id.loadAdButton);
        btn.setOnClickListener(view -> {
            targetingDataView.setText("");

            MainActivity.OPTABLE.targeting().observe(getViewLifecycleOwner(), result -> {
                PublisherAdRequest.Builder adRequest = new PublisherAdRequest.Builder();
                final StringBuilder msg = new StringBuilder();
                msg.append(targetingDataView.getText().toString());

                if (result.getStatus() == OptableSDK.Status.SUCCESS) {
                    msg.append("Loading GAM ad with targeting data:\n\n");
                    result.getData().forEach((key, values) -> {
                        adRequest.addCustomTargeting(key, values);
                        msg.append(key.toString() + " = " + values.toString());
                    });
                } else {
                    msg.append("OptableSDK Error: " + result.getMessage());
                }

                targetingDataView.setText(msg.toString());
                mPublisherAdView.loadAd(adRequest.build());
                witness();
            });
        });

        // loadAdButton2 loads targeting data from cache, and then the GAM banner:
        btn = root.findViewById(R.id.loadAdButton2);
        btn.setOnClickListener(view -> {
            targetingDataView.setText("");
            PublisherAdRequest.Builder adRequest = new PublisherAdRequest.Builder();
            final StringBuilder msg = new StringBuilder();
            HashMap<String, List<String>> data = MainActivity.OPTABLE.targetingFromCache();

            if (data != null) {
                msg.append("Loading GAM ad with cached targeting data:\n\n");
                data.forEach((key, values) -> {
                    adRequest.addCustomTargeting(key, values);
                    msg.append(key.toString() + " = " + values.toString());
                });
            } else {
                msg.append("Targeting data cache empty.");
            }

            targetingDataView.setText(msg.toString());
            mPublisherAdView.loadAd(adRequest.build());
            witness();
        });

        // loadAdButton3 clears targeting data cache:
        btn = root.findViewById(R.id.loadAdButton3);
        btn.setOnClickListener(view -> {
            targetingDataView.setText("Clearing targeting data cache.\n\n");
            MainActivity.OPTABLE.targetingClearCache();
        });

        return root;
    }

    private void witness() {
        HashMap<String, String> eventProperties = new HashMap<String, String>();
        eventProperties.put("exampleKey", "exampleValue");

        MainActivity.OPTABLE
                .witness("GAMBannerFragment.loadAdButtonClicked", eventProperties)
                .observe(getViewLifecycleOwner(), result -> {
                    final StringBuilder msg = new StringBuilder();
                    msg.append(targetingDataView.getText().toString());

                    if (result.getStatus() == OptableSDK.Status.SUCCESS) {
                        msg.append("\n\nSuccess calling witness API to log loadAdButtonClicked event.\n\n");
                    } else {
                        msg.append("\n\nOptableSDK Error: " + result.getMessage() + "\n\n");
                    }

                    targetingDataView.setText(msg.toString());
                });
    }
}