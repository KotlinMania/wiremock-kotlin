// port-lint: source wiremock/src/respond.rs
package io.github.kotlinmania.wiremock

/**
 * Anything that implements Respond can be used to reply to an incoming request.
 */
public fun interface Respond {
    public fun respond(request: Request): ResponseTemplate
}

/**
 * Like Respond, but it allows returning an error through a function.
 */
public fun interface RespondErr {
    public fun respondErr(request: Request): ErrorResponse
}
