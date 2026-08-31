// port-lint: tests tests/mocks.rs
package io.github.kotlinmania.wiremock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MockTest {
    @Test
    fun mockBuilderAndMatching() {
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.path("/api/v1/users"))
                .respondWith(ResponseTemplate.new(200).setBodyString("[]"))
                .named("get-users-mock")
                .priority(1u)
                .upToNTimes(3uL)
                .expect(Times.exactly(3uL))

        assertEquals("get-users-mock", mock.name)
        assertEquals(1u.toUByte(), mock.priority)
        assertEquals(3uL, mock.maxNMatches)

        val matchingReq = Request(url = "http://localhost/api/v1/users", method = "GET")
        val nonMatchingMethod = Request(url = "http://localhost/api/v1/users", method = "POST")
        val nonMatchingPath = Request(url = "http://localhost/api/v1/posts", method = "GET")

        assertTrue(mock.matches(matchingReq))
        assertFalse(mock.matches(nonMatchingMethod))
        assertFalse(mock.matches(nonMatchingPath))
    }

    @Test
    fun wireMockGivenHelper() {
        val mock =
            WireMock
                .given(Matchers.method("POST"))
                .respondWith(ResponseTemplate.new(201))

        assertTrue(mock.matches(Request(url = "/", method = "POST")))
    }
}
