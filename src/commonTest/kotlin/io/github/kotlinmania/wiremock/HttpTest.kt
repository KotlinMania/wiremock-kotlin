// port-lint: tests wiremock/src/http.rs
package io.github.kotlinmania.wiremock

import kotlin.test.Test
import kotlin.test.assertEquals

class HttpTest {
    @Test
    fun testHttpConstants() {
        assertEquals("GET", Http.GET)
        assertEquals("POST", Http.POST)
        assertEquals("PUT", Http.PUT)
        assertEquals("DELETE", Http.DELETE)
        assertEquals("PATCH", Http.PATCH)
        assertEquals("HEAD", Http.HEAD)
        assertEquals("OPTIONS", Http.OPTIONS)
    }
}
