// port-lint: source request.rs
package io.github.kotlinmania.wiremock

/**
 * Limitations on printing request bodies when logging requests.
 */
public sealed class BodyPrintLimit {
    public data class Limited(
        val limit: Int,
    ) : BodyPrintLimit()

    public object Unlimited : BodyPrintLimit()
}

/**
 * An incoming request to a mock server.
 */
public data class Request(
    public val url: String,
    public val method: String,
    public val headers: Map<String, List<String>> = emptyMap(),
    public val body: ByteArray = ByteArray(0),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Request) return false
        if (url != other.url) return false
        if (method != other.method) return false
        if (headers != other.headers) return false
        if (!body.contentEquals(other.body)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + headers.hashCode()
        result = 31 * result + body.contentHashCode()
        return result
    }

    public fun bodyString(): String = body.decodeToString()

    public companion object {
        public const val BODY_PRINT_LIMIT: Int = 10_000
    }
}
