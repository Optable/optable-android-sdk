package co.optable.android_sdk.core

import co.optable.android_sdk.OptableConsents
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class ConsentsManagerTest {

    @Mock
    private lateinit var localStorage: LocalStorage

    private lateinit var consentsManager: ConsentsManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `subjectToGdpr should return value from customConsents when it is available`() {
        consentsManager = ConsentsManager(localStorage, OptableConsents(gdprSubject = true))
        assertTrue(consentsManager.subjectToGdpr()!!)

        consentsManager = ConsentsManager(localStorage, OptableConsents(gdprSubject = false))
        assertFalse(consentsManager.subjectToGdpr()!!)
    }

    @Test
    fun `subjectToGdpr should return value from localStorage when customConsents is null`() {
        `when`(localStorage.getSubjectToGdpr()).thenReturn(1)
        consentsManager = ConsentsManager(localStorage)
        assertTrue(consentsManager.subjectToGdpr()!!)

        `when`(localStorage.getSubjectToGdpr()).thenReturn(0)
        consentsManager = ConsentsManager(localStorage)
        assertFalse(consentsManager.subjectToGdpr()!!)
    }

    @Test
    fun `subjectToGdpr should return null when both sources are unavailable`() {
        `when`(localStorage.getSubjectToGdpr()).thenReturn(null)
        consentsManager = ConsentsManager(localStorage)
        assertNull(consentsManager.subjectToGdpr())
    }

    @Test
    fun `gdprConsent should return value from customConsents when it is available`() {
        val consentString = "test_consent"
        consentsManager = ConsentsManager(localStorage, OptableConsents(gdprConsent = consentString))
        assertEquals(consentString, consentsManager.gdprConsent())
    }

    @Test
    fun `gdprConsent should return value from localStorage when customConsents is null`() {
        val consentString = "local_storage_consent"
        `when`(localStorage.getGdprConsent()).thenReturn(consentString)
        consentsManager = ConsentsManager(localStorage)
        assertEquals(consentString, consentsManager.gdprConsent())
    }

    @Test
    fun `gdprConsent should return null when both sources are unavailable`() {
        `when`(localStorage.getGdprConsent()).thenReturn(null)
        consentsManager = ConsentsManager(localStorage)
        assertNull(consentsManager.gdprConsent())
    }

    @Test
    fun `gppConsent should return value from customConsents when it is available`() {
        val gppString = "test_gpp"
        consentsManager = ConsentsManager(localStorage, OptableConsents(gpp = gppString))
        assertEquals(gppString, consentsManager.gppConsent())
    }

    @Test
    fun `gppConsent should return value from localStorage when customConsents is null`() {
        val gppString = "local_storage_gpp"
        `when`(localStorage.getGppConsent()).thenReturn(gppString)
        consentsManager = ConsentsManager(localStorage)
        assertEquals(gppString, consentsManager.gppConsent())
    }

    @Test
    fun `gppConsent should return null when both sources are unavailable`() {
        `when`(localStorage.getGppConsent()).thenReturn(null)
        consentsManager = ConsentsManager(localStorage)
        assertNull(consentsManager.gppConsent())
    }

    @Test
    fun `gppSid should return value from customConsents`() {
        val gppSidString = "test_gpp_sid"
        consentsManager = ConsentsManager(localStorage, OptableConsents(gppSid = gppSidString))
        assertEquals(gppSidString, consentsManager.gppSid())
    }

    @Test
    fun `gppSid should return null when it is not in customConsents`() {
        consentsManager = ConsentsManager(localStorage)
        assertNull(consentsManager.gppSid())
    }

    @Test
    fun `customRegulation should return value from customConsents`() {
        val regString = "test_reg"
        consentsManager = ConsentsManager(localStorage, OptableConsents(reg = regString))
        assertEquals(regString, consentsManager.customRegulation())
    }

    @Test
    fun `customRegulation should return null when it is not in customConsents`() {
        consentsManager = ConsentsManager(localStorage)
        assertNull(consentsManager.customRegulation())
    }
}