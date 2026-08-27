// port-lint: tests wiremock/tests/priority.rs
package io.github.kotlinmania.wiremock

import kotlin.test.Test
import kotlin.test.assertEquals

class PriorityTest {
    @Test
    fun shouldPrioritizeMockWithHighestPriority() {
        val set = MountedMockSet()
        val exact =
            Mock.given(Matchers.method("GET"))
                .and(Matchers.path("abcd"))
                .respondWith(ResponseTemplate.new(200))
                .withPriority(2)
        set.register(exact)

        val regex =
            Mock.given(Matchers.method("GET"))
                .and(Matchers.pathRegex("[a-z]{4}"))
                .respondWith(ResponseTemplate.new(201))
                .withPriority(1)
        set.register(regex)

        val req = Request(url = "/abcd", method = "GET")
        val (response, _) = set.handleRequest(req)
        assertEquals(201, response.statusCode)
    }

    @Test
    fun shouldNotPrioritizeMockWithLowerPriority() {
        val set = MountedMockSet()
        val exact =
            Mock.given(Matchers.method("GET"))
                .and(Matchers.path("abcd"))
                .respondWith(ResponseTemplate.new(200))
                .withPriority(255)
        set.register(exact)

        val regex =
            Mock.given(Matchers.method("GET"))
                .and(Matchers.pathRegex("[a-z]{4}"))
                .respondWith(ResponseTemplate.new(201))
        set.register(regex)

        val req = Request(url = "/abcd", method = "GET")
        val (response, _) = set.handleRequest(req)
        assertEquals(201, response.statusCode)
    }

    @Test
    fun byDefaultShouldUseInsertionOrder() {
        val set1 = MountedMockSet()
        val exact1 =
            Mock.given(Matchers.method("GET"))
                .and(Matchers.path("abcd"))
                .respondWith(ResponseTemplate.new(200))
        val regex1 =
            Mock.given(Matchers.method("GET"))
                .and(Matchers.pathRegex("[a-z]{4}"))
                .respondWith(ResponseTemplate.new(201))
        set1.register(exact1)
        set1.register(regex1)

        val req = Request(url = "/abcd", method = "GET")
        val (response1, _) = set1.handleRequest(req)
        assertEquals(200, response1.statusCode)

        val set2 = MountedMockSet()
        set2.register(regex1)
        set2.register(exact1)

        val (response2, _) = set2.handleRequest(req)
        assertEquals(201, response2.statusCode)
    }
}
