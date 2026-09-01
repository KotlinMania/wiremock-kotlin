// port-lint: source mock_server/hyper.rs
package io.github.kotlinmania.wiremock.mockserver

import io.github.kotlinmania.wiremock.ErrorResponse
import io.github.kotlinmania.wiremock.Request
import io.github.kotlinmania.wiremock.ResponseTemplate

internal class HyperServerHandler(
    private val serverState: MockServerState,
) {
    public fun handle(request: Request): Pair<ResponseTemplate, ErrorResponse?> = serverState.handleRequest(request)
}
