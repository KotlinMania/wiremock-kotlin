// port-lint: tests tests/mocks.rs
package io.github.kotlinmania.wiremock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class ResponseTemplateTest {
    @Test
    fun responseTemplateBuilderMethods() {
        val template =
            ResponseTemplate
                .new(200)
                .insertHeader("X-Correlation-ID", "12345")
                .appendHeader("X-Custom", "value1")
                .appendHeader("X-Custom", "value2")
                .setBodyString("{\"ok\":true}")
                .setDelay(100.milliseconds)

        assertEquals(200, template.statusCode)
        assertEquals(listOf("12345"), template.headers["X-Correlation-ID"]?.toList())
        assertEquals(listOf("value1", "value2"), template.headers["X-Custom"]?.toList())
        assertEquals("{\"ok\":true}", template.body?.decodeToString())
        assertEquals(100.milliseconds, template.delay)

        val request = Request(url = "/test", method = "GET")
        val response = template.respond(request)
        assertEquals(200, response.statusCode)
    }
}
