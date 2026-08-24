// port-lint: tests tests/mock_set.rs
package io.github.kotlinmania.wiremock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MockSetTest {
    @Test
    fun generationIsIncrementedForEveryReset() {
        val set = MountedMockSet()
        assertEquals(0u.toUShort(), set.generation)

        for (i in 1..9) {
            set.reset()
            assertEquals(i.toUShort(), set.generation)
        }
    }

    @Test
    fun accessingMockIdAfterResetThrows() {
        val set = MountedMockSet()
        val mock = Mock.given(Matchers.path("/")).respondWith(ResponseTemplate.new(200))
        val mockId = set.register(mock)

        set.reset()

        assertFailsWith<IllegalArgumentException> {
            set.getMock(mockId)
        }
    }

    @Test
    fun deactivatingMockDoesNotInvalidateOtherIds() {
        val set = MountedMockSet()
        val firstMock = Mock.given(Matchers.path("/")).respondWith(ResponseTemplate.new(200))
        val secondMock = Mock.given(Matchers.path("/hello")).respondWith(ResponseTemplate.new(500))
        val firstMockId = set.register(firstMock)
        val secondMockId = set.register(secondMock)

        set.deactivate(firstMockId)

        val first = set.getMock(firstMockId)
        assertEquals(MountedMockState.OutOfScope, first.second)
        val second = set.getMock(secondMockId)
        assertEquals(MountedMockState.InScope, second.second)
    }

    @Test
    fun handleRequestMatchesAndReturnsResponse() {
        val set = MountedMockSet()
        val mock = Mock.given(Matchers.path("/hello")).respondWith(ResponseTemplate.new(200).setBodyString("world"))
        set.register(mock)

        val req1 = Request(url = "/hello", method = "GET")
        val (response1, err1) = set.handleRequest(req1)
        assertEquals(200, response1.statusCode)
        assertEquals("world", response1.body?.decodeToString())
        assertEquals(null, err1)

        val req2 = Request(url = "/missing", method = "GET")
        val (response2, err2) = set.handleRequest(req2)
        assertEquals(404, response2.statusCode)
        assertEquals(null, err2)
    }
}
