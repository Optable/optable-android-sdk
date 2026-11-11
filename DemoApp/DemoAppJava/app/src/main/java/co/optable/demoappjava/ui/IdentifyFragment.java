package co.optable.demoappjava.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import co.optable.demoappjava.R;
import co.optable.demoappjava.TheApplication;
import co.optable.sdk.OptableIdentifier;
import co.optable.sdk.OptableResult;
import co.optable.sdk.OptableSDK;
import kotlin.Unit;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class IdentifyFragment extends Fragment {

    private TextView statusTextView;
    private EditText emailText;

    private OptableSDK optable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_identify, container, false);
        initUi(root);
        optable = TheApplication.optable;
        return root;
    }

    private void initUi(View root) {
        statusTextView = root.findViewById(R.id.statusTextView);
        emailText = root.findViewById(R.id.editTextTextEmailAddress);

        root.findViewById(R.id.identifyButton).setOnClickListener(view -> onClickIdentify());
    }

    private void onClickIdentify() {
        statusTextView.setText("");

        List<OptableIdentifier> ids = Arrays.asList(
                new OptableIdentifier.Email(emailText.getText().toString())
        );
        optable.identify(ids, result -> {
            if (result instanceof OptableResult.Success) {
                statusTextView.setText("Identify success");
                checkVisitorId();
            } else if (result instanceof OptableResult.Error<Unit> error) {
                statusTextView.setText("Identify error: " + error.getMessage());
            }
        });
    }

    private void checkVisitorId() {
        String sfx = "na.edge.optable.co/prebidtest/android-sdk";
        String visitorKey = "OPTABLE_PASS_" + Base64.encodeToString(sfx.getBytes(StandardCharsets.UTF_8), 0);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String visitorId = prefs.getString(visitorKey, "null");
        statusTextView.append("\nVisitor ID: " + visitorId);
    }

}
