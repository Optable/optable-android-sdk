package co.optable.demoappjava.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import co.optable.android_sdk.OptableResult;
import co.optable.android_sdk.OptableSDK;
import co.optable.demoappjava.R;
import co.optable.demoappjava.TheApplication;
import kotlin.Unit;

public class IdentifyFragment extends Fragment {

    private TextView identifyView;
    private EditText emailText;
    private Switch gaidSwitch;

    private OptableSDK optable;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_identify, container, false);
        initUi(root);
        optable = TheApplication.optable;
        return root;
    }

    private void initUi(View root) {
        identifyView = root.findViewById(R.id.identifyView);
        emailText = root.findViewById(R.id.editTextTextEmailAddress);
        gaidSwitch = root.findViewById(R.id.gaidSwitch);

        root.findViewById(R.id.identifyButton).setOnClickListener(view -> onClickIdentify());
    }

    private void onClickIdentify() {
        identifyView.setText("");

        optable.identify(emailText.getText().toString(), gaidSwitch.isChecked(), result -> {
            if (result instanceof OptableResult.Success) {
                identifyView.setText("Identify success");
            } else if (result instanceof OptableResult.Error<Unit> error) {
                identifyView.setText("Identify error: " + error.getMessage());
            }
        });
    }

}
