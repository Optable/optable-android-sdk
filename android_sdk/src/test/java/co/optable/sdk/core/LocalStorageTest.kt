package co.optable.sdk.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.preference.PreferenceManager
import co.optable.sdk.OptableConfig
import co.optable.sdk.OptableTargeting
import com.google.gson.Gson
import io.mockk.*
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

class LocalStorageTest {

    private companion object {
        private const val KEY_SUBJECT_TO_GDPR = "IABTCF_gdprApplies"
        private const val KEY_GDPR_CONSENT = "IABTCF_TCString"
        private const val KEY_GPP_CONSENT = "IABGPP_2_TCString"
        private const val HOUR_MS = 60 * 60 * 1000L
    }

    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockContext: Context
    private lateinit var localStorage: LocalStorage
    private lateinit var targeting: OptableTargeting

    @Before
    fun setUp() {
        mockSharedPreferences = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)

        mockkStatic(PreferenceManager::class, Base64::class, Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0

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
        cacheTimestamp(localStorage, System.currentTimeMillis() - HOUR_MS)
        assertNull(localStorage.getTargeting())
    }

    @Test
    fun `getTargeting should not write to the cache when no targeting is stored`() {
        val targetingKey = getPrivateKey(localStorage, "targetingKey")
        every { mockSharedPreferences.getString(targetingKey, null) } returns null
        // No timestamp either, as on a fresh install or right after clearTargeting().
        cacheTimestamp(localStorage, 0L)

        assertNull(localStorage.getTargeting())

        verify(exactly = 0) { mockSharedPreferences.edit() }
    }

    @Test
    fun `setTargeting and getTargeting should store and retrieve the value`() {
        val targetingKey = getPrivateKey(localStorage, "targetingKey")
        val targetingJson = Gson().toJson(targeting)
        every { mockSharedPreferences.getString(targetingKey, null) } returns targetingJson
        cacheTimestamp(localStorage, System.currentTimeMillis() - HOUR_MS)

        localStorage.setTargeting(targeting)

        verify { mockEditor.putString(targetingKey, targetingJson) }
        val retrieved = localStorage.getTargeting()?.gamTargetingKeywords
        assertEquals(targeting.gamTargetingKeywords, retrieved)
    }

    @Test
    fun `setTargeting should store the time at which the data was cached`() {
        val timestampKey = getPrivateKey(localStorage, "targetingTimestampKey")
        val before = System.currentTimeMillis()

        localStorage.setTargeting(targeting)

        val cachedAt = slot<Long>()
        verify { mockEditor.putLong(timestampKey, capture(cachedAt)) }
        assertTrue(cachedAt.captured >= before)
        assertTrue(cachedAt.captured <= System.currentTimeMillis())
    }

    @Test
    fun `clearTargeting should remove targeting data`() {
        val targetingKey = getPrivateKey(localStorage, "targetingKey")
        val timestampKey = getPrivateKey(localStorage, "targetingTimestampKey")
        localStorage.clearTargeting()
        verify { mockEditor.remove(targetingKey) }
        verify { mockEditor.remove(timestampKey) }
    }

    @Test
    fun `getTargeting should return null and clear the cache when the data is older than the TTL`() {
        val targetingKey = getPrivateKey(localStorage, "targetingKey")
        val timestampKey = getPrivateKey(localStorage, "targetingTimestampKey")
        every { mockSharedPreferences.getString(targetingKey, null) } returns Gson().toJson(targeting)
        // The default TTL is 24 hours.
        cacheTimestamp(localStorage, System.currentTimeMillis() - 25 * HOUR_MS)

        assertNull(localStorage.getTargeting())

        verify { mockEditor.remove(targetingKey) }
        verify { mockEditor.remove(timestampKey) }
    }

    @Test
    fun `getTargeting should return null and clear the cache when the timestamp is in the future`() {
        val targetingKey = getPrivateKey(localStorage, "targetingKey")
        val timestampKey = getPrivateKey(localStorage, "targetingTimestampKey")
        every { mockSharedPreferences.getString(targetingKey, null) } returns Gson().toJson(targeting)
        // A device clock that moved backwards leaves the entry stamped in the future, making its age unusable.
        cacheTimestamp(localStorage, System.currentTimeMillis() + HOUR_MS)

        assertNull(localStorage.getTargeting())

        verify { mockEditor.remove(targetingKey) }
        verify { mockEditor.remove(timestampKey) }
    }

    @Test
    fun `getTargeting should return null when the cached data has no timestamp`() {
        val targetingKey = getPrivateKey(localStorage, "targetingKey")
        every { mockSharedPreferences.getString(targetingKey, null) } returns Gson().toJson(targeting)
        cacheTimestamp(localStorage, 0L)

        assertNull(localStorage.getTargeting())

        verify { mockEditor.remove(targetingKey) }
    }

    @Test
    fun `getTargeting should apply the TTL configured in OptableConfig`() {
        val ttlSeconds = 2 * 60 * 60L
        val storage = LocalStorage(
            OptableConfig(mockContext, "testhost", "testtenant", "testslug", cacheTtl = ttlSeconds)
        )
        val targetingKey = getPrivateKey(storage, "targetingKey")
        every { mockSharedPreferences.getString(targetingKey, null) } returns Gson().toJson(targeting)

        cacheTimestamp(storage, System.currentTimeMillis() - HOUR_MS)
        assertEquals(targeting.gamTargetingKeywords, storage.getTargeting()?.gamTargetingKeywords)

        cacheTimestamp(storage, System.currentTimeMillis() - 3 * HOUR_MS)
        assertNull(storage.getTargeting())
    }

    @Test
    fun `getTargeting should keep the cache valid when the configured TTL is huge`() {
        val storage = LocalStorage(
            OptableConfig(mockContext, "testhost", "testtenant", "testslug", cacheTtl = Long.MAX_VALUE)
        )
        val targetingKey = getPrivateKey(storage, "targetingKey")
        every { mockSharedPreferences.getString(targetingKey, null) } returns Gson().toJson(targeting)
        cacheTimestamp(storage, System.currentTimeMillis() - HOUR_MS)

        assertEquals(targeting.gamTargetingKeywords, storage.getTargeting()?.gamTargetingKeywords)
    }

    private fun cacheTimestamp(storage: LocalStorage, timestamp: Long) {
        val timestampKey = getPrivateKey(storage, "targetingTimestampKey")
        every { mockSharedPreferences.getLong(timestampKey, 0L) } returns timestamp
    }

    @Test
    fun `getId5Signature should return null when not set`() {
        val id5SignatureKey = getPrivateKey(localStorage, "id5SignatureKey")
        every { mockSharedPreferences.getString(id5SignatureKey, null) } returns null
        assertNull(localStorage.getId5Signature())
    }

    @Test
    fun `setId5Signature and getId5Signature should store and retrieve the value`() {
        val signature = "test-id5-signature"
        val id5SignatureKey = getPrivateKey(localStorage, "id5SignatureKey")
        every { mockSharedPreferences.getString(id5SignatureKey, null) } returns signature

        localStorage.setId5Signature(signature)

        verify { mockEditor.putString(id5SignatureKey, signature) }
        assertEquals(signature, localStorage.getId5Signature())
    }

    @Test
    fun `clearTargeting should also remove the id5 signature`() {
        val id5SignatureKey = getPrivateKey(localStorage, "id5SignatureKey")
        localStorage.clearTargeting()
        verify { mockEditor.remove(id5SignatureKey) }
    }

    @Test
    fun `getTargeting should handle invalid JSON gracefully`() {
        val targetingKey = getPrivateKey(localStorage, "targetingKey")
        every { mockSharedPreferences.getString(targetingKey, null) } returns "{invalid-json}"
        cacheTimestamp(localStorage, System.currentTimeMillis() - HOUR_MS)

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
