/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk

import android.util.Base64

class Config(val host: String, val app: String, val insecure: Boolean = false) {

    fun edgeBaseURL(): String {
        var proto = "https://"
        if (this.insecure) {
            proto = "http://"
        }
        return proto + this.host + "/"
    }

    fun passportKey(): String {
        return key("PASS")
    }

    fun targetingKey(): String {
        return key("TGT")
    }

    private fun key(kind: String): String {
        val sfx = this.host + "/" + this.app
        return "OPTABLE_" + kind + "_" + Base64.encodeToString(sfx.toByteArray(), 0)
    }

}