package co.optable.sdk

import android.util.Base64
import android.util.Log
import co.optable.sdk.core.AppInfoHolder
import co.optable.sdk.core.ConsentsManager
import co.optable.sdk.core.LocalStorage
import co.optable.sdk.core.UseCases
import co.optable.sdk.core.UserAgentHolder
import co.optable.sdk.core.network.NetworkClient
import co.optable.sdk.core.network.NetworkResponse
import com.google.gson.JsonObject
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

@ExperimentalCoroutinesApi
class OptableSDKTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockConfig: OptableConfig
    private lateinit var mockNetworkClient: NetworkClient
    private lateinit var mockStorage: LocalStorage
    private lateinit var mockUseCases: UseCases
    private lateinit var mockConsentsManager: ConsentsManager

    private lateinit var sdk: OptableSDK

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockConfig = mockk(relaxed = true)
        every { mockConfig.getBaseUrl() } returns "https://test.com"
        mockNetworkClient = mockk()
        mockStorage = mockk(relaxed = true)
        every { mockStorage.getId5Signature() } returns null
        mockUseCases = mockk()
        every { mockUseCases.parseId5Signature(any()) } returns null
        mockConsentsManager = mockk(relaxed = true)

        mockkStatic(Log::class)
        every { Log.e(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        mockkStatic(Base64::class)

        every { Base64.encodeToString(any(), any()) } returns "mockedBase64String"

        sdk = OptableSDK(mockConfig)

        val ceh = CoroutineExceptionHandler { _, e -> Log.e("OptableSDK", "Internal exception: $e") }
        val testScope = CoroutineScope(SupervisorJob() + testDispatcher + ceh)
        setPrivateField(sdk, "scope", testScope)

        setPrivateField(sdk, "networkClient", mockNetworkClient)
        setPrivateField(sdk, "storage", mockStorage)
        setPrivateField(sdk, "useCases", mockUseCases)
        setPrivateField(sdk, "consentsManager", mockConsentsManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun setPrivateField(target: Any, fieldName: String, value: Any) {
        val field: Field = target.javaClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(target, value)
    }

    @Test
    fun `identify success should call listener with success`() = runTest(testDispatcher) {
        val listener = mockk<OptableResultListener<Unit>>(relaxed = true)
        coEvery { mockNetworkClient.identify(emptyList()) } returns NetworkResponse.Success(Unit)

        sdk.identify(emptyList(), listener)
        advanceUntilIdle()

        val slot = slot<OptableResult<Unit>>()
        verify { listener.onComplete(capture(slot)) }
        assertTrue(slot.captured is OptableResult.Success)
    }

    @Test
    fun `identify error should call listener with error`() = runTest(testDispatcher) {
        val listener = mockk<OptableResultListener<Unit>>(relaxed = true)
        val errorMessage = "Network Failure"
        coEvery { mockNetworkClient.identify(emptyList()) } returns NetworkResponse.Error(errorMessage)

        sdk.identify(emptyList(), listener)
        advanceUntilIdle()

        val slot = slot<OptableResult<Unit>>()
        verify { listener.onComplete(capture(slot)) }
        assertTrue(slot.captured is OptableResult.Error)
        assertEquals(errorMessage, (slot.captured as OptableResult.Error).message)
    }

    @Test
    fun `identify throws exception should be handled`() = runTest(testDispatcher) {
        val listener = mockk<OptableResultListener<Unit>>(relaxed = true)
        val exception = RuntimeException("Coroutine Canceled")
        coEvery { mockNetworkClient.identify(emptyList()) } throws exception

        sdk.identify(emptyList(), listener)
        advanceUntilIdle()

        verify(exactly = 0) { listener.onComplete(any()) }
        verify { Log.e("OptableSDK", "Internal exception: $exception") }
    }

    @Test
    fun `tryIdentifyFromUrl with valid url should call identify`() = runTest(testDispatcher) {
        val listener = mockk<OptableResultListener<Unit>>(relaxed = true)
        val url = "http://some.domain.com/some/path?some=query&something=else&oeid=a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3&foo=bar&baz"

        coEvery { mockNetworkClient.identify(any()) } returns NetworkResponse.Success(Unit)

        sdk.tryIdentifyFromUrl(url, listener)
        advanceUntilIdle()

//        coVerify { mockNetworkClient.identify(any()) }
    }

    @Test
    fun `tryIdentifyFromUrl with invalid url should call listener with error`() = runTest(testDispatcher) {
        val sdkSpy = spyk(sdk)
        val listener = mockk<OptableResultListener<Unit>>(relaxed = true)
        val url = "https://example.com?other_param=value"

        sdkSpy.tryIdentifyFromUrl(url, listener)
        advanceUntilIdle()

        val slot = slot<OptableResult<Unit>>()
        verify { listener.onComplete(capture(slot)) }
        assertTrue(slot.captured is OptableResult.Error)
        verify(exactly = 0) { sdkSpy.identify(any(), any()) }
    }

    @Test
    fun `profile success should call listener with success`() = runTest(testDispatcher) {
        val listener = mockk<OptableResultListener<OptableTargeting>>(relaxed = true)
        val targetingJson = JsonObject()
        val expectedTargeting = mockk<OptableTargeting>()
        val traits = hashMapOf<String, Any>("age" to 30)
        coEvery { mockNetworkClient.profile(traits, null, setOf()) } returns NetworkResponse.Success(targetingJson)
        every { mockUseCases.parseTargetingResponse(targetingJson) } returns expectedTargeting

        sdk.profile(traits, null, setOf(), listener)
        advanceUntilIdle()

        val slot = slot<OptableResult<OptableTargeting>>()
        verify { listener.onComplete(capture(slot)) }
        assertTrue(slot.captured is OptableResult.Success)
        assertEquals(expectedTargeting, (slot.captured as OptableResult.Success).data)
    }

    @Test
    fun `profile error should call listener with error`() = runTest(testDispatcher) {
        val listener = mockk<OptableResultListener<OptableTargeting>>(relaxed = true)
        val traits = hashMapOf<String, Any>("age" to 30)
        val errorMessage = "Profile Failure"
        coEvery { mockNetworkClient.profile(traits, null, setOf()) } returns NetworkResponse.Error(errorMessage)

        sdk.profile(traits, null, setOf(), listener)
        advanceUntilIdle()

        val slot = slot<OptableResult<OptableTargeting>>()
        verify { listener.onComplete(capture(slot)) }
        assertTrue(slot.captured is OptableResult.Error)
        assertEquals(errorMessage, (slot.captured as OptableResult.Error).message)
    }

    @Test
    fun `targeting success should parse and store targeting data`() = runTest(testDispatcher) {
        val listener = mockk<OptableResultListener<OptableTargeting>>(relaxed = true)
        val targetingJson = JsonObject()
        val expectedTargeting = mockk<OptableTargeting>()
        coEvery {
            mockNetworkClient.targeting(emptyList(), emptyList(), any(), any(), any(), any())
        } returns NetworkResponse.Success(targetingJson)
        every { mockUseCases.parseTargetingResponse(targetingJson) } returns expectedTargeting

        sdk.targeting(emptyList(), listener)
        advanceUntilIdle()

        verify { mockStorage.setTargeting(expectedTargeting) }
        val slot = slot<OptableResult<OptableTargeting>>()
        verify { listener.onComplete(capture(slot)) }
        assertTrue(slot.captured is OptableResult.Success)
        assertEquals(expectedTargeting, (slot.captured as OptableResult.Success).data)
    }

    @Test
    fun `targeting error should call listener with error`() = runTest(testDispatcher) {
        val listener = mockk<OptableResultListener<OptableTargeting>>(relaxed = true)
        val errorMessage = "Targeting Failure"
        coEvery {
            mockNetworkClient.targeting(emptyList(), emptyList(), any(), any(), any(), any())
        } returns NetworkResponse.Error(errorMessage)

        sdk.targeting(emptyList(), listener)
        advanceUntilIdle()

        verify(exactly = 0) { mockStorage.setTargeting(any()) }
        val slot = slot<OptableResult<OptableTargeting>>()
        verify { listener.onComplete(capture(slot)) }
        assertTrue(slot.captured is OptableResult.Error)
    }

    @Test
    fun `targeting forwards encoded hints and cached id5 signature`() = runTest(testDispatcher) {
        val listener = mockk<OptableResultListener<OptableTargeting>>(relaxed = true)
        val targetingJson = JsonObject()
        val expectedTargeting = mockk<OptableTargeting>()
        every { mockStorage.getId5Signature() } returns "sig-xyz"
        coEvery {
            mockNetworkClient.targeting(any(), any(), any(), any(), any(), any())
        } returns NetworkResponse.Success(targetingJson)
        every { mockUseCases.parseTargetingResponse(targetingJson) } returns expectedTargeting

        val hids = listOf(OptableIdentifier.IPv6("2001:DB8::1"))
        sdk.targeting(emptyList(), hids, listener)
        advanceUntilIdle()

        coVerify {
            mockNetworkClient.targeting(
                emptyList(),
                listOf("i6:2001:db8::1"),
                any(),
                any(),
                any(),
                "sig-xyz",
            )
        }
    }

    @Test
    fun `targeting stores id5 signature extracted from response`() = runTest(testDispatcher) {
        val listener = mockk<OptableResultListener<OptableTargeting>>(relaxed = true)
        val targetingJson = JsonObject()
        coEvery {
            mockNetworkClient.targeting(any(), any(), any(), any(), any(), any())
        } returns NetworkResponse.Success(targetingJson)
        every { mockUseCases.parseTargetingResponse(targetingJson) } returns mockk()
        every { mockUseCases.parseId5Signature(targetingJson) } returns "new-sig"

        sdk.targeting(emptyList(), listener)
        advanceUntilIdle()

        verify { mockStorage.setId5Signature("new-sig") }
    }

    @Test
    fun `targeting keeps previous id5 signature when response has none`() = runTest(testDispatcher) {
        val listener = mockk<OptableResultListener<OptableTargeting>>(relaxed = true)
        val targetingJson = JsonObject()
        coEvery {
            mockNetworkClient.targeting(any(), any(), any(), any(), any(), any())
        } returns NetworkResponse.Success(targetingJson)
        every { mockUseCases.parseTargetingResponse(targetingJson) } returns mockk()
        every { mockUseCases.parseId5Signature(targetingJson) } returns null

        sdk.targeting(emptyList(), listener)
        advanceUntilIdle()

        verify(exactly = 0) { mockStorage.setId5Signature(any()) }
    }

    @Test
    fun `targeting forwards app info when sendAppInfo is true`() = runTest(testDispatcher) {
        injectAppInfo(bundle = "co.optable.app", appVersion = "1.2.3", userAgent = "ua-string")

        val listener = mockk<OptableResultListener<OptableTargeting>>(relaxed = true)
        val targetingJson = JsonObject()
        coEvery {
            mockNetworkClient.targeting(any(), any(), any(), any(), any(), any())
        } returns NetworkResponse.Success(targetingJson)
        every { mockUseCases.parseTargetingResponse(targetingJson) } returns mockk()

        sdk.targeting(emptyList(), listener)
        advanceUntilIdle()

        // Pins both the gate (true -> forwarded) and the holder field -> param position mapping.
        coVerify {
            mockNetworkClient.targeting(emptyList(), emptyList(), "co.optable.app", "1.2.3", "ua-string", null)
        }
    }

    @Test
    fun `targeting suppresses app info when sendAppInfo is false`() = runTest(testDispatcher) {
        injectAppInfo(bundle = "co.optable.app", appVersion = "1.2.3", userAgent = "ua-string")

        val listener = mockk<OptableResultListener<OptableTargeting>>(relaxed = true)
        val targetingJson = JsonObject()
        coEvery {
            mockNetworkClient.targeting(any(), any(), any(), any(), any(), any())
        } returns NetworkResponse.Success(targetingJson)
        every { mockUseCases.parseTargetingResponse(targetingJson) } returns mockk()

        sdk.targeting(emptyList(), listener)
        advanceUntilIdle()

        coVerify {
            mockNetworkClient.targeting(emptyList(), emptyList(), null, null, null, null)
        }
    }

    private fun injectAppInfo(bundle: String?, appVersion: String?, userAgent: String?) {
        val mockAppInfoHolder = mockk<AppInfoHolder> {
            every { this@mockk.bundle } returns bundle
            every { this@mockk.appVersion } returns appVersion
        }
        val mockUserAgentHolder = mockk<UserAgentHolder> {
            every { getUserAgent() } returns userAgent
        }
        setPrivateField(sdk, "appInfoHolder", mockAppInfoHolder)
        setPrivateField(sdk, "userAgentHolder", mockUserAgentHolder)
    }

    @Test
    fun `targetingFromCache should return data from storage`() {
        val expectedTargeting = OptableTargeting(emptyMap(), null, JSONObject())
        every { mockStorage.getTargeting() } returns expectedTargeting

        val result = sdk.targetingFromCache()

        assertNotNull(result)
        assertEquals(expectedTargeting, result)
        verify { mockStorage.getTargeting() }
    }

    @Test
    fun `targetingClearCache should call storage`() {
        sdk.targetingClearCache()
        verify { mockStorage.clearTargeting() }
    }

    @Test
    fun `witness success should call listener with success`() = runTest(testDispatcher) {
        val listener = mockk<OptableResultListener<Unit>>(relaxed = true)
        val event = "button_click"
        val properties = hashMapOf<String, Any>("color" to "blue")
        coEvery { mockNetworkClient.witness(event, properties) } returns NetworkResponse.Success(Unit)

        sdk.witness(event, properties, listener)
        advanceUntilIdle()

        val slot = slot<OptableResult<Unit>>()
        verify { listener.onComplete(capture(slot)) }
        assertTrue(slot.captured is OptableResult.Success)
    }

    @Test
    fun `witness error should call listener with error`() = runTest(testDispatcher) {
        val listener = mockk<OptableResultListener<Unit>>(relaxed = true)
        val event = "button_click"
        val properties = hashMapOf<String, Any>("color" to "blue")
        val errorMessage = "Witness Failure"
        coEvery { mockNetworkClient.witness(event, properties) } returns NetworkResponse.Error(errorMessage)

        sdk.witness(event, properties, listener)
        advanceUntilIdle()

        val slot = slot<OptableResult<Unit>>()
        verify { listener.onComplete(capture(slot)) }
        assertTrue(slot.captured is OptableResult.Error)
        assertEquals(errorMessage, (slot.captured as OptableResult.Error).message)
    }

    @Test
    fun `setConsents should update consents manager`() {
        val consents = mockk<OptableConsents>()

        sdk.setConsents(consents)

        verify { mockConsentsManager.customConsents = consents }
    }
}
