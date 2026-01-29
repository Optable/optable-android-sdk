package co.optable.android_sdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OptableResultTest {

    @Test
    fun `Success should hold the correct data`() {
        val successData = "Test data"
        val result = OptableResult.Success(successData)

        assertTrue(result is OptableResult.Success)
        assertEquals(successData, result.data)
    }

    @Test
    fun `Error should hold the correct message`() {
        val errorMessage = "An error occurred"
        val result = OptableResult.Error<Any>(errorMessage)

        assertTrue(result is OptableResult.Error)
        assertEquals(errorMessage, result.message)
    }
}