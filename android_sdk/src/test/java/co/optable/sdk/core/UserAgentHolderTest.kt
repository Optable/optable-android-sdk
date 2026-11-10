package co.optable.sdk.core

import android.content.Context
import android.webkit.WebSettings
import co.optable.sdk.OptableConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UserAgentHolderTest {

    private lateinit var context: Context
    private lateinit var config: OptableConfig

    @Before
    fun setup() {
        context = mockk()
        config = OptableConfig(mockk { every { applicationContext } returns context }, "tenant", "slug")
        mockkStatic(WebSettings::class)
        every { WebSettings.getDefaultUserAgent(context) } returns "DefaultAgent/1.0"
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getUserAgent should return the default user agent when no custom one is set`() {
        assertEquals("DefaultAgent/1.0", UserAgentHolder(config).getUserAgent())
    }

    @Test
    fun `getUserAgent should apply a custom user agent changed after construction`() {
        val holder = UserAgentHolder(config)

        config.customUserAgent = "CustomAgent/1.0"
        assertEquals("CustomAgent/1.0", holder.getUserAgent())

        config.customUserAgent = null
        assertEquals("DefaultAgent/1.0", holder.getUserAgent())
    }

    @Test
    fun `getUserAgent should not read the default user agent when a custom one is set`() {
        config.customUserAgent = "CustomAgent/1.0"

        assertEquals("CustomAgent/1.0", UserAgentHolder(config).getUserAgent())
        verify(exactly = 0) { WebSettings.getDefaultUserAgent(any()) }
    }
}
