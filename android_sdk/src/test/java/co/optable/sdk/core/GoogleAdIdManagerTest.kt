package co.optable.sdk.core

import android.content.Context
import android.util.Log
import co.optable.sdk.OptableConfig
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

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
            every { advertisingIdTimeoutMs } returns 1_000
        }
        manager = GoogleAdIdManager(config)

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0

        mockkStatic(AdvertisingIdClient::class)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    private fun adInfo(adId: String, limitAdTracking: Boolean = false) = mockk<AdvertisingIdClient.Info> {
        every { id } returns adId
        every { isLimitAdTrackingEnabled } returns limitAdTracking
    }

    @Test
    fun `getId returns the ad id`() = runBlocking {
        every { AdvertisingIdClient.getAdvertisingIdInfo(context) } returns adInfo("12345-abcde")

        assertEquals("12345-abcde", manager.getId())
    }

    @Test
    fun `getId reads the ad id on every call`() = runBlocking {
        every { AdvertisingIdClient.getAdvertisingIdInfo(context) } returnsMany listOf(
            adInfo("first-id"),
            adInfo("reset-id"),
            adInfo("reset-id", limitAdTracking = true),
        )

        assertEquals("first-id", manager.getId())
        assertEquals("reset-id", manager.getId())
        assertNull(manager.getId())
        verify(exactly = 3) { AdvertisingIdClient.getAdvertisingIdInfo(context) }
    }

    @Test
    fun `getId returns null when limit ad tracking is enabled`() = runBlocking {
        every { AdvertisingIdClient.getAdvertisingIdInfo(context) } returns adInfo("12345-abcde", limitAdTracking = true)

        assertNull(manager.getId())
    }

    @Test
    fun `getId does not read the ad id when skip detection is true`() = runBlocking {
        every { config.skipAdvertisingIdDetection } returns true

        assertNull(manager.getId())
        verify(exactly = 0) { AdvertisingIdClient.getAdvertisingIdInfo(any()) }
    }

    @Test
    fun `getId does not read the ad id when the timeout is zero`() = runBlocking {
        every { config.advertisingIdTimeoutMs } returns 0

        assertNull(manager.getId())
        verify(exactly = 0) { AdvertisingIdClient.getAdvertisingIdInfo(any()) }
    }

    @Test
    fun `getId returns null gracefully when exception occurs`() = runBlocking {
        every { AdvertisingIdClient.getAdvertisingIdInfo(context) } throws IllegalStateException("Google Play Services not available")

        assertNull(manager.getId())
        verify(exactly = 1) { Log.w("OptableGaidManager", any<String>()) }
    }

    @Test
    fun `getId stops waiting after the timeout`() = runBlocking {
        every { config.advertisingIdTimeoutMs } returns 50
        every { AdvertisingIdClient.getAdvertisingIdInfo(context) } answers {
            Thread.sleep(1_000)
            adInfo("slow-id")
        }

        val start = System.currentTimeMillis()
        val id = manager.getId()
        val elapsed = System.currentTimeMillis() - start

        assertNull(id)
        assertTrue("waited $elapsed ms", elapsed < 900)
    }
}
