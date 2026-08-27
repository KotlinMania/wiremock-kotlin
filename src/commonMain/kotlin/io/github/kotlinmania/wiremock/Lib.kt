// port-lint: source wiremock/src/lib.rs
package io.github.kotlinmania.wiremock

public typealias ErrorResponse = Throwable

/**
 * Public exports and library utilities for WireMock.
 */
public object WireMock {
    public fun given(matcher: Match): MockBuilder = Mock.given(matcher)
}
