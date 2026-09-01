// port-lint: source mock_server/builder.rs
package io.github.kotlinmania.wiremock.mockserver

import io.github.kotlinmania.wiremock.BodyPrintLimit
import io.github.kotlinmania.wiremock.Request

public class MockServerBuilder internal constructor() {
    private var serverAddress: String? = null
    private var recordIncomingRequests: Boolean = true
    private var bodyPrintLimit: BodyPrintLimit = BodyPrintLimit.Limited(Request.BODY_PRINT_LIMIT)

    public fun listener(serverAddress: String): MockServerBuilder {
        this.serverAddress = serverAddress
        return this
    }

    public fun disableRequestRecording(): MockServerBuilder {
        this.recordIncomingRequests = false
        return this
    }

    public fun bodyPrintLimit(limit: BodyPrintLimit): MockServerBuilder {
        this.bodyPrintLimit = limit
        return this
    }

    internal fun buildBare(): BareMockServer {
        val addr = serverAddress ?: "127.0.0.1:0"
        val recording = if (recordIncomingRequests) RequestRecording.Enabled else RequestRecording.Disabled
        return BareMockServer.start(addr, recording, bodyPrintLimit)
    }

    public fun start(): MockServer {
        val bare = buildBare()
        return MockServer(bare)
    }

    public companion object {
        public fun new(): MockServerBuilder = MockServerBuilder()
    }
}
