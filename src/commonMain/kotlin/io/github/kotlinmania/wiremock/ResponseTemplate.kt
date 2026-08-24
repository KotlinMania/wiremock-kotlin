// port-lint: source response_template.rs
package io.github.kotlinmania.wiremock

import kotlin.time.Duration

/**
 * The blueprint for the response returned by a mock when matched.
 */
public class ResponseTemplate(
    public val statusCode: Int,
) : Respond {
    private val _headers: MutableMap<String, MutableList<String>> = mutableMapOf()
    public val headers: Map<String, List<String>> get() = _headers

    public var body: ByteArray? = null
        private set

    public var delay: Duration? = null
        private set

    public fun appendHeader(
        key: String,
        value: String,
    ): ResponseTemplate {
        _headers.getOrPut(key) { mutableListOf() }.add(value)
        return this
    }

    public fun insertHeader(
        key: String,
        value: String,
    ): ResponseTemplate {
        _headers[key] = mutableListOf(value)
        return this
    }

    public fun setBodyString(bodyString: String): ResponseTemplate {
        body = bodyString.encodeToByteArray()
        return this
    }

    public fun setBodyBytes(bodyBytes: ByteArray): ResponseTemplate {
        body = bodyBytes.copyOf()
        return this
    }

    public fun setDelay(duration: Duration): ResponseTemplate {
        delay = duration
        return this
    }

    override fun respond(request: Request): ResponseTemplate = this

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ResponseTemplate) return false
        if (statusCode != other.statusCode) return false
        if (_headers != other._headers) return false
        if (delay != other.delay) return false
        val thisBody = body
        val otherBody = other.body
        if (thisBody != null) {
            if (otherBody == null || !thisBody.contentEquals(otherBody)) return false
        } else if (otherBody != null) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = statusCode
        result = 31 * result + _headers.hashCode()
        result = 31 * result + (body?.contentHashCode() ?: 0)
        result = 31 * result + (delay?.hashCode() ?: 0)
        return result
    }

    public companion object {
        public fun new(statusCode: Int): ResponseTemplate = ResponseTemplate(statusCode)
    }
}
