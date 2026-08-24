// port-lint: source lib.rs
package io.github.kotlinmania.wiremock

/**
 * Public exports and library utilities for WireMock.
 */
public object WireMock {
    public fun given(matcher: Match): MockBuilder = Mock.given(matcher)
}
