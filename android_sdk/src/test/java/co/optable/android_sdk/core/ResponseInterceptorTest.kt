package co.optable.android_sdk.core

import co.optable.android_sdk.core.network.ResponseInterceptor
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class ResponseInterceptorTest {

    @Mock
    private lateinit var storage: LocalStorage

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: OkHttpClient
    private lateinit var webServerUrl: HttpUrl
    private lateinit var mocks: AutoCloseable

    @Before
    fun setUp() {
        mocks = MockitoAnnotations.openMocks(this)
        mockWebServer = MockWebServer()
        mockWebServer.start()
        webServerUrl = mockWebServer.url("/")
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        mocks.close()
    }


    @Test
    fun `interceptor saves passport to storage`() {
        makeRequest("passport")

        verify(storage).setPassport("passport")
    }

    @Test
    fun `interceptor ignores empty header`() {
        makeRequest(null)

        verify(storage, never()).setPassport(any())
    }

    private fun makeRequest(passport: String?): Response {
        val response = MockResponse()
        response.setResponseCode(200)
        if (passport != null) {
            response.setHeader("X-Optable-Visitor", passport)
        }
        mockWebServer.enqueue(response)

        val responseInterceptor = ResponseInterceptor(storage)
        client = OkHttpClient.Builder()
            .addInterceptor(responseInterceptor)
            .build()

        val request = Request.Builder().url(webServerUrl).build()
        return client.newCall(request).execute()
    }

}