// port-lint: tests wiremock/tests/mocks.rs
package io.github.kotlinmania.wiremock

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RequestTest {
    @Test
    fun requestPropertiesAndBodyString() {
        val bodyText = "hello world"
        val request =
            Request(
                url = "http://localhost:8080/path?query=1",
                method = "POST",
                headers = mapOf("Content-Type" to listOf("text/plain")),
                body = bodyText.encodeToByteArray(),
            )

        assertEquals("http://localhost:8080/path?query=1", request.url)
        assertEquals("POST", request.method)
        assertEquals(bodyText, request.bodyString())
        assertEquals(1, request.headers.size)
    }

    @Test
    fun bodyPrintLimitVariants() {
        val limited: BodyPrintLimit = BodyPrintLimit.Limited(1024)
        val unlimited: BodyPrintLimit = BodyPrintLimit.Unlimited

        when (limited) {
            is BodyPrintLimit.Limited -> assertEquals(1024, limited.limit)
            BodyPrintLimit.Unlimited -> error("expected limited")
        }

        when (unlimited) {
            is BodyPrintLimit.Limited -> error("expected unlimited")
            BodyPrintLimit.Unlimited -> assertTrue(true)
        }
    }
}
