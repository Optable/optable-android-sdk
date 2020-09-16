/*
 * Copyright © 2020 Optable Technologies Inc. All rights reserved.
 * See LICENSE for details.
 */
package co.optable.android_sdk

class Config(val host: String, val app: String, val insecure: Boolean = false) {

    fun edgeBaseURL(): String {
        var proto = "https://"
        if (this.insecure) {
            proto = "http://"
        }
        return proto + this.host + "/"
    }

    fun passportKey(): String {
        return "OPTABLE_" + this.host + "/" + this.app
    }

}