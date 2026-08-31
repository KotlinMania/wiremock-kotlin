// port-lint: tests wiremock/tests/request_header_matching.rs
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

    @Test
    fun anyMatcherMatchesAll() {
        val matcher = Matchers.any()
        assertTrue(matcher.matches(Request(url = "/any", method = "GET")))
        assertTrue(matcher.matches(Request(url = "/other", method = "POST")))
    }

    @Test
    fun headerExistsAndRegexMatchers() {
        val existsMatcher = Matchers.headerExists("X-Custom")
        val regexMatcher = Matchers.headerRegex("X-Custom", Regex("^[0-9]+$"))
        val req1 = Request(url = "/test", method = "GET", headers = mapOf("x-custom" to listOf("12345")))
        val req2 = Request(url = "/test", method = "GET", headers = mapOf("x-custom" to listOf("abc")))
        val req3 = Request(url = "/test", method = "GET")

        assertTrue(existsMatcher.matches(req1))
        assertTrue(existsMatcher.matches(req2))
        assertFalse(existsMatcher.matches(req3))

        assertTrue(regexMatcher.matches(req1))
        assertFalse(regexMatcher.matches(req2))
        assertFalse(regexMatcher.matches(req3))
    }

    @Test
    fun bodyStringContainsMatcher() {
        val matcher = Matchers.bodyStringContains("key")
        val req1 = Request(url = "/test", method = "POST", body = "the secret key here".encodeToByteArray())
        val req2 = Request(url = "/test", method = "POST", body = "nothing here".encodeToByteArray())

        assertTrue(matcher.matches(req1))
        assertFalse(matcher.matches(req2))
    }

    @Test
    fun queryParamMatchers() {
        val exact = Matchers.queryParam("page", "2")
        val contains = Matchers.queryParamContains("filter", "admin")
        val missing = Matchers.queryParamIsMissing("debug")

        val req1 = Request(url = "/items?page=2&filter=super_admin", method = "GET")
        val req2 = Request(url = "/items?page=1&debug=true", method = "GET")

        assertTrue(exact.matches(req1))
        assertFalse(exact.matches(req2))

        assertTrue(contains.matches(req1))
        assertFalse(contains.matches(req2))

        assertTrue(missing.matches(req1))
        assertFalse(missing.matches(req2))
    }

    @Test
    fun authMatchers() {
        val basic = Matchers.basicAuth("user", "pass")
        val bearer = Matchers.bearerToken("token123")

        val reqBasic = Request(url = "/test", method = "GET", headers = mapOf("Authorization" to listOf("Basic dXNlcjpwYXNz")))
        val reqBearer = Request(url = "/test", method = "GET", headers = mapOf("Authorization" to listOf("Bearer token123")))
        val reqOther = Request(url = "/test", method = "GET")

        assertTrue(basic.matches(reqBasic))
        assertFalse(basic.matches(reqBearer))
        assertFalse(basic.matches(reqOther))

        assertTrue(bearer.matches(reqBearer))
        assertFalse(bearer.matches(reqBasic))
        assertFalse(bearer.matches(reqOther))
    }
}
