package com.svantheemsche.appetijt.data.local.converter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DateConvertersTest {

    private val converters = DateConverters()

    @Test
    fun fromTimestamp_returnsCorrectDate() {
        val dateString = "2023-10-27"
        val expected = LocalDate.of(2023, 10, 27)
        assertEquals(expected, converters.fromTimestamp(dateString))
    }

    @Test
    fun fromTimestamp_returnsNull_forNullInput() {
        assertNull(converters.fromTimestamp(null))
    }

    @Test
    fun dateToTimestamp_returnsCorrectString() {
        val date = LocalDate.of(2023, 10, 27)
        val expected = "2023-10-27"
        assertEquals(expected, converters.dateToTimestamp(date))
    }

    @Test
    fun dateToTimestamp_returnsNull_forNullInput() {
        assertNull(converters.dateToTimestamp(null))
    }
}
