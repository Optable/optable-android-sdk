package co.optable.sdk

import android.content.Context
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class OptableConfigTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockApplicationContext: Context

    @Before
    fun setUp() {
        `when`(mockContext.applicationContext).thenReturn(mockApplicationContext)
    }

    @Test
    fun `constructor should set properties correctly with default values`() {
        val config = OptableConfig(
            providedContext = mockContext,
            tenant = "test-tenant",
            originSlug = "test-slug"
        )

        assertEquals("test-tenant", config.tenant)
        assertEquals("test-slug", config.originSlug)
        assertEquals("na.edge.optable.co", config.host)
        assertEquals("v2", config.path)
        assertFalse(config.insecure)
        assertNull(config.apiKey)
        assertNull(config.customUserAgent)
        assertFalse(config.skipAdvertisingIdDetection)
        assertEquals(mockApplicationContext, config.context)
    }

    @Test
    fun `constructor should set all provided properties correctly`() {
        val customConsents = OptableConsents(gpp = "custom")
        val config = OptableConfig(
            providedContext = mockContext,
            tenant = "custom-tenant",
            originSlug = "custom-slug",
            host = "custom.host.com",
            path = "v3",
            insecure = true,
            apiKey = "test-api-key",
            customUserAgent = "TestAgent/1.0",
            skipAdvertisingIdDetection = true,
            consents = customConsents
        )

        assertEquals("custom-tenant", config.tenant)
        assertEquals("custom-slug", config.originSlug)
        assertEquals("custom.host.com", config.host)
        assertEquals("v3", config.path)
        assertTrue(config.insecure)
        assertEquals("test-api-key", config.apiKey)
        assertEquals("TestAgent/1.0", config.customUserAgent)
        assertTrue(config.skipAdvertisingIdDetection)
        assertEquals(customConsents, config.consents)
    }

    @Test
    fun `getBaseUrl should return HTTPS url when insecure is false`() {
        val config = OptableConfig(
            providedContext = mockContext,
            tenant = "test-tenant",
            originSlug = "test-slug",
            host = "my.host.com",
            path = "api",
            insecure = false
        )

        val expectedUrl = "https://my.host.com/api/"
        assertEquals(expectedUrl, config.getBaseUrl())
    }

    @Test
    fun `getBaseUrl should return HTTP url when insecure is true`() {
        val config = OptableConfig(
            providedContext = mockContext,
            tenant = "test-tenant",
            originSlug = "test-slug",
            host = "my.host.com",
            path = "api",
            insecure = true
        )

        val expectedUrl = "http://my.host.com/api/"
        assertEquals(expectedUrl, config.getBaseUrl())
    }

    @Test
    fun `consents property should be updatable`() {
        val config = OptableConfig(
            providedContext = mockContext,
            tenant = "test-tenant",
            originSlug = "test-slug"
        )
        val initialConsents = config.consents
        val newConsents = OptableConsents(gdprSubject = true)

        config.consents = newConsents

        assertEquals(newConsents, config.consents)
        assertNotEquals(initialConsents, config.consents)
    }
}
