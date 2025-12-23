package co.optable.android_sdk.core

import co.optable.BuildConfig
import co.optable.android_sdk.OptableConfig
import co.optable.android_sdk.core.network.RequestInterceptor
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MockWebServerTest {

    @Mock
    private lateinit var config: OptableConfig

    @Mock
    private lateinit var storage: LocalStorage

    @Mock
    private lateinit var userAgentHolder: UserAgentHolder

    @Mock
    private lateinit var consentsManager: ConsentsManager

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: OkHttpClient
    private lateinit var webServerUrl: HttpUrl
    private lateinit var mocks: AutoCloseable

    @Before
    fun setUp() {
        mocks = MockitoAnnotations.openMocks(this)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        mockWebServer.enqueue(MockResponse())
        webServerUrl = mockWebServer.url("/")

        whenever(consentsManager.subjectToGdpr()).thenReturn(null)
        whenever(consentsManager.gdprConsent()).thenReturn(null)
        whenever(consentsManager.gppConsent()).thenReturn(null)
        whenever(consentsManager.gppSid()).thenReturn(null)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        mocks.close()
    }

    @Test
    fun `only required fields`() {
        whenever(config.tenant).thenReturn("tenant")
        whenever(config.originSlug).thenReturn("originSlug")

        val request = makeRequest().request

        assertNull(request.headers["Authorization"])
        assertNull(request.headers["X-Optable-Visitor"])
        assertNull(request.headers["User-Agent"])

        assertEquals("application/json", request.headers["Accept"])
        assertEquals(
            "android-" + BuildConfig.VERSION_NAME + "-" + BuildConfig.VERSION_CODE,
            request.url.queryParameter("osdk")
        )
        assertEquals("tenant", request.url.queryParameter("t"))
        assertEquals("originSlug", request.url.queryParameter("o"))
    }

    @Test
    fun `optional header, api key`() {
        whenever(config.apiKey).thenReturn("apiKey")

        val request = makeRequest().request

        assertEquals("Bearer apiKey", request.headers["Authorization"])
    }

    @Test
    fun `optional header, passport`() {
        whenever(storage.getPassport()).thenReturn("passport")

        val request = makeRequest().request

        assertEquals("passport", request.headers["X-Optable-Visitor"])
    }

    @Test
    fun `complete request`() {
        whenever(config.tenant).thenReturn("tenant")
        whenever(config.originSlug).thenReturn("originSlug")
        whenever(config.apiKey).thenReturn("apiKey")
        whenever(storage.getPassport()).thenReturn("passport")
        whenever(userAgentHolder.getUserAgent()).thenReturn("userAgent")

        val request = makeRequest().request

        assertEquals("application/json", request.headers["Accept"])
        assertEquals("Bearer apiKey", request.headers["Authorization"])
        verify(config, times(1)).apiKey
        assertEquals("passport", request.headers["X-Optable-Visitor"])
        verify(storage, times(1)).getPassport()
        assertEquals("userAgent", request.headers["User-Agent"])
        verify(userAgentHolder, times(1)).getUserAgent()

        assertEquals(
            "android-" + BuildConfig.VERSION_NAME + "-" + BuildConfig.VERSION_CODE,
            request.url.queryParameter("osdk")
        )
        assertEquals("tenant", request.url.queryParameter("t"))
        verify(config, times(1)).tenant
        assertEquals("originSlug", request.url.queryParameter("o"))
        verify(config, times(1)).originSlug
    }

    @Test
    fun `full url test`() {
        whenever(config.tenant).thenReturn("tenant")
        whenever(config.originSlug).thenReturn("originSlug")

        val request = makeRequest().request
        val expectedQueryParams = "?osdk=android-${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}&t=tenant&o=originSlug"
        assertEquals(webServerUrl.toString() + expectedQueryParams, request.url.toString())
    }

    @Test
    fun `subject to GDPR, true`() {
        whenever(consentsManager.subjectToGdpr()).thenReturn(true)

        val request = makeRequest().request
        assertEquals("1", request.url.queryParameter("gdpr"))
    }

    @Test
    fun `subject to GDPR, false`() {
        whenever(consentsManager.subjectToGdpr()).thenReturn(false)

        val request = makeRequest().request
        assertEquals("0", request.url.queryParameter("gdpr"))
    }

    @Test
    fun `GDPR consent string`() {
        whenever(consentsManager.gdprConsent()).thenReturn("gdpr_consent_string")

        val request = makeRequest().request
        assertEquals("gdpr_consent_string", request.url.queryParameter("gdpr_consent"))
    }

    @Test
    fun `GPP consent string`() {
        whenever(consentsManager.gppConsent()).thenReturn("gpp_consent_string")

        val request = makeRequest().request
        assertEquals("gpp_consent_string", request.url.queryParameter("gpp"))
    }

    @Test
    fun `GPP sid`() {
        whenever(consentsManager.gppSid()).thenReturn("gpp_sid")

        val request = makeRequest().request
        assertEquals("gpp_sid", request.url.queryParameter("gpp_sid"))
    }

    @Test
    fun `all consents`() {
        whenever(consentsManager.subjectToGdpr()).thenReturn(true)
        whenever(consentsManager.gdprConsent()).thenReturn("gdpr_consent_string")
        whenever(consentsManager.gppConsent()).thenReturn("gpp_consent_string")
        whenever(consentsManager.gppSid()).thenReturn("gpp_sid")

        val request = makeRequest().request
        assertEquals("1", request.url.queryParameter("gdpr"))
        assertEquals("gdpr_consent_string", request.url.queryParameter("gdpr_consent"))
        assertEquals("gpp_consent_string", request.url.queryParameter("gpp"))
        assertEquals("gpp_sid", request.url.queryParameter("gpp_sid"))
    }

    private fun makeRequest(): Response {
        val interceptor = RequestInterceptor(config, storage, userAgentHolder, consentsManager)
        client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val request = Request.Builder().url(webServerUrl).build()
        return client.newCall(request).execute()
    }

}
