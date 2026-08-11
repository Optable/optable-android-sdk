package co.optable.sdk.core

import android.content.Context
import co.optable.sdk.OptableConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppInfoHolderTest {

    @Test
    fun `bundle resolves to context package name`() {
        val context = RuntimeEnvironment.getApplication()
        val config = mockk<OptableConfig>()
        every { config.context } returns context

        val holder = AppInfoHolder(config)

        assertEquals(context.packageName, holder.bundle)
    }

    @Test
    fun `appVersion resolves from package manager`() {
        val context = RuntimeEnvironment.getApplication()
        shadowOf(context.packageManager)
            .getInternalMutablePackageInfo(context.packageName)
            .versionName = "9.8.7"
        val config = mockk<OptableConfig>()
        every { config.context } returns context

        val holder = AppInfoHolder(config)

        assertEquals("9.8.7", holder.appVersion)
    }

    @Test
    fun `appVersion is null when version name is blank`() {
        val context = RuntimeEnvironment.getApplication()
        shadowOf(context.packageManager)
            .getInternalMutablePackageInfo(context.packageName)
            .versionName = "   "
        val config = mockk<OptableConfig>()
        every { config.context } returns context

        val holder = AppInfoHolder(config)

        assertNull(holder.appVersion)
    }

    @Test
    fun `appVersion is null and bundle still resolves when package manager throws`() {
        val context = mockk<Context>()
        every { context.packageName } returns "co.optable.app"
        every { context.packageManager } throws RuntimeException("boom")
        val config = mockk<OptableConfig>()
        every { config.context } returns context

        val holder = AppInfoHolder(config)

        assertNull(holder.appVersion)
        assertEquals("co.optable.app", holder.bundle)
    }

    @Test
    fun `bundle is null when package name is blank`() {
        val context = mockk<Context>(relaxed = true)
        every { context.packageName } returns "   "
        val config = mockk<OptableConfig>()
        every { config.context } returns context

        val holder = AppInfoHolder(config)

        assertNull(holder.bundle)
    }
}
