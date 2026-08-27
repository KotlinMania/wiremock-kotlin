// port-lint: tests tests/tokio.rs
package io.github.kotlinmania.wiremock

import kotlin.test.Test
import kotlin.test.assertEquals

class TokioTest {
    @Test
    fun helloReqwest() {
        val set = MountedMockSet()
        val mock =
            Mock.given(Matchers.method("GET"))
                .and(Matchers.path("/"))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET")
        val (response, _) = set.handleRequest(req)
        assertEquals(200, response.statusCode)
    }

    @Test
    fun helloReqwestActix() {
        val set = MountedMockSet()
        val mock =
            Mock.given(Matchers.method("GET"))
                .and(Matchers.path("/"))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET")
        val (response, _) = set.handleRequest(req)
        assertEquals(200, response.statusCode)
    }

    @Test
    fun helloReqwestHttp2() {
        val set = MountedMockSet()
        val mock =
            Mock.given(Matchers.method("GET"))
                .and(Matchers.path("/"))
                .respondWith(ResponseTemplate.new(200))
        set.register(mock)

        val req = Request(url = "/", method = "GET", headers = mapOf("upgrade" to listOf("h2c")))
        val (response, _) = set.handleRequest(req)
        assertEquals(200, response.statusCode)
    }
}
