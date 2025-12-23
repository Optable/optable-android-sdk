package co.optable.android_sdk.core

import co.optable.android_sdk.OptableConsents

internal class ConsentsManager(
    private val localStorage: LocalStorage,
    var customConsents: OptableConsents = OptableConsents()
) {

    fun subjectToGdpr(): Boolean? {
        if (customConsents.gdprSubject != null) {
            return customConsents.gdprSubject
        }
        if (localStorage.getSubjectToGdpr() != null) {
            return localStorage.getSubjectToGdpr() == 1
        }
        return null
    }

    fun gdprConsent(): String? {
        if (customConsents.gdprConsent != null) {
            return customConsents.gdprConsent
        }
        if (localStorage.getGdprConsent() != null) {
            return localStorage.getGdprConsent()
        }
        return null
    }

    fun gppConsent(): String? {
        if (customConsents.gpp != null) {
            return customConsents.gpp
        }
        if (localStorage.getGppConsent() != null) {
            return localStorage.getGppConsent()
        }
        return null
    }

    fun gppSid(): String? {
        return customConsents.gppSid
    }

    fun customRegulation(): String? {
        return customConsents.reg
    }

}