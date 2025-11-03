/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
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

class IdentifyFragment : Fragment() {

    private lateinit var identifyView: TextView
    private lateinit var emailText: EditText
    private lateinit var gaidSwitch: Switch

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_identify, container, false)
        initUi(root)
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

        MainActivity.OPTABLE
            .identify(email, gaidStatus)
            .observe(viewLifecycleOwner, Observer { result ->
                var msg = "Calling identify API... "
                msg += if (result.status == OptableSDK.Status.SUCCESS) {
                    "Success"
                } else {
                    "\n\nOptableSDK Error: ${result.message}"
                }

                identifyView.text = msg
            })
    }

}