package co.optable.android_sdk

import org.junit.Assert.*
import org.junit.Test

class OptableConsentsTest {

    @Test
    fun `constructor should correctly initialize properties`() {
        val consents = OptableConsents(
            gdprSubject = true,
            gdprConsent = "test_consent_string",
            gpp = "test_gpp_string",
            gppSid = "test_gpp_sid",
            reg = "gdpr"
        )

        assertEquals(true, consents.gdprSubject)
        assertEquals("test_consent_string", consents.gdprConsent)
        assertEquals("test_gpp_string", consents.gpp)
        assertEquals("test_gpp_sid", consents.gppSid)
        assertEquals("gdpr", consents.reg)
    }

    @Test
    fun `properties should be null by default`() {
        val consents = OptableConsents()

        assertNull(consents.gdprSubject)
        assertNull(consents.gdprConsent)
        assertNull(consents.gpp)
        assertNull(consents.gppSid)
        assertNull(consents.reg)
    }

    @Test
    fun `equals and hashCode should work as expected for data class`() {
        val consents1 = OptableConsents(gdprSubject = true, reg = "gdpr")
        val consents2 = OptableConsents(gdprSubject = true, reg = "gdpr")
        val consents3 = OptableConsents(gdprSubject = false, reg = "us")

        assertEquals(consents1, consents2)
        assertEquals(consents1.hashCode(), consents2.hashCode())

        assertNotEquals(consents1, consents3)
    }

    @Test
    fun `copy should create a new object with modified values`() {
        val originalConsents = OptableConsents(gdprSubject = true, reg = "gdpr")

        val copiedConsents = originalConsents.copy(reg = "us")

        assertNotEquals(originalConsents, copiedConsents)
        assertEquals(true, copiedConsents.gdprSubject)
        assertEquals("us", copiedConsents.reg)

        assertEquals("gdpr", originalConsents.reg)
    }
}