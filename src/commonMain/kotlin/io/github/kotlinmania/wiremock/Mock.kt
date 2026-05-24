// port-lint: source mock.rs
package io.github.kotlinmania.wiremock

/**
 * Specifies how many times we expect a mock to match via its expectation setting.
 * It is used to set expectations on the usage of a mock in a test case.
 *
 * You can either specify an exact value:
 *
 * ```
 * val times = Times.exactly(10uL)
 * ```
 *
 * Or a range:
 *
 * ```
 * val betweenTenAndFifteen = Times.range(10uL, 15uL)
 * val betweenTenAndFifteenInclusive = Times.rangeInclusive(10uL, 15uL)
 * val atLeastTen = Times.rangeFrom(10uL)
 * val lessThanFifteen = Times.rangeTo(15uL)
 * val lessThanSixteen = Times.rangeToInclusive(15uL)
 * ```
 */
class Times private constructor(
    private val inner: TimesEnum,
) {
    internal fun contains(nCalls: ULong): Boolean {
        return inner.contains(nCalls)
    }

    override fun toString(): String {
        return inner.display()
    }

    companion object {
        fun exactly(x: ULong): Times {
            return Times(TimesEnum.Exact(x))
        }

        fun unbounded(): Times {
            return Times(TimesEnum.Unbounded)
        }

        fun range(startInclusive: ULong, endExclusive: ULong): Times {
            require(startInclusive <= endExclusive) {
                "range start must be less than or equal to range end"
            }
            return Times(TimesEnum.Range(startInclusive, endExclusive))
        }

        fun rangeFrom(startInclusive: ULong): Times {
            return Times(TimesEnum.RangeFrom(startInclusive))
        }

        fun rangeTo(endExclusive: ULong): Times {
            return Times(TimesEnum.RangeTo(endExclusive))
        }

        fun rangeToInclusive(endInclusive: ULong): Times {
            return Times(TimesEnum.RangeToInclusive(endInclusive))
        }

        fun rangeInclusive(startInclusive: ULong, endInclusive: ULong): Times {
            require(startInclusive <= endInclusive) {
                "range start must be less than or equal to range end"
            }
            return Times(TimesEnum.RangeInclusive(startInclusive, endInclusive))
        }
    }
}

// Implementation notes: this has gone through a couple of iterations before landing to
// what you see now.
//
// The original draft had Times itself as a sealed type with two variants, Exact and Range,
// with the Range variant generic over a range-bounds type.
//
// We switched to a generic struct wrapper around a private range-bounds value when we
// realised that you would have had to specify a range type when creating the Exact variant.
//
// We achieved the same functionality with a struct wrapper, but exact values had to be
// converted to ranges with a single element.
// Not the most expressive representation, but we would have lived with it.
//
// We changed once again when we started to update our Mock actor: we are storing all Mocks
// in a list. Being generic over the range type leaked into the overall Mock and MountedMock
// type, thus making those generic as well over the range type.
// To store them in a list all mocks would have had to use the same range internally, which is
// obviously an unreasonable restriction.
// At the same time, we cannot have an erased range-bounds object because contains is generic
// and therefore object-safety requirements are not satisfied.
//
// Thus we ended up creating this master sealed type that wraps all range variants with the
// addition of the Exact variant.
// If you can do better, please submit a PR.
// We keep the variants private to the module to allow for future refactoring.
private sealed interface TimesEnum {
    fun contains(nCalls: ULong): Boolean
    fun display(): String

    data class Exact(val value: ULong) : TimesEnum {
        override fun contains(nCalls: ULong): Boolean {
            return value == nCalls
        }

        override fun display(): String {
            return "== $value"
        }
    }

    data object Unbounded : TimesEnum {
        override fun contains(nCalls: ULong): Boolean {
            return true
        }

        override fun display(): String {
            return "0 <= x"
        }
    }

    data class Range(val start: ULong, val endExclusive: ULong) : TimesEnum {
        override fun contains(nCalls: ULong): Boolean {
            return nCalls >= start && nCalls < endExclusive
        }

        override fun display(): String {
            return "$start <= x < $endExclusive"
        }
    }

    data class RangeFrom(val start: ULong) : TimesEnum {
        override fun contains(nCalls: ULong): Boolean {
            return nCalls >= start
        }

        override fun display(): String {
            return "$start <= x"
        }
    }

    data class RangeTo(val endExclusive: ULong) : TimesEnum {
        override fun contains(nCalls: ULong): Boolean {
            return nCalls < endExclusive
        }

        override fun display(): String {
            return "0 <= x < $endExclusive"
        }
    }

    data class RangeToInclusive(val endInclusive: ULong) : TimesEnum {
        override fun contains(nCalls: ULong): Boolean {
            return nCalls <= endInclusive
        }

        override fun display(): String {
            return "0 <= x <= $endInclusive"
        }
    }

    data class RangeInclusive(val start: ULong, val endInclusive: ULong) : TimesEnum {
        override fun contains(nCalls: ULong): Boolean {
            return nCalls >= start && nCalls <= endInclusive
        }

        override fun display(): String {
            return "$start <= x <= $endInclusive"
        }
    }
}
