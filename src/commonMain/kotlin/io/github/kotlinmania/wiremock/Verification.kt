// port-lint: source wiremock/src/verification.rs
package io.github.kotlinmania.wiremock

/**
 * A report returned by a MountedMock detailing what the user expectations were and
 * how many calls were actually received since the mock was mounted on the server.
 */
public data class VerificationReport(
    public val mockName: String?,
    public val expectationRange: Times,
    public val nMatchedRequests: ULong,
    public val positionInSet: Int,
) {
    public fun errorMessage(): String =
        if (mockName != null) {
            "$mockName.\n\tExpected range of matching incoming requests: $expectationRange\n\tNumber of matched incoming requests: $nMatchedRequests"
        } else {
            "Mock #$positionInSet.\n\tExpected range of matching incoming requests: $expectationRange\n\tNumber of matched incoming requests: $nMatchedRequests"
        }

    public fun isSatisfied(): Boolean = expectationRange.contains(nMatchedRequests)
}

public sealed interface VerificationOutcome {
    public data object Success : VerificationOutcome

    public data class Failure(
        public val failedVerifications: List<VerificationReport>,
    ) : VerificationOutcome
}
