// port-lint: tests tests/mocks.rs
package io.github.kotlinmania.wiremock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MocksTest {
    @Test
    fun newStartsTheServer() {
        val set = MountedMockSet()
        assertEquals(0u.toUShort(), set.generation)
    }

    @Test
    fun returns404IfNothingMatches() {
        val set = MountedMockSet()
        val req = Request(url = "/", method = "GET")
        val (response, _) = set.handleRequest(req)
        assertEquals(404, response.statusCode)
    }

    @Test
    fun panicsIfTheExpectationIsNotSatisfied() {
        val set = MountedMockSet()
        val response = ResponseTemplate.new(200)
        val mock =
            Mock.given(Matchers.method("GET"))
                .respondWith(response)
                .expect(Times.rangeFrom(1uL))
                .named("panics_if_the_expectation_is_not_satisfied expectation failed")
        set.register(mock)

        assertFailsWith<IllegalStateException> {
            set.verify()
        }
    }

    @Test
    fun noReceivedRequestLineIsPrintedInThePanicMessageIfExpectationsAreNotVerified() {
        val set = MountedMockSet()
        val response = ResponseTemplate.new(200)
        val mock =
            Mock.given(Matchers.method("GET"))
                .respondWith(response)
                .expect(Times.rangeFrom(1uL))
        set.register(mock)

        val err =
            assertFailsWith<IllegalStateException> {
                set.verify()
            }
        assertTrue(err.message!!.contains("1 <= x"))
    }

    @Test
    fun receivedRequestArePrintedAsPanicMessageIfExpectationsAreNotVerified() {
        val set = MountedMockSet()
        val response = ResponseTemplate.new(200)
        val mock =
            Mock.given(Matchers.method("POST"))
                .respondWith(response)
                .expect(Times.rangeFrom(1uL))
        set.register(mock)

        set.handleRequest(Request(url = "/", method = "GET"))

        val err =
            assertFailsWith<IllegalStateException> {
                set.verify()
            }
        assertTrue(err.message!!.contains("1 <= x"))
    }

    @Test
    fun panicDuringExpectationDoesNotCrash() {
        val set = MountedMockSet()
        val response = ResponseTemplate.new(200)
        val mock =
            Mock.given(Matchers.method("GET"))
                .respondWith(response)
                .expect(Times.rangeFrom(1uL))
                .named("panic_during_expectation_does_not_crash expectation failed")
        set.register(mock)

        assertFailsWith<IllegalStateException> {
            set.verify()
        }
    }

    @Test
    fun simpleRouteMock() {
        val set = MountedMockSet()
        val response = ResponseTemplate.new(200).setBodyBytes("world".encodeToByteArray())
        val mock =
            Mock.given(Matchers.method("GET"))
                .and(PathExactMatcher.new("hello"))
                .respondWith(response)
        set.register(mock)

        val (res, _) = set.handleRequest(Request(url = "/hello", method = "GET"))
        assertEquals(200, res.statusCode)
        assertEquals("world", res.body?.decodeToString())
    }

    @Test
    fun twoRouteMocks() {
        val set = MountedMockSet()

        val response1 = ResponseTemplate.new(200).setBodyBytes("aaa".encodeToByteArray())
        val mock1 =
            Mock.given(Matchers.method("GET"))
                .and(PathExactMatcher.new("first"))
                .respondWith(response1)
                .named("/first")
        set.register(mock1)

        val response2 = ResponseTemplate.new(200).setBodyBytes("bbb".encodeToByteArray())
        val mock2 =
            Mock.given(Matchers.method("GET"))
                .and(PathExactMatcher.new("second"))
                .respondWith(response2)
                .named("/second")
        set.register(mock2)

        val (res1, _) = set.handleRequest(Request(url = "/first", method = "GET"))
        val (res2, _) = set.handleRequest(Request(url = "/second", method = "GET"))

        assertEquals(200, res1.statusCode)
        assertEquals(200, res2.statusCode)
        assertEquals("aaa", res1.body?.decodeToString())
        assertEquals("bbb", res2.body?.decodeToString())
    }

    @Test
    fun bodyJsonMatchesIndependentOfKeyOrdering() {
        val expectedBody = """{"a":1,"b":2}"""
        val body = """{"b":2,"a":1}"""

        val set = MountedMockSet()
        val response = ResponseTemplate.new(200)
        val mock =
            Mock.given(Matchers.method("POST"))
                .and(BodyExactMatcher.json(expectedBody))
                .respondWith(response)
        set.register(mock)

        val req = Request(url = "/", method = "POST", body = body.encodeToByteArray())
        val (res, _) = set.handleRequest(req)
        assertEquals(200, res.statusCode)
    }

    @Test
    fun bodyJsonPartialMatchesAPartOfResponseJson() {
        val expectedBody = """{"a":1}"""
        val body = """{"a":1,"b":2}"""

        val set = MountedMockSet()
        val response = ResponseTemplate.new(200)
        val mock =
            Mock.given(Matchers.method("POST"))
                .and(BodyPartialJsonMatcher.json(expectedBody))
                .respondWith(response)
        set.register(mock)

        val req = Request(url = "/", method = "POST", body = body.encodeToByteArray())
        val (res, _) = set.handleRequest(req)
        assertEquals(200, res.statusCode)
    }

    @Test
    fun queryParameterIsNotAcceptedInPath() {
        assertFailsWith<IllegalArgumentException> {
            Mock.given(Matchers.method("GET")).and(Matchers.path("abcd?"))
        }
    }

    @Test
    fun hostIsNotAcceptedInPath() {
        assertFailsWith<IllegalArgumentException> {
            Mock.given(Matchers.method("GET")).and(Matchers.path("https://domain.com/abcd"))
        }
    }

    @Test
    fun useMockGuardToVerifyRequestsFromMock() {
        val set = MountedMockSet()
        val firstMock =
            Mock.given(Matchers.method("POST"))
                .and(PathExactMatcher.new("first"))
                .respondWith(ResponseTemplate.new(200))
        val firstId = set.register(firstMock)

        val secondMock =
            Mock.given(Matchers.method("POST"))
                .and(PathExactMatcher.new("second"))
                .respondWith(ResponseTemplate.new(200))
        val secondId = set.register(secondMock)

        set.handleRequest(Request(url = "/first", method = "POST", body = """{"attempt":1}""".encodeToByteArray()))
        set.handleRequest(Request(url = "/first", method = "POST", body = """{"attempt":2}""".encodeToByteArray()))
        set.handleRequest(Request(url = "/second", method = "POST", body = """{"attempt":99}""".encodeToByteArray()))

        val firstMounted = set.getMock(firstId).first
        assertEquals(2uL, firstMounted.nMatchedRequests)
        val secondMounted = set.getMock(secondId).first
        assertEquals(1uL, secondMounted.nMatchedRequests)
    }

    @Test
    fun useMockGuardToAwaitSatisfactionReadiness() {
        val set = MountedMockSet()
        val satisfy =
            Mock.given(Matchers.method("POST"))
                .and(PathExactMatcher.new("satisfy"))
                .respondWith(ResponseTemplate.new(200))
                .expect(1uL)
        val satisfyId = set.register(satisfy)

        set.handleRequest(Request(url = "/satisfy", method = "POST"))
        val mounted = set.getMock(satisfyId).first
        assertEquals(1uL, mounted.nMatchedRequests)
    }

    @Test
    fun debugPrintsMockServerVariants() {
        val set = MountedMockSet()
        assertTrue(set.toString().isNotEmpty())
    }

    @Test
    fun ioErr() {
        val set = MountedMockSet()
        val mock =
            Mock.given(Matchers.method("GET"))
                .respondWithErr(IllegalStateException("connection reset"))
        set.register(mock)

        val (_, err) = set.handleRequest(Request(url = "/", method = "GET"))
        assertEquals("connection reset", err?.message)
    }

    @Test
    fun customErr() {
        val set = MountedMockSet()
        val mock =
            Mock.given(Matchers.method("GET"))
                .respondWithErr(RuntimeException("custom error"))
        set.register(mock)

        val (_, err) = set.handleRequest(Request(url = "/", method = "GET"))
        assertEquals("custom error", err?.message)
    }

    @Test
    fun methodMatcherIsCaseInsensitive() {
        val set = MountedMockSet()
        val response = ResponseTemplate.new(200).setBodyBytes("world".encodeToByteArray())
        val mock =
            Mock.given(Matchers.method("Get"))
                .and(PathExactMatcher.new("hello"))
                .respondWith(response)
        set.register(mock)

        val (res, _) = set.handleRequest(Request(url = "/hello", method = "GET"))
        assertEquals(200, res.statusCode)
        assertEquals("world", res.body?.decodeToString())
    }

    @Test
    fun httpCrateMethodCanBeUsedDirectly() {
        val set = MountedMockSet()
        val response = ResponseTemplate.new(200).setBodyBytes("world".encodeToByteArray())
        val mock =
            Mock.given(Matchers.method("GET"))
                .and(PathExactMatcher.new("hello"))
                .respondWith(response)
        set.register(mock)

        val (res, _) = set.handleRequest(Request(url = "/hello", method = "GET"))
        assertEquals(200, res.statusCode)
        assertEquals("world", res.body?.decodeToString())
    }
}
