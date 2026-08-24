// port-lint: tests tests/request_header_matching.rs
package io.github.kotlinmania.wiremock

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MatchersTest {
    @Test
    fun methodMatcherMatchesCorrectly() {
        val matcher = Matchers.method("GET")
        val req1 = Request(url = "/hello", method = "GET")
        val req2 = Request(url = "/hello", method = "POST")

        assertTrue(matcher.matches(req1))
        assertFalse(matcher.matches(req2))
    }

    @Test
    fun pathMatcherMatchesCorrectly() {
        val matcher = Matchers.path("/hello")
        val req1 = Request(url = "http://localhost:8080/hello", method = "GET")
        val req2 = Request(url = "/hello?query=1", method = "GET")
        val req3 = Request(url = "/other", method = "GET")

        assertTrue(matcher.matches(req1))
        assertTrue(matcher.matches(req2))
        assertFalse(matcher.matches(req3))
    }

    @Test
    fun pathRegexMatcherMatchesCorrectly() {
        val matcher = Matchers.pathRegex("^/items/[0-9]+$")
        val req1 = Request(url = "http://localhost:8080/items/123", method = "GET")
        val req2 = Request(url = "/items/abc", method = "GET")

        assertTrue(matcher.matches(req1))
        assertFalse(matcher.matches(req2))
    }

    @Test
    fun headerMatcherMatchesCorrectly() {
        val matcher = Matchers.header("Authorization", "Bearer secret")
        val req1 =
            Request(
                url = "/test",
                method = "GET",
                headers = mapOf("authorization" to listOf("Bearer secret")),
            )
        val req2 =
            Request(
                url = "/test",
                method = "GET",
                headers = mapOf("authorization" to listOf("Basic admin")),
            )

        assertTrue(matcher.matches(req1))
        assertFalse(matcher.matches(req2))
    }

    @Test
    fun bodyStringMatcherMatchesCorrectly() {
        val matcher = Matchers.bodyString("hello")
        val req1 = Request(url = "/test", method = "POST", body = "hello".encodeToByteArray())
        val req2 = Request(url = "/test", method = "POST", body = "world".encodeToByteArray())

        assertTrue(matcher.matches(req1))
        assertFalse(matcher.matches(req2))
    }
}
