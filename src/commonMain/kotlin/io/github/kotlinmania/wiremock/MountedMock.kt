// port-lint: source mounted_mock.rs
package io.github.kotlinmania.wiremock

/**
 * Given the behaviour specification as a Mock, keep track of runtime information
 * concerning this mock - e.g. how many times it matched on an incoming request.
 */
public class MountedMock(
    public val specification: Mock,
    public val positionInSet: Int,
) {
    public var nMatchedRequests: ULong = 0uL
        private set

    private val _matchedRequests: MutableList<Request> = mutableListOf()
    public val matchedRequests: List<Request> get() = _matchedRequests

    public fun matches(request: Request): Boolean {
        val maxN = specification.maxNMatches
        if (maxN != null && nMatchedRequests == maxN) {
            return false
        }
        val matched = specification.matches(request)
        if (matched) {
            nMatchedRequests += 1uL
            _matchedRequests.add(request)
        }
        return matched
    }

    public fun verify(): VerificationReport =
        VerificationReport(
            mockName = specification.name,
            expectationRange = specification.expectationRange,
            nMatchedRequests = nMatchedRequests,
            positionInSet = positionInSet,
        )

    public fun responseTemplate(request: Request): ResponseTemplate =
        specification.response.respond(request)
}
