package co.optable.sdk.core.network.edge

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EdgeServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var service: EdgeService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EdgeService::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `targeting sends all params, hids repeated and URL-component encoded`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        service.targeting(
            idList = listOf("e:abc", "g:123"),
            hidList = listOf("e:16bbea8f", "i6:2001:0db8::1"),
            bundle = "co.optable.app",
            version = "1.2.3",
            userAgent = "Mozilla/5.0 (Linux; Android)",
            id5Signature = "sig+123",
        )

        val recorded = server.takeRequest()
        val url = recorded.requestUrl!!

        // Decoded values
        assertEquals(listOf("e:abc", "g:123"), url.queryParameterValues("id"))
        assertEquals(listOf("e:16bbea8f", "i6:2001:0db8::1"), url.queryParameterValues("hid"))
        assertEquals("co.optable.app", url.queryParameter("bundle"))
        assertEquals("1.2.3", url.queryParameter("ver"))
        assertEquals("Mozilla/5.0 (Linux; Android)", url.queryParameter("ua"))
        assertEquals("sig+123", url.queryParameter("id5_signature"))

        // Raw wire encoding: every param must be URL-component encoded.
        val rawPath = recorded.path!!
        // Identifier prefixes' colons -> %3A
        assertTrue(rawPath, rawPath.contains("id=e%3Aabc"))
        assertTrue(rawPath, rawPath.contains("hid=e%3A16bbea8f"))
        assertTrue(rawPath, rawPath.contains("hid=i6%3A2001%3A0db8%3A%3A1"))
        // Scalar params: reserved '+' -> %2B (not left literal / form-encoded as space)
        assertTrue(rawPath, rawPath.contains("id5_signature=sig%2B123"))
        assertTrue(rawPath, rawPath.contains("bundle=co.optable.app"))
        assertTrue(rawPath, rawPath.contains("ver=1.2.3"))
        // User agent: space -> %20 (not '+'), '/' -> %2F, '(' ';' ')' -> %28 %3B %29
        assertTrue(rawPath, rawPath.contains("ua=Mozilla%2F5.0%20%28Linux%3B%20Android%29"))
    }

    @Test
    fun `targeting omits optional params when null or empty`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        service.targeting(
            idList = listOf("e:abc"),
            hidList = emptyList(),
            bundle = null,
            version = null,
            userAgent = null,
            id5Signature = null,
        )

        val recorded = server.takeRequest()
        val url = recorded.requestUrl!!

        assertEquals(listOf("e:abc"), url.queryParameterValues("id"))
        assertTrue(url.queryParameterValues("hid").isEmpty())
        assertNull(url.queryParameter("bundle"))
        assertNull(url.queryParameter("ver"))
        assertNull(url.queryParameter("ua"))
        assertNull(url.queryParameter("id5_signature"))
        assertFalse(recorded.path!!.contains("hid="))
    }
}
