package co.optable.androidsdkdemo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import co.optable.android_sdk.OptableResult
import co.optable.android_sdk.OptableSDK
import co.optable.androidsdkdemo.R
import co.optable.androidsdkdemo.TheApplication

class IdentifyFragment : Fragment() {

    private lateinit var identifyView: TextView
    private lateinit var emailText: EditText
    private lateinit var gaidSwitch: Switch

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
        identifyView = root.findViewById(R.id.identifyView)
        emailText = root.findViewById(R.id.editTextTextEmailAddress)
        gaidSwitch = root.findViewById(R.id.gaidSwitch)

        root.findViewById<Button>(R.id.identifyButton).setOnClickListener {
            onClickIdentify()
        }
    }

    private fun onClickIdentify() {
        identifyView.text = ""

        val email = emailText.text.toString()
        val gaidStatus = gaidSwitch.isChecked

        optable.identify(email, gaidStatus) { result ->
            val msg = when (result) {
                is OptableResult.Success -> "Identify success"
                is OptableResult.Error -> "Identify error: ${result.message}"
            }
            identifyView.text = msg
        }
    }

}