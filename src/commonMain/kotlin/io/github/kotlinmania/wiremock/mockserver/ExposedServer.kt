// port-lint: source wiremock/src/mock_server/exposed_server.rs
package io.github.kotlinmania.wiremock.mockserver

import io.github.kotlinmania.wiremock.ErrorResponse
import io.github.kotlinmania.wiremock.Mock
import io.github.kotlinmania.wiremock.Request
import io.github.kotlinmania.wiremock.ResponseTemplate
import io.github.kotlinmania.wiremock.VerificationOutcome

public class MockServer internal constructor(
    private val bare: BareMockServer,
) {
    public suspend fun register(mock: Mock) {
        bare.register(mock)
    }

    public suspend fun registerAsScoped(mock: Mock): MockGuard {
        return bare.registerAsScoped(mock)
    }

    public suspend fun reset() {
        bare.reset()
    }

    public suspend fun verify() {
        val outcome = bare.verify()
        if (outcome is VerificationOutcome.Failure) {
            val details = outcome.failedVerifications.joinToString("\n") { "- " + it.errorMessage() }
            error("Verifications failed:\n$details")
        }
    }

    public fun uri(): String = bare.uri()

    public fun address(): String = bare.address()

    public suspend fun receivedRequests(): List<Request>? = bare.receivedRequests()

    internal suspend fun handleRequest(request: Request): Pair<ResponseTemplate, ErrorResponse?> =
        bare.handleRequest(request)

    public companion object {
        public fun builder(): MockServerBuilder = MockServerBuilder.new()

        public fun start(): MockServer {
            val pooled = MockServerPool.getPooledMockServer()
            return MockServer(pooled)
        }
    }
}
