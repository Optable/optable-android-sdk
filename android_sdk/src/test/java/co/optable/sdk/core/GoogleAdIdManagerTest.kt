package co.optable.sdk.core

import android.content.Context
import android.util.Log
import co.optable.sdk.OptableConfig
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GoogleAdIdManagerTest {

    private lateinit var config: OptableConfig
    private lateinit var context: Context
    private lateinit var manager: GoogleAdIdManager

    @Before
    fun setup() {
        context = mockk()
        config = mockk {
            every { context } returns this@GoogleAdIdManagerTest.context
            every { skipAdvertisingIdDetection } returns false
        }
        manager = GoogleAdIdManager(config)

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        mockkStatic(AdvertisingIdClient::class)

        resetCompanionState()
    }

    @After
    fun teardown() {
        unmockkAll()
        resetCompanionState()
    }

    @Test
    fun `getId returns null initially when nothing is fetched`() {
        assertNull(manager.getId())
    }

    @Test
    fun `fetchAdvertisingId completes deferred with null when skip detection is true`() = runTest {
        every { config.skipAdvertisingIdDetection } returns true

        manager.fetchAdvertisingId()

        assertNull(manager.deferredTask.await())
        assertNull(manager.getId())

        verify(exactly = 0) { AdvertisingIdClient.getAdvertisingIdInfo(any()) }
    }

    @Test
    fun `fetchAdvertisingId successfully fetches ad id and updates state`() = runTest {
        val expectedAdId = "12345-abcde"
        val mockInfo = mockk<AdvertisingIdClient.Info> {
            every { id } returns expectedAdId
            every { isLimitAdTrackingEnabled } returns false
        }
        every { AdvertisingIdClient.getAdvertisingIdInfo(context) } returns mockInfo

        manager.fetchAdvertisingId()

        assertEquals(expectedAdId, manager.deferredTask.await())
        assertEquals(expectedAdId, manager.getId())
    }

    @Test
    fun `fetchAdvertisingId sets id but getId returns null when limit ad tracking is enabled`() = runTest {
        val expectedAdId = "12345-abcde"
        val mockInfo = mockk<AdvertisingIdClient.Info> {
            every { id } returns expectedAdId
            every { isLimitAdTrackingEnabled } returns true
        }
        every { AdvertisingIdClient.getAdvertisingIdInfo(context) } returns mockInfo

        manager.fetchAdvertisingId()

        assertEquals(expectedAdId, manager.deferredTask.await())

        assertNull(manager.getId())
    }

    @Test
    fun `fetchAdvertisingId completes with null gracefully when exception occurs`() = runTest {
        every { AdvertisingIdClient.getAdvertisingIdInfo(context) } throws IllegalStateException("Google Play Services not available")

        manager.fetchAdvertisingId()

        assertNull(manager.deferredTask.await())
        assertNull(manager.getId())

        verify(exactly = 1) { Log.w("OptableGaidManager", any<String>()) }
    }


    @Test
    fun `deferredTask await blocks until fetchAdvertisingId completes`() = runTest {
        val expectedAdId = "async-test-id"
        val mockInfo = mockk<AdvertisingIdClient.Info> {
            every { id } returns expectedAdId
            every { isLimitAdTrackingEnabled } returns false
        }
        every { AdvertisingIdClient.getAdvertisingIdInfo(context) } returns mockInfo

        val testManager = GoogleAdIdManager(config)

        var awaitCompleted = false
        val awaitJob = launch {
            testManager.deferredTask.await()
            awaitCompleted = true
        }

        advanceTimeBy(100)
        assertEquals("await() should be suspended until fetchAdvertisingId completes", false, awaitCompleted)

        testManager.fetchAdvertisingId()

        awaitJob.join()

        assertEquals("await() should complete after fetchAdvertisingId", true, awaitCompleted)
        assertEquals(expectedAdId, testManager.getId())
    }

    @Test
    fun `multiple calls to deferredTask await return the same result`() = runTest {
        val expectedAdId = "shared-id-12345"
        val mockInfo = mockk<AdvertisingIdClient.Info> {
            every { id } returns expectedAdId
            every { isLimitAdTrackingEnabled } returns false
        }
        every { AdvertisingIdClient.getAdvertisingIdInfo(context) } returns mockInfo

        val testManager = GoogleAdIdManager(config)

        val results = mutableListOf<String?>()
        val jobs = List(5) {
            launch {
                val result = testManager.deferredTask.await()
                results.add(result)
            }
        }

        advanceTimeBy(50)
        assertEquals("No results should be available yet", 0, results.size)

        testManager.fetchAdvertisingId()

        jobs.forEach { it.join() }

        assertEquals(5, results.size)
        results.forEach { result ->
            assertEquals(expectedAdId, result)
        }

        verify(exactly = 1) { AdvertisingIdClient.getAdvertisingIdInfo(context) }
    }


    private fun resetCompanionState() {
        try {
            val adIdField = GoogleAdIdManager::class.java.getDeclaredField("adId")
            adIdField.isAccessible = true
            adIdField.set(null, null)

            val limitField = GoogleAdIdManager::class.java.getDeclaredField("limitAdTracking")
            limitField.isAccessible = true
            limitField.set(null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
