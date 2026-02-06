package co.optable.android_sdk.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.preference.PreferenceManager
import co.optable.android_sdk.OptableConfig
import co.optable.android_sdk.OptableTargeting
import com.google.gson.Gson
import io.mockk.*
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

class LocalStorageTest {

    private companion object {
        private const val KEY_SUBJECT_TO_GDPR = "IABTCF_gdprApplies"
        private const val KEY_GDPR_CONSENT = "IABTCF_TCString"
        private const val KEY_GPP_CONSENT = "IABGPP_2_TCString"
    }

    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var localStorage: LocalStorage
    private lateinit var targeting: OptableTargeting

    @Before
    fun setUp() {
        mockSharedPreferences = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)
        val mockContext = mockk<Context>(relaxed = true)

        mockkStatic(PreferenceManager::class, Base64::class, Log::class)
        every { Log.e(any(), any()) } returns 0

        every { PreferenceManager.getDefaultSharedPreferences(any()) } returns mockSharedPreferences

        every { mockSharedPreferences.edit() } returns mockEditor
        every { Base64.encodeToString(any(), any()) } returns "TEST_KEY_SUFFIX"


        val config = OptableConfig(mockContext, "testhost", "testtenant", "testslug")
        localStorage = LocalStorage(config)

        val mockJson = mockk<JSONObject>(relaxed = true)
        targeting = OptableTargeting(
            gamTargetingKeywords = mapOf("foo" to listOf("bar", "baz")),
            openRtbJson = "{}",
            targetingData = mockJson,
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getPassport should return null when not set`() {
        val passportKey = getPrivateKey(localStorage, "passportKey")
        every { mockSharedPreferences.getString(passportKey, null) } returns null
        assertNull(localStorage.getPassport())
    }

    @Test
    fun `setPassport and getPassport should store and retrieve the value`() {
        val passport = "test-passport-value"
        val passportKey = getPrivateKey(localStorage, "passportKey")
        every { mockSharedPreferences.getString(passportKey, null) } returns passport

        localStorage.setPassport(passport)

        verify { mockEditor.putString(passportKey, passport) }
        assertEquals(passport, localStorage.getPassport())
    }

    @Test
    fun `getTargeting should return null when not set`() {
        val targetingKey = getPrivateKey(localStorage, "targetingKey")
        every { mockSharedPreferences.getString(targetingKey, null) } returns null
        assertNull(localStorage.getTargeting())
    }

    @Test
    fun `setTargeting and getTargeting should store and retrieve the value`() {
        val targetingKey = getPrivateKey(localStorage, "targetingKey")
        val targetingJson = Gson().toJson(targeting)
        every { mockSharedPreferences.getString(targetingKey, null) } returns targetingJson

        localStorage.setTargeting(targeting)

        verify { mockEditor.putString(targetingKey, targetingJson) }
        val retrieved = localStorage.getTargeting()?.gamTargetingKeywords
        assertEquals(targeting.gamTargetingKeywords, retrieved)
    }

    @Test
    fun `clearTargeting should remove targeting data`() {
        val targetingKey = getPrivateKey(localStorage, "targetingKey")
        localStorage.clearTargeting()
        verify { mockEditor.remove(targetingKey) }
    }

    @Test
    fun `getTargeting should handle invalid JSON gracefully`() {
        val targetingKey = getPrivateKey(localStorage, "targetingKey")
        every { mockSharedPreferences.getString(targetingKey, null) } returns "{invalid-json}"

        val result = localStorage.getTargeting()

        assertNull(result)
        verify { mockEditor.remove(targetingKey) }
    }

    @Test
    fun `getSubjectToGdpr should return correct values from SharedPreferences`() {
        every { mockSharedPreferences.getInt(KEY_SUBJECT_TO_GDPR, -1) } returns 1
        assertEquals(1, localStorage.getSubjectToGdpr())

        every { mockSharedPreferences.getInt(KEY_SUBJECT_TO_GDPR, -1) } returns 0
        assertEquals(0, localStorage.getSubjectToGdpr())
    }

    @Test
    fun `getSubjectToGdpr should return null when not set`() {
        every { mockSharedPreferences.getInt(KEY_SUBJECT_TO_GDPR, -1) } returns -1
        assertNull(localStorage.getSubjectToGdpr())
    }

    @Test
    fun `getGdprConsent should return value from SharedPreferences`() {
        val consent = "test-gdpr-consent"
        every { mockSharedPreferences.getString(KEY_GDPR_CONSENT, null) } returns consent
        assertEquals(consent, localStorage.getGdprConsent())
    }

    @Test
    fun `getGdprConsent should return null when not set`() {
        every { mockSharedPreferences.getString(KEY_GDPR_CONSENT, null) } returns null
        assertNull(localStorage.getGdprConsent())
    }

    @Test
    fun `getGppConsent should return value from SharedPreferences`() {
        val consent = "test-gpp-consent"
        every { mockSharedPreferences.getString(KEY_GPP_CONSENT, null) } returns consent
        assertEquals(consent, localStorage.getGppConsent())
    }

    @Test
    fun `getGppConsent should return null when not set`() {
        every { mockSharedPreferences.getString(KEY_GPP_CONSENT, null) } returns null
        assertNull(localStorage.getGppConsent())
    }

    private fun getPrivateKey(instance: Any, fieldName: String): String {
        val field: Field = instance.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(instance) as String
    }
}