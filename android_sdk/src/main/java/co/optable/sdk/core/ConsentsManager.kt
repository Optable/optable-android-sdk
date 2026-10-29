package co.optable.sdk.core

import co.optable.sdk.OptableConsents

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
        if (customConsents.gppSid != null) {
            return customConsents.gppSid
        }
        // The CMP's section IDs are only used with the CMP's GPP string, never with a custom one.
        if (customConsents.gpp == null && localStorage.getGppConsent() != null) {
            return localStorage.getGppSid()
        }
        return null
    }

    fun customRegulation(): String? {
        return customConsents.reg
    }

}
