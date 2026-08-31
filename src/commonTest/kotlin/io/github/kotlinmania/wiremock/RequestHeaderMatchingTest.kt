// port-lint: tests wiremock/tests/request_header_matching.rs
package io.github.kotlinmania.wiremock

import kotlin.test.Test
import kotlin.test.assertEquals

class RequestHeaderMatchingTest {
    @Test
    fun shouldMatchSimpleRequestHeader() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.header("content-type", "application/json"))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("content-type" to listOf("application/json")))
        val (response, _) = set.handleRequest(req)
        assertEquals(200, response.statusCode)
    }

    @Test
    fun shouldNotMatchSimpleRequestHeaderUponWrongKey() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.header("content-type", "application/json"))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("accept" to listOf("application/json")))
        val (response, _) = set.handleRequest(req)
        assertEquals(404, response.statusCode)
    }

    @Test
    fun shouldNotMatchSimpleRequestHeaderUponWrongValue() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.header("content-type", "application/json"))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("content-type" to listOf("application/xml")))
        val (response, _) = set.handleRequest(req)
        assertEquals(404, response.statusCode)
    }

    @Test
    fun shouldMatchMultiRequestHeader() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.header("cache-control", "no-cache"))
                .and(Matchers.header("cache-control", "no-store"))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("cache-control" to listOf("no-cache", "no-store")))
        val (response, _) = set.handleRequest(req)
        assertEquals(200, response.statusCode)
    }

    @Test
    fun shouldMatchMultiRequestHeaderX() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.header("cache-control", "no-cache"))
                .and(Matchers.header("cache-control", "no-store"))
                .respondWith(ResponseTemplate.new(200))
                .expect(1uL)
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("cache-control" to listOf("no-cache", "no-store")))
        val (response, _) = set.handleRequest(req)
        assertEquals(200, response.statusCode)
    }

    @Test
    fun shouldNotMatchMultiRequestHeaderUponWrongValues() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.header("cache-control", "no-cache"))
                .and(Matchers.header("cache-control", "no-store"))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("cache-control" to listOf("no-cache", "no-transform")))
        val (response, _) = set.handleRequest(req)
        assertEquals(404, response.statusCode)
    }

    @Test
    fun shouldNotMatchMultiRequestHeaderUponIncompleteValues() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.header("cache-control", "no-cache"))
                .and(Matchers.header("cache-control", "no-store"))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("cache-control" to listOf("no-cache")))
        val (response, _) = set.handleRequest(req)
        assertEquals(404, response.statusCode)
    }

    @Test
    fun shouldMatchRegexSingleHeaderValue() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.headerRegex("cache-control", Regex("no-(cache|store)")))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("cache-control" to listOf("no-cache")))
        val (response, _) = set.handleRequest(req)
        assertEquals(200, response.statusCode)
    }

    @Test
    fun shouldMatchRegexMultipleHeaderValues() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.headerRegex("cache-control", Regex("no-(cache|store)")))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("cache-control" to listOf("no-cache", "no-store")))
        val (response, _) = set.handleRequest(req)
        assertEquals(200, response.statusCode)
    }

    @Test
    fun shouldNotMatchRegexWithWrongHeaderValue() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.headerRegex("cache-control", Regex("no-(cache|store)")))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("cache-control" to listOf("no-junk")))
        val (response, _) = set.handleRequest(req)
        assertEquals(404, response.statusCode)
    }

    @Test
    fun shouldNotMatchRegexWithAtLeastOneWrongHeaderValue() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.headerRegex("cache-control", Regex("no-(cache|store)")))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("cache-control" to listOf("no-cache", "no-junk")))
        val (response, _) = set.handleRequest(req)
        assertEquals(200, response.statusCode)
    }

    @Test
    fun shouldNotMatchRegexWithNoValuesForHeader() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.headerRegex("cache-control", Regex("no-(cache|store)")))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = emptyMap())
        val (response, _) = set.handleRequest(req)
        assertEquals(404, response.statusCode)
    }

    @Test
    fun shouldMatchBasicAuthHeader() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.basicAuth("Aladdin", "open sesame"))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("Authorization" to listOf("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==")))
        val (response, _) = set.handleRequest(req)
        assertEquals(200, response.statusCode)
    }

    @Test
    fun shouldNotMatchBadBasicAuthHeader() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.basicAuth("Aladdin", "close sesame"))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("Authorization" to listOf("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==")))
        val (response, _) = set.handleRequest(req)
        assertEquals(404, response.statusCode)
    }

    @Test
    fun shouldMatchBearerTokenHeader() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.bearerToken("delightful"))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("Authorization" to listOf("Bearer delightful")))
        val (response, _) = set.handleRequest(req)
        assertEquals(200, response.statusCode)
    }

    @Test
    fun shouldNotMatchBearerTokenHeader() {
        val set = MountedMockSet()
        val mock =
            Mock
                .given(Matchers.method("GET"))
                .and(Matchers.bearerToken("expired"))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("Authorization" to listOf("Bearer delightful")))
        val (response, _) = set.handleRequest(req)
        assertEquals(404, response.statusCode)
    }
}
