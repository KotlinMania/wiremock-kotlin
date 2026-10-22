// port-lint: source mock_server/bare_server.rs
package io.github.kotlinmania.wiremock.mockserver

import io.github.kotlinmania.wiremock.BodyPrintLimit
import io.github.kotlinmania.wiremock.Mock
import io.github.kotlinmania.wiremock.MockId
import io.github.kotlinmania.wiremock.MountedMockSet
import io.github.kotlinmania.wiremock.Request
import io.github.kotlinmania.wiremock.ResponseTemplate
import io.github.kotlinmania.wiremock.VerificationOutcome
import io.github.kotlinmania.wiremock.VerificationReport
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public enum class RequestRecording {
    Enabled,
    Disabled,
}

internal class MockServerState(
    public val mockSet: MountedMockSet,
    private val _receivedRequests: MutableList<Request>?,
    public val bodyPrintLimit: BodyPrintLimit,
) {
    public val receivedRequests: List<Request>? get() = _receivedRequests?.toList()

    public fun handleRequest(request: Request): Pair<ResponseTemplate, kotlin.Throwable?> {
        _receivedRequests?.add(request)
        return mockSet.handleRequest(request)
    }

    public fun reset() {
        mockSet.reset()
        _receivedRequests?.clear()
    }
}

internal class BareMockServer internal constructor(
    public val serverAddress: String,
    private val state: MockServerState,
    private val mutex: Mutex = Mutex(),
) {
    public suspend fun register(mock: Mock): MockId {
        mutex.withLock {
            return state.mockSet.register(mock)
        }
    }

    public suspend fun registerAsScoped(mock: Mock): MockGuard {
        return mutex.withLock {
            val mockId = state.mockSet.register(mock)
            MockGuard(mockId, this, state.mockSet.getMock(mockId).first.satisfaction)
        }
    }

    public suspend fun reset() {
        mutex.withLock {
            state.reset()
        }
    }

    public suspend fun verify(): VerificationOutcome =
        mutex.withLock {
            state.mockSet.verifyAll()
        }

    public fun uri(): String = "http://$serverAddress"

    public fun address(): String = serverAddress

    public fun bodyPrintLimit(): BodyPrintLimit = state.bodyPrintLimit

    public suspend fun receivedRequests(): List<Request>? =
        mutex.withLock {
            state.receivedRequests
        }

    internal suspend fun handleRequest(request: Request): Pair<ResponseTemplate, kotlin.Throwable?> =
        mutex.withLock {
            state.handleRequest(request)
        }

    internal fun state(): MockServerState = state

    internal suspend fun receivedRequests(mockId: MockId): List<Request> =
        mutex.withLock {
            state.mockSet.getMock(mockId).first.receivedRequests()
        }

    internal suspend fun verify(mockId: MockId): VerificationReport =
        mutex.withLock {
            state.mockSet.verify(mockId)
        }

    internal suspend fun deactivate(mockId: MockId) {
        mutex.withLock {
            state.mockSet.deactivate(mockId)
        }
    }

    public companion object {
        public fun start(
            serverAddress: String = "127.0.0.1:0",
            requestRecording: RequestRecording = RequestRecording.Enabled,
            bodyPrintLimit: BodyPrintLimit = BodyPrintLimit.Unlimited,
        ): BareMockServer {
            val requests = if (requestRecording == RequestRecording.Enabled) mutableListOf<Request>() else null
            val state =
                MockServerState(
                    mockSet = MountedMockSet(bodyPrintLimit),
                    _receivedRequests = requests,
                    bodyPrintLimit = bodyPrintLimit,
                )
            return BareMockServer(serverAddress, state)
        }
    }
}

public class MockGuard internal constructor(
    public val mockId: MockId,
    private val server: BareMockServer,
    private val satisfaction: Deferred<Unit>,
) {
    public suspend fun receivedRequests(): List<Request> = server.receivedRequests(mockId)

    public suspend fun waitUntilSatisfied() {
        satisfaction.await()
    }

    public suspend fun verify(): VerificationReport = server.verify(mockId)

    public suspend fun deactivate() {
        server.deactivate(mockId)
    }
}
