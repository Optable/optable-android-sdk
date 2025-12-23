package co.optable.android_sdk.core

import co.optable.android_sdk.OptableConfig

internal class ConsentsManager(
    private val config: OptableConfig,
    private val localStorage: LocalStorage,
) {

    fun subjectToGdpr(): Boolean? {
        val customConsents = config.consents
        if (customConsents.gdprSubject != null) {
            return customConsents.gdprSubject
        }
        if (localStorage.getSubjectToGdpr() != null) {
            return localStorage.getSubjectToGdpr() == 1
        }
        return null
    }

    fun gdprConsent(): String? {
        val customConsents = config.consents
        if (customConsents.gdprConsent != null) {
            return customConsents.gdprConsent
        }
        if (localStorage.getGdprConsent() != null) {
            return localStorage.getGdprConsent()
        }
        return null
    }

    fun gppConsent(): String? {
        val customConsents = config.consents
        if (customConsents.gpp != null) {
            return customConsents.gpp
        }
        if (localStorage.getGppConsent() != null) {
            return localStorage.getGppConsent()
        }
        return null
    }

    fun gppSid(): String? {
        val customConsents = config.consents
        return customConsents.gppSid
    }

}