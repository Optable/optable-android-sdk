package co.optable.androidsdkdemo.ui

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import co.optable.android_sdk.OptableIdentifiers
import co.optable.android_sdk.OptableResult
import co.optable.android_sdk.OptableSDK
import co.optable.androidsdkdemo.R
import co.optable.androidsdkdemo.TheApplication
import java.nio.charset.StandardCharsets


class IdentifyFragment : Fragment() {

    private lateinit var statusTextView: TextView
    private lateinit var emailText: EditText

    private lateinit var optable: OptableSDK

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_identify, container, false)
        initUi(root)
        optable = TheApplication.optable
        return root
    }

    private fun initUi(root: View) {
        statusTextView = root.findViewById(R.id.statusTextView)
        emailText = root.findViewById(R.id.editTextTextEmailAddress)

        root.findViewById<Button>(R.id.identifyButton).setOnClickListener {
            onClickIdentify()
        }
    }

    private fun onClickIdentify() {
        statusTextView.text = ""

        val ids = OptableIdentifiers.Builder()
            .email(emailText.getText().toString())
            .build()
        optable.identify(ids) { result ->
            val msg = when (result) {
                is OptableResult.Success -> "Identify success"
                is OptableResult.Error -> "Identify error: ${result.message}"
            }
            statusTextView.text = msg
            checkVisitorId()
        }
    }


    private fun checkVisitorId() {
        val sfx = "na.edge.optable.co/prebidtest/js-sdk"
        val visitorKey = "OPTABLE_PASS_" + Base64.encodeToString(sfx.toByteArray(StandardCharsets.UTF_8), 0)

        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val visitorId: String = prefs.getString(visitorKey, "null")!!
        statusTextView.append("\nVisitor ID: " + visitorId)
    }

}