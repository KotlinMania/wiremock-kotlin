// port-lint: source mock.rs
package io.github.kotlinmania.wiremock

/**
 * Specifies how many times we expect a mock to match via its expectation setting.
 * It is used to set expectations on the usage of a mock in a test case.
 */
public class Times private constructor(
    private val inner: TimesEnum,
) {
    internal fun contains(nCalls: ULong): Boolean = inner.contains(nCalls)

    override fun toString(): String = inner.display()

    public companion object {
        public fun exactly(x: ULong): Times = Times(TimesEnum.Exact(x))

        public fun unbounded(): Times = Times(TimesEnum.Unbounded)

        public fun range(
            startInclusive: ULong,
            endExclusive: ULong,
        ): Times {
            require(startInclusive <= endExclusive) {
                "range start must be less than or equal to range end"
            }
            return Times(TimesEnum.Range(startInclusive, endExclusive))
        }

        public fun rangeFrom(startInclusive: ULong): Times = Times(TimesEnum.RangeFrom(startInclusive))

        public fun rangeTo(endExclusive: ULong): Times = Times(TimesEnum.RangeTo(endExclusive))

        public fun rangeToInclusive(endInclusive: ULong): Times = Times(TimesEnum.RangeToInclusive(endInclusive))

        public fun rangeInclusive(
            startInclusive: ULong,
            endInclusive: ULong,
        ): Times {
            require(startInclusive <= endInclusive) {
                "range start must be less than or equal to range end"
            }
            return Times(TimesEnum.RangeInclusive(startInclusive, endInclusive))
        }
    }
}

private sealed interface TimesEnum {
    fun contains(nCalls: ULong): Boolean

    fun display(): String

    data class Exact(
        val value: ULong,
    ) : TimesEnum {
        override fun contains(nCalls: ULong): Boolean = value == nCalls

        override fun display(): String = "== $value"
    }

    data object Unbounded : TimesEnum {
        override fun contains(nCalls: ULong): Boolean = true

        override fun display(): String = "0 <= x"
    }

    data class Range(
        val start: ULong,
        val endExclusive: ULong,
    ) : TimesEnum {
        override fun contains(nCalls: ULong): Boolean = nCalls >= start && nCalls < endExclusive

        override fun display(): String = "$start <= x < $endExclusive"
    }

    data class RangeFrom(
        val start: ULong,
    ) : TimesEnum {
        override fun contains(nCalls: ULong): Boolean = nCalls >= start

        override fun display(): String = "$start <= x"
    }

    data class RangeTo(
        val endExclusive: ULong,
    ) : TimesEnum {
        override fun contains(nCalls: ULong): Boolean = nCalls < endExclusive

        override fun display(): String = "0 <= x < $endExclusive"
    }

    data class RangeToInclusive(
        val endInclusive: ULong,
    ) : TimesEnum {
        override fun contains(nCalls: ULong): Boolean = nCalls <= endInclusive

        override fun display(): String = "0 <= x <= $endInclusive"
    }

    data class RangeInclusive(
        val start: ULong,
        val endInclusive: ULong,
    ) : TimesEnum {
        override fun contains(nCalls: ULong): Boolean = nCalls >= start && nCalls <= endInclusive

        override fun display(): String = "$start <= x <= $endInclusive"
    }
}

/**
 * Given a set of matchers, instructs a MockServer to return a response when conditions are satisfied.
 */
public class Mock(
    public val matchers: List<Match>,
    public val response: Respond,
    public var maxNMatches: ULong? = null,
    public var priority: UByte = 5u,
    public var name: String? = null,
    public var expectationRange: Times = Times.unbounded(),
) {
    public fun upToNTimes(n: ULong): Mock {
        require(n > 0uL) { "n must be strictly greater than 0" }
        maxNMatches = n
        return this
    }

    public fun priority(priority: UByte): Mock {
        this.priority = priority
        return this
    }

    public fun withPriority(priority: UByte): Mock {
        require(priority > 0u) { "priority must be strictly greater than 0!" }
        this.priority = priority
        return this
    }

    public fun withPriority(priority: Int): Mock = withPriority(priority.toUByte())

    public fun named(name: String): Mock {
        this.name = name
        return this
    }

    public fun expect(times: Times): Mock {
        this.expectationRange = times
        return this
    }

    public fun expect(nCalls: ULong): Mock {
        this.expectationRange = Times.exactly(nCalls)
        return this
    }

    public fun mount(server: MountedMockSet): MockId = server.register(this)

    public fun mountAsScoped(server: MountedMockSet): MockId = server.register(this)

    public fun matches(request: Request): Boolean = matchers.all { it.matches(request) }

    public companion object {
        public fun given(matcher: Match): MockBuilder = MockBuilder(mutableListOf(matcher))
    }
}

/**
 * A fluent builder to construct a Mock instance.
 */
public class MockBuilder internal constructor(
    private val matchersList: MutableList<Match>,
) {
    public constructor(matcher: Match) : this(mutableListOf(matcher))

    public val matchers: List<Match> get() = matchersList

    public fun and(matcher: Match): MockBuilder {
        matchersList.add(matcher)
        return this
    }

    public fun respondWith(response: Respond): Mock = Mock(matchersList.toList(), response)

    public fun respondWith(template: ResponseTemplate): Mock = Mock(matchersList.toList(), template)

    public fun respondWithErr(err: Throwable): Mock =
        Mock(
            matchersList.toList(),
            object : Respond {
                override fun respond(request: Request): ResponseTemplate {
                    throw err
                }
            },
        )
}

