package com.svantheemsche.appetijt.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkResultTest {

    @Test
    fun `Success data is correct`() {
        val data = "Success"
        val result = WorkResult.Success(data)
        assertEquals(data, result.data)
    }

    @Test
    fun `Failure properties are correct`() {
        val cause = Exception("Error")
        val result = WorkResult.Failure(ErrorCodes.UNKNOWN_ERROR, "Msg", cause)
        
        assertEquals(ErrorCodes.UNKNOWN_ERROR, result.code)
        assertEquals("Msg", result.message)
        assertEquals(cause, result.cause)
    }

    @Test
    fun `isSuccess returns true for Success`() {
        val result: WorkResult<String> = WorkResult.Success("OK")
        assertTrue(result is WorkResult.Success)
    }
}
