package co.optable.androidsdkdemo.ui.Identify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import co.optable.android_sdk.OptableSDK
import co.optable.androidsdkdemo.MainActivity
import co.optable.androidsdkdemo.R
import java.security.MessageDigest

class IdentifyFragment : Fragment() {
    private lateinit var identifyView: TextView
    private lateinit var emailText: EditText
    private lateinit var gaidSwitch: Switch

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_identify, container, false)
        identifyView = root.findViewById(R.id.identifyView)
        emailText = root.findViewById(R.id.editTextTextEmailAddress)
        gaidSwitch = root.findViewById(R.id.gaidSwitch)

        var btn = root.findViewById(R.id.identifyButton) as Button
        btn.setOnClickListener {
            MainActivity.OPTABLE!!
                .identify(emailText.text.toString(), gaidSwitch.isChecked)
                .observe(viewLifecycleOwner, Observer
            { result ->
                var msg = "Calling identify API... "

                if (result.status == OptableSDK.Status.SUCCESS) {
                    msg += "Success"
                } else {
                    msg += "\n\nOptableSDK Error: ${result.message}"
                }

                identifyView.setText(msg)
            })
        }

        return root
    }

    /*
     * eid(email) is a helper that returns SHA256(downcase(email))
     */
    private fun eid(email: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(email.toLowerCase().toByteArray())
            .fold("", { str, it -> str + "%02x".format(it) })
    }
}