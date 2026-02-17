package co.optable.android_sdk.core.network

import co.optable.android_sdk.OptableConfig
import co.optable.android_sdk.core.network.data.TraitsRequest
import co.optable.android_sdk.core.network.edge.EdgeService
import com.google.gson.JsonObject
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response
import java.lang.reflect.Field

@RunWith(MockitoJUnitRunner::class)
class NetworkClientTest {

    @Mock
    private lateinit var mockEdgeService: EdgeService

    private lateinit var networkClient: NetworkClient

    @Before
    fun setUp() {
        val config = mockk<OptableConfig>()
        every { config.getBaseUrl() } returns "https://test.com"
        val requestInterceptor = mockk<RequestInterceptor>()
        val responseInterceptor = mockk<ResponseInterceptor>()

        networkClient = NetworkClient(config, requestInterceptor, responseInterceptor)

        try {
            val field: Field = NetworkClient::class.java.getDeclaredField("edgeService")
            field.isAccessible = true
            field.set(networkClient, mockEdgeService)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @Test
    fun identify_shouldReturnSuccess_whenApiCallIsSuccessful() = runBlocking {
        val expectedResponse: Response<Unit> = Response.success(Unit)

        `when`(mockEdgeService.identify(emptyList())).thenReturn(expectedResponse)

        val result = networkClient.identify(emptyList())

        assertTrue(result is NetworkResponse.Success)
        assertEquals(Unit, (result as NetworkResponse.Success).result)
    }

    @Test
    fun identify_shouldReturnError_whenApiCallIsUnsuccessful() = runBlocking {
        val errorBody = ResponseBody.create(null, "Error")
        val expectedResponse: Response<Unit> = Response.error(404, errorBody)

        `when`(mockEdgeService.identify(emptyList())).thenReturn(expectedResponse)

        val result = networkClient.identify(emptyList())

        assertTrue(result is NetworkResponse.Error)
    }

    @Test
    fun identify_shouldReturnError_whenApiCallThrowsException() = runBlocking {
        val exception = RuntimeException("Network error")

        `when`(mockEdgeService.identify(emptyList())).thenThrow(exception)

        val result = networkClient.identify(emptyList())

        assertTrue(result is NetworkResponse.Error)
        assertEquals(exception.message, (result as NetworkResponse.Error).message)
    }

    @Test
    fun profile_shouldReturnSuccess_whenApiCallIsSuccessful() = runBlocking {
        val traits = hashMapOf<String, Any>("key" to "value")
        val traitsRequest = TraitsRequest.from(traits, null, setOf())
        val jsonObject = JsonObject().apply { addProperty("status", "success") }
        val expectedResponse: Response<JsonObject> = Response.success(jsonObject)

        `when`(mockEdgeService.profile(traitsRequest)).thenReturn(expectedResponse)

        val result = networkClient.profile(traits, null, setOf())

        assertTrue(result is NetworkResponse.Success)
        assertEquals(jsonObject, (result as NetworkResponse.Success).result)
    }

    @Test
    fun profile_shouldReturnError_whenApiCallIsUnsuccessful() = runBlocking {
        val traits = hashMapOf<String, Any>("key" to "value")
        val traitsRequest = TraitsRequest.from(traits, null, setOf())
        val errorBody = ResponseBody.create(null, "Error")
        val expectedResponse: Response<JsonObject> = Response.error(400, errorBody)

        `when`(mockEdgeService.profile(traitsRequest)).thenReturn(expectedResponse)

        val result = networkClient.profile(traits, null, setOf())

        assertTrue(result is NetworkResponse.Error)
    }

    @Test
    fun profile_shouldReturnError_whenApiCallThrowsException() = runBlocking {
        val traits = hashMapOf<String, Any>("key" to "value")
        val traitsRequest = TraitsRequest.from(traits, null, setOf())
        val exception = RuntimeException("Network error")

        `when`(mockEdgeService.profile(traitsRequest)).thenThrow(exception)

        val result = networkClient.profile(traits, null, setOf())

        assertTrue(result is NetworkResponse.Error)
        assertEquals(exception.message, (result as NetworkResponse.Error).message)
    }

    @Test
    fun targeting_shouldReturnSuccessWithData_whenApiCallIsSuccessful() = runBlocking {
        val jsonObject = JsonObject().apply { addProperty("targetingKey", "targetingValue") }
        val expectedResponse: Response<JsonObject> = Response.success(jsonObject)

        `when`(mockEdgeService.targeting(emptyList())).thenReturn(expectedResponse)

        val result = networkClient.targeting(emptyList())

        assertTrue(result is NetworkResponse.Success)
        assertEquals(jsonObject, (result as NetworkResponse.Success).result)
    }

    @Test
    fun targeting_shouldReturnError_whenApiCallIsUnsuccessful() = runBlocking {
        val errorBody = ResponseBody.create(null, "Error")
        val expectedResponse: Response<JsonObject> = Response.error(500, errorBody)

        `when`(mockEdgeService.targeting(emptyList())).thenReturn(expectedResponse)

        val result = networkClient.targeting(emptyList())

        assertTrue(result is NetworkResponse.Error)
    }

    @Test
    fun targeting_shouldReturnError_whenApiCallThrowsException() = runBlocking {
        val exception = RuntimeException("Network error")

        `when`(mockEdgeService.targeting(emptyList())).thenThrow(exception)

        val result = networkClient.targeting(emptyList())

        assertTrue(result is NetworkResponse.Error)
        assertEquals(exception.message, (result as NetworkResponse.Error).message)
    }

    @Test
    fun witness_shouldReturnSuccess_whenApiCallIsSuccessful() = runBlocking {
        val event = "test_event"
        val properties = hashMapOf<String, Any>("prop" to "value")
        val witnessBody = hashMapOf<String, Any>("event" to event, "properties" to properties)
        val expectedResponse: Response<Unit> = Response.success(Unit)

        `when`(mockEdgeService.witness(witnessBody)).thenReturn(expectedResponse)

        val result = networkClient.witness(event, properties)

        assertTrue(result is NetworkResponse.Success)
        assertEquals(Unit, (result as NetworkResponse.Success).result)
    }

    @Test
    fun witness_shouldReturnError_whenApiCallIsUnsuccessful() = runBlocking {
        val event = "test_event"
        val properties = hashMapOf<String, Any>("prop" to "value")
        val witnessBody = hashMapOf<String, Any>("event" to event, "properties" to properties)
        val errorBody = ResponseBody.create(null, "Error")
        val expectedResponse: Response<Unit> = Response.error(403, errorBody)

        `when`(mockEdgeService.witness(witnessBody)).thenReturn(expectedResponse)

        val result = networkClient.witness(event, properties)

        assertTrue(result is NetworkResponse.Error)
    }

    @Test
    fun witness_shouldReturnError_whenApiCallThrowsException() = runBlocking {
        val event = "test_event"
        val properties = hashMapOf<String, Any>("prop" to "value")
        val witnessBody = hashMapOf<String, Any>("event" to event, "properties" to properties)
        val exception = RuntimeException("Network error")

        `when`(mockEdgeService.witness(witnessBody)).thenThrow(exception)

        val result = networkClient.witness(event, properties)

        assertTrue(result is NetworkResponse.Error)
        assertEquals(exception.message, (result as NetworkResponse.Error).message)
    }
}
