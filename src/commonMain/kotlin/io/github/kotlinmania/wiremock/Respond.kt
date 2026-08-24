// port-lint: source respond.rs
package io.github.kotlinmania.wiremock

/**
 * Anything that implements Respond can be used to reply to an incoming request.
 */
public fun interface Respond {
    public fun respond(request: Request): ResponseTemplate
}
