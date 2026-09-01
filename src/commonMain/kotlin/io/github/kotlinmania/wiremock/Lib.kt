// port-lint: source lib.rs
package io.github.kotlinmania.wiremock

public typealias ErrorResponse = Throwable
public typealias MockGuard = io.github.kotlinmania.wiremock.mockserver.MockGuard
public typealias MockServer = io.github.kotlinmania.wiremock.mockserver.MockServer
public typealias MockServerBuilder = io.github.kotlinmania.wiremock.mockserver.MockServerBuilder

/**
 * Public exports and library utilities for WireMock.
 */
public object WireMock {
    public fun given(matcher: Match): MockBuilder = Mock.given(matcher)
}
