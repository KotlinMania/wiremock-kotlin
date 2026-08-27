// port-lint: tests wiremock/tests/timeout.rs
package io.github.kotlinmania.wiremock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

class TimeoutTest {
    private fun testBody() {
        val set = MountedMockSet()
        val response = ResponseTemplate.new(200).setDelay(60.seconds)
        val mock = Mock.given(Matchers.any()).respondWith(response)
        set.register(mock)

        val req = Request(url = "/", method = "GET")
        val (res, _) = set.handleRequest(req)
        assertEquals(200, res.statusCode)
        assertNotNull(res.delay)
        assertEquals(60.seconds, res.delay)
    }

    @Test
    fun requestTimesOutIfTheServerTakesTooLongWithActix() {
        testBody()
    }

    @Test
    fun requestTimesOutIfTheServerTakesTooLongWithTokio() {
        testBody()
    }
}
