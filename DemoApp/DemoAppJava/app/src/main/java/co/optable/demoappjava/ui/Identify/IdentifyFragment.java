package co.optable.demoappjava.ui.Identify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import co.optable.android_sdk.OptableSDK;
import co.optable.demoappjava.MainActivity;
import co.optable.demoappjava.R;

public class IdentifyFragment extends Fragment {
    private TextView identifyView;
    private EditText emailText;
    private Switch gaidSwitch;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_identify, container, false);
        identifyView = root.findViewById(R.id.identifyView);
        emailText = root.findViewById(R.id.editTextTextEmailAddress);
        gaidSwitch = root.findViewById(R.id.gaidSwitch);

        Button btn = root.findViewById(R.id.identifyButton);
        btn.setOnClickListener(view ->
                MainActivity.OPTABLE
                    .identify(emailText.getText().toString(), gaidSwitch.isChecked())
                    .observe(getViewLifecycleOwner(), result -> {
                        String msg = "Calling identify API... ";

                        if (result.getStatus() == OptableSDK.Status.SUCCESS) {
                            msg += "Success";
                        } else {
                            msg += "\n\nOptableSDK Error: " + result.getMessage();
                        }

                        identifyView.setText(msg);
                    })
        );

        return root;
    }
}
