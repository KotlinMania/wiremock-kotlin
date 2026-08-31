// port-lint: source wiremock/src/mock_set.rs
package io.github.kotlinmania.wiremock

public enum class MountedMockState {
    InScope,
    OutOfScope,
}

public data class MockId(
    public val index: Int,
    public val generation: UShort,
)

public class MountedMockSet(
    public val bodyPrintLimit: BodyPrintLimit = BodyPrintLimit.Unlimited,
) {
    private val mocks: MutableList<Pair<MountedMock, MountedMockState>> = mutableListOf()
    public var generation: UShort = 0u
        private set

    public fun handleRequest(request: Request): Pair<ResponseTemplate, ErrorResponse?> {
        val sortedMocks = mocks.sortedBy { it.first.specification.priority }
        for ((mock, state) in sortedMocks) {
            if (state == MountedMockState.OutOfScope) continue
            if (mock.matches(request)) {
                return try {
                    Pair(mock.responseTemplate(request), null)
                } catch (e: Throwable) {
                    Pair(ResponseTemplate.new(500), e)
                }
            }
        }
        return Pair(ResponseTemplate.new(404), null)
    }

    public fun register(mock: Mock): MockId {
        val index = mocks.size
        val mounted = MountedMock(mock, index)
        mocks.add(Pair(mounted, MountedMockState.InScope))
        return MockId(index, generation)
    }

    public fun reset() {
        mocks.clear()
        generation = (generation + 1u).toUShort()
    }

    public fun deactivate(mockId: MockId) {
        checkMockId(mockId)
        val current = mocks[mockId.index]
        mocks[mockId.index] = Pair(current.first, MountedMockState.OutOfScope)
    }

    public fun getMock(mockId: MockId): Pair<MountedMock, MountedMockState> {
        checkMockId(mockId)
        return mocks[mockId.index]
    }

    public fun verifyAll(): VerificationOutcome {
        val failed =
            mocks
                .filter { it.second == MountedMockState.InScope }
                .map { it.first.verify() }
                .filter { !it.isSatisfied() }
        return if (failed.isEmpty()) {
            VerificationOutcome.Success
        } else {
            VerificationOutcome.Failure(failed)
        }
    }

    public fun verify() {
        val outcome = verifyAll()
        if (outcome is VerificationOutcome.Failure) {
            val details = outcome.failedVerifications.joinToString("\n") { "- " + it.errorMessage() }
            error("Verifications failed:\n$details")
        }
    }

    public fun verify(mockId: MockId): VerificationReport {
        checkMockId(mockId)
        return mocks[mockId.index].first.verify()
    }

    private fun checkMockId(mockId: MockId) {
        require(mockId.generation == generation) {
            "The mock you are trying to access is no longer active. It has been deleted from the active set via reset."
        }
        require(mockId.index in mocks.indices) {
            "Mock ID index out of bounds."
        }
    }

    public companion object {
        public fun new(bodyPrintLimit: BodyPrintLimit = BodyPrintLimit.Unlimited): MountedMockSet =
            MountedMockSet(bodyPrintLimit)
    }
}

