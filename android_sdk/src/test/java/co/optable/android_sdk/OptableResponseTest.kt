package co.optable.android_sdk

import org.junit.Assert.*
import org.junit.Test

class OptableResponseTest {

    @Test
    fun `success should create response with SUCCESS status and provided data`() {
        val testData = "Test"
        val response = OptableResponse.success(testData)

        assertEquals(OptableResponse.Status.SUCCESS, response.status)
        assertEquals(testData, response.data)
        assertNull(response.message)
    }

    @Test
    fun `success should handle null data correctly`() {
        val response = OptableResponse.success<String?>(null)

        assertEquals(OptableResponse.Status.SUCCESS, response.status)
        assertNull(response.data)
        assertNull(response.message)
    }

    @Test
    fun `error should create response with ERROR status and formatted message`() {
        val errorMessage = "Wrong"
        val errorTrace = "stacktrace..."
        val error = OptableResponse.Error(errorMessage, errorTrace)
        val response: OptableResponse<Any> = OptableResponse.error(error)

        assertEquals(OptableResponse.Status.ERROR, response.status)
        assertNull(response.data)
        assertEquals("$errorMessage (trace: $errorTrace)", response.message)
    }

    @Test
    fun `Error data class should store error and trace`() {
        val errorMessage = "Error"
        val errorTrace = "Trace"
        val error = OptableResponse.Error(errorMessage, errorTrace)

        assertEquals(errorMessage, error.error)
        assertEquals(errorTrace, error.trace)
    }

    @Test
    fun `OptableResponse instances should be comparable`() {
        val response1 = OptableResponse.success("data")
        val response2 = OptableResponse.success("data")
        val response3 = OptableResponse.success("different data")
        val errorResponse = OptableResponse.error<String>(OptableResponse.Error("e", "t"))

        assertEquals(response1, response2)
        assertEquals(response1.hashCode(), response2.hashCode())
        assertNotEquals(response1, response3)
        assertNotEquals(response1.hashCode(), response3.hashCode())
        assertNotEquals(response1, errorResponse)
    }

    @Test
    fun `copy method should work as expected`() {
        val originalResponse = OptableResponse.success("Initial data")
        val copiedResponse = originalResponse.copy(status = OptableResponse.Status.ERROR, message = "An error occurred")

        assertEquals(OptableResponse.Status.ERROR, copiedResponse.status)
        assertEquals("Initial data", copiedResponse.data)
        assertEquals("An error occurred", copiedResponse.message)
    }

    @Test
    fun `toString should not be empty`() {
        val response = OptableResponse.success("data")
        assert(response.toString().isNotEmpty())

        val error = OptableResponse.Error("e", "t")
        assert(error.toString().isNotEmpty())
    }
}