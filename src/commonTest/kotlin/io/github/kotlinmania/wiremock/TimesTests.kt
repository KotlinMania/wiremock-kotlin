// port-lint: ignore
// Kotlin regression tests for the expectation range behavior translated from mock.rs.
package io.github.kotlinmania.wiremock

import kotlin.test.Test
import kotlin.test.assertEquals

class TimesTests {
    @Test
    fun exactMatchesOnlyOneCallCount() {
        val times = Times.exactly(10uL)

        assertEquals(false, times.contains(9uL))
        assertEquals(true, times.contains(10uL))
        assertEquals(false, times.contains(11uL))
        assertEquals("== 10", times.toString())
    }

    @Test
    fun halfOpenRangeMatchesStartButNotEnd() {
        val times = Times.range(10uL, 15uL)

        assertEquals(false, times.contains(9uL))
        assertEquals(true, times.contains(10uL))
        assertEquals(true, times.contains(14uL))
        assertEquals(false, times.contains(15uL))
        assertEquals("10 <= x < 15", times.toString())
    }

    @Test
    fun inclusiveRangeMatchesBothBounds() {
        val times = Times.rangeInclusive(10uL, 15uL)

        assertEquals(false, times.contains(9uL))
        assertEquals(true, times.contains(10uL))
        assertEquals(true, times.contains(15uL))
        assertEquals(false, times.contains(16uL))
        assertEquals("10 <= x <= 15", times.toString())
    }

    @Test
    fun oneSidedRangesKeepTheirBoundSemantics() {
        val from = Times.rangeFrom(10uL)
        val to = Times.rangeTo(15uL)
        val toInclusive = Times.rangeToInclusive(15uL)
        val unbounded = Times.unbounded()

        assertEquals(false, from.contains(9uL))
        assertEquals(true, from.contains(10uL))
        assertEquals("10 <= x", from.toString())

        assertEquals(true, to.contains(14uL))
        assertEquals(false, to.contains(15uL))
        assertEquals("0 <= x < 15", to.toString())

        assertEquals(true, toInclusive.contains(15uL))
        assertEquals(false, toInclusive.contains(16uL))
        assertEquals("0 <= x <= 15", toInclusive.toString())

        assertEquals(true, unbounded.contains(0uL))
        assertEquals(true, unbounded.contains(100uL))
        assertEquals("0 <= x", unbounded.toString())
    }
}
