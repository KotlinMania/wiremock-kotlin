// port-lint: source wiremock/src/response_template.rs
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

    public var mime: String = ""
        private set

    public fun appendHeader(
        key: String,
        value: String,
    ): ResponseTemplate {
        _headers.getOrPut(key) { mutableListOf() }.add(value)
        return this
    }

    public fun appendHeaders(headers: List<HeaderEntry>): ResponseTemplate {
        for (header in headers) {
            appendHeader(header.name, header.value)
        }
        return this
    }

    public fun appendHeaders(vararg headers: HeaderEntry): ResponseTemplate = appendHeaders(headers.toList())

    public fun appendHeaders(headers: Map<String, List<String>>): ResponseTemplate {
        for ((name, values) in headers) {
            for (value in values) {
                appendHeader(name, value)
            }
        }
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
        mime = "text/plain"
        return this
    }

    public fun setBodyJson(bodyJson: String): ResponseTemplate {
        body = bodyJson.encodeToByteArray()
        mime = "application/json"
        _headers["Content-Type"] = mutableListOf("application/json")
        return this
    }

    public fun setBodyRaw(bodyBytes: ByteArray, mimeType: String): ResponseTemplate {
        body = bodyBytes.copyOf()
        mime = mimeType
        _headers["Content-Type"] = mutableListOf(mimeType)
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

    public fun generateResponse(): ResponseTemplate = this


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

/**
 * An HTTP header entry with name and value.
 */
public data class HeaderEntry(
    public val name: String,
    public val value: String,
)

