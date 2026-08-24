// port-lint: source matchers.rs
package io.github.kotlinmania.wiremock

/**
 * Strategy interface to match incoming HTTP requests.
 */
public fun interface Match {
    public fun matches(request: Request): Boolean
}

/**
 * Match exactly the HTTP method.
 */
public class MethodExactMatcher(
    public val method: String,
) : Match {
    override fun matches(request: Request): Boolean = request.method.equals(method, ignoreCase = true)
}

/**
 * Match exactly the request path.
 */
public class PathExactMatcher(
    public val path: String,
) : Match {
    override fun matches(request: Request): Boolean {
        val url = request.url
        val pathPart =
            if (url.contains("://")) {
                val withoutScheme = url.substringAfter("://")
                val slashIndex = withoutScheme.indexOf('/')
                if (slashIndex != -1) withoutScheme.substring(slashIndex).substringBefore('?') else "/"
            } else {
                url.substringBefore('?')
            }
        return pathPart == path
    }
}

/**
 * Match regex pattern against the request path.
 */
public class PathRegexMatcher(
    public val regex: Regex,
) : Match {
    override fun matches(request: Request): Boolean {
        val url = request.url
        val pathPart =
            if (url.contains("://")) {
                val withoutScheme = url.substringAfter("://")
                val slashIndex = withoutScheme.indexOf('/')
                if (slashIndex != -1) withoutScheme.substring(slashIndex).substringBefore('?') else "/"
            } else {
                url.substringBefore('?')
            }
        return regex.matches(pathPart)
    }
}

/**
 * Match header exact value.
 */
public class HeaderExactMatcher(
    public val key: String,
    public val value: String,
) : Match {
    override fun matches(request: Request): Boolean {
        val values =
            request.headers.entries
                .firstOrNull { it.key.equals(key, ignoreCase = true) }
                ?.value ?: return false
        return values.contains(value)
    }
}

/**
 * Match request body exact bytes.
 */
public class BodyExactMatcher(
    public val body: ByteArray,
) : Match {
    override fun matches(request: Request): Boolean = request.body.contentEquals(body)
}

/**
 * Match request body string.
 */
public class BodyStringMatcher(
    public val body: String,
) : Match {
    override fun matches(request: Request): Boolean = request.bodyString() == body
}

public object Matchers {
    public fun method(method: String): Match = MethodExactMatcher(method)

    public fun path(path: String): Match = PathExactMatcher(path)

    public fun pathRegex(regex: String): Match = PathRegexMatcher(Regex(regex))

    public fun header(
        key: String,
        value: String,
    ): Match = HeaderExactMatcher(key, value)

    public fun bodyString(body: String): Match = BodyStringMatcher(body)

    public fun bodyBytes(body: ByteArray): Match = BodyExactMatcher(body)
}
