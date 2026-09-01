// port-lint: source matchers.rs
package io.github.kotlinmania.wiremock

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Strategy interface to match incoming HTTP requests.
 */
public fun interface Match {
    public fun matches(request: Request): Boolean
}

/**
 * Match all incoming requests, regardless of their method, path, headers, or body.
 */
public class AnyMatcher : Match {
    override fun matches(request: Request): Boolean = true
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
    path: String,
) : Match {
    public val path: String

    init {
        if (path.contains('?')) {
            throw IllegalArgumentException(
                "Wiremock can't match the path `$path` because it contains a `?`. You must use `wiremock::matchers::query_param` to match on query parameters (the part of the path after the `?`).",
            )
        }
        if (path.contains("://")) {
            val withoutScheme = path.substringAfter("://")
            val host = withoutScheme.substringBefore('/')
            val pathPart = if (withoutScheme.contains('/')) "/" + withoutScheme.substringAfter('/') else "/"
            throw IllegalArgumentException(
                "Wiremock can't match the path `$path` because it contains the host `$host`. You don't have to specify the host - wiremock knows it. Try replacing your path with `path(\"$pathPart\")`",
            )
        }
        this.path = if (path.startsWith('/')) path else "/$path"
    }

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
        val normalizedPath = if (pathPart.startsWith('/')) pathPart else "/$pathPart"
        return normalizedPath == this.path
    }

    public companion object {
        public fun new(path: String): PathExactMatcher = PathExactMatcher(path)
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
        return regex.containsMatchIn(pathPart)
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
 * Match if a header with the given key exists.
 */
public class HeaderExistsMatcher(
    public val key: String,
) : Match {
    override fun matches(request: Request): Boolean =
        request.headers.keys.any { it.equals(key, ignoreCase = true) }
}

/**
 * Match header values against a regular expression.
 */
public class HeaderRegexMatcher(
    public val key: String,
    public val regex: Regex,
) : Match {
    override fun matches(request: Request): Boolean {
        val values =
            request.headers.entries
                .firstOrNull { it.key.equals(key, ignoreCase = true) }
                ?.value ?: return false
        return values.any { regex.containsMatchIn(it) }
    }
}

/**
 * Match multiple header values for a single key.
 */
public class HeaderMultiMatcher(
    public val key: String,
    public val values: List<String>,
) : Match {
    override fun matches(request: Request): Boolean {
        val reqValues =
            request.headers.entries
                .firstOrNull { it.key.equals(key, ignoreCase = true) }
                ?.value ?: return false
        return values.all { reqValues.contains(it) }
    }
}

/**
 * Match request body exact bytes or JSON.
 */
public class BodyExactMatcher(
    public val body: ByteArray,
) : Match {
    override fun matches(request: Request): Boolean {
        if (request.body.contentEquals(body)) return true
        val reqStr = request.bodyString().filter { !it.isWhitespace() }
        val expectedStr = body.decodeToString().filter { !it.isWhitespace() }
        if (reqStr == expectedStr) return true
        if (reqStr.startsWith('{') && reqStr.endsWith('}') && expectedStr.startsWith('{') && expectedStr.endsWith('}')) {
            val reqTokens =
                reqStr
                    .drop(1)
                    .dropLast(1)
                    .split(',')
                    .sorted()
            val expTokens =
                expectedStr
                    .drop(1)
                    .dropLast(1)
                    .split(',')
                    .sorted()
            return reqTokens == expTokens
        }
        return false
    }

    public companion object {
        public fun string(body: String): BodyExactMatcher = BodyExactMatcher(body.encodeToByteArray())

        public fun bytes(body: ByteArray): BodyExactMatcher = BodyExactMatcher(body.copyOf())

        public fun json(body: String): BodyExactMatcher = BodyExactMatcher(body.encodeToByteArray())

        public fun jsonString(body: String): BodyExactMatcher = BodyExactMatcher(body.encodeToByteArray())
    }
}

/**
 * Match request body string.
 */
public class BodyStringMatcher(
    public val body: String,
) : Match {
    override fun matches(request: Request): Boolean = request.bodyString() == body
}

/**
 * Match if the request body string contains a substring.
 */
public class BodyContainsMatcher(
    public val substring: String,
) : Match {
    override fun matches(request: Request): Boolean = request.bodyString().contains(substring)

    public companion object {
        public fun string(body: String): BodyContainsMatcher = BodyContainsMatcher(body)
    }
}

/**
 * Match partial JSON body.
 */
public class BodyPartialJsonMatcher(
    public val jsonPart: String,
) : Match {
    override fun matches(request: Request): Boolean {
        val reqBody = request.bodyString()
        val cleanedReq = reqBody.filter { !it.isWhitespace() }
        val tokens = jsonPart.filter { !it.isWhitespace() && it != '{' && it != '}' }.split(',')
        return tokens.all { token ->
            if (token.isEmpty()) true else cleanedReq.contains(token)
        }
    }

    public companion object {
        public fun json(body: String): BodyPartialJsonMatcher = BodyPartialJsonMatcher(body)

        public fun jsonString(body: String): BodyPartialJsonMatcher = BodyPartialJsonMatcher(body)
    }
}

/**
 * Match exact query parameter key and value.
 */
public class QueryParamExactMatcher(
    public val key: String,
    public val value: String,
) : Match {
    override fun matches(request: Request): Boolean {
        val query = request.url.substringAfter('?', "")
        if (query.isEmpty()) return false
        val params =
            query.split('&').map {
                val parts = it.split('=', limit = 2)
                parts[0] to (parts.getOrNull(1) ?: "")
            }
        return params.any { it.first == key && it.second == value }
    }
}

/**
 * Match query parameter containing substring in its value.
 */
public class QueryParamContainsMatcher(
    public val key: String,
    public val substring: String,
) : Match {
    override fun matches(request: Request): Boolean {
        val query = request.url.substringAfter('?', "")
        if (query.isEmpty()) return false
        val params =
            query.split('&').map {
                val parts = it.split('=', limit = 2)
                parts[0] to (parts.getOrNull(1) ?: "")
            }
        return params.any { it.first == key && it.second.contains(substring) }
    }
}

/**
 * Match if a query parameter key is missing.
 */
public class QueryParamIsMissingMatcher(
    public val key: String,
) : Match {
    override fun matches(request: Request): Boolean {
        val query = request.url.substringAfter('?', "")
        if (query.isEmpty()) return true
        val params = query.split('&').map { it.substringBefore('=') }
        return !params.contains(key)
    }
}

/**
 * Match basic authentication header.
 */
public class BasicAuthMatcher(
    private val headerMatcher: HeaderExactMatcher,
) : Match {
    override fun matches(request: Request): Boolean = headerMatcher.matches(request)

    public companion object {
        @OptIn(ExperimentalEncodingApi::class)
        public fun fromCredentials(
            username: String,
            password: String,
        ): BasicAuthMatcher {
            val encoded = Base64.encode("$username:$password".encodeToByteArray())
            return fromToken(encoded)
        }

        public fun fromToken(token: String): BasicAuthMatcher =
            BasicAuthMatcher(HeaderExactMatcher("Authorization", "Basic $token"))
    }
}

/**
 * Match bearer token authentication header.
 */
public class BearerTokenMatcher(
    private val headerMatcher: HeaderExactMatcher,
) : Match {
    override fun matches(request: Request): Boolean = headerMatcher.matches(request)

    public companion object {
        public fun fromToken(token: String): BearerTokenMatcher =
            BearerTokenMatcher(HeaderExactMatcher("Authorization", "Bearer $token"))
    }
}

public object Matchers {
    public fun any(): Match = AnyMatcher()

    public fun method(method: String): Match = MethodExactMatcher(method)

    public fun path(path: String): Match = PathExactMatcher(path)

    public fun pathRegex(regex: String): Match = PathRegexMatcher(Regex(regex))

    public fun header(
        key: String,
        value: String,
    ): Match = HeaderExactMatcher(key, value)

    public fun headerExact(
        key: String,
        value: String,
    ): Match = HeaderExactMatcher(key, value)

    public fun headerExists(key: String): Match = HeaderExistsMatcher(key)

    public fun headerRegex(
        key: String,
        regex: Regex,
    ): Match = HeaderRegexMatcher(key, regex)

    public fun headers(
        key: String,
        values: List<String>,
    ): Match = HeaderMultiMatcher(key, values)

    public fun bodyString(body: String): Match = BodyStringMatcher(body)

    public fun bodyStringContains(substring: String): Match = BodyContainsMatcher(substring)

    public fun bodyBytes(body: ByteArray): Match = BodyExactMatcher(body)

    public fun bodyJson(body: String): Match = BodyExactMatcher.json(body)

    public fun bodyJsonString(body: String): Match = BodyExactMatcher.jsonString(body)

    public fun bodyPartialJson(body: String): Match = BodyPartialJsonMatcher.json(body)

    public fun bodyPartialJsonString(body: String): Match = BodyPartialJsonMatcher.jsonString(body)

    public fun bodyJsonSchema(validator: (Request) -> Boolean): Match = Match { validator(it) }

    public fun queryParam(
        key: String,
        value: String,
    ): Match = QueryParamExactMatcher(key, value)

    public fun queryParamContains(
        key: String,
        substring: String,
    ): Match = QueryParamContainsMatcher(key, substring)

    public fun queryParamIsMissing(key: String): Match = QueryParamIsMissingMatcher(key)

    public fun basicAuth(
        username: String,
        password: String,
    ): Match = BasicAuthMatcher.fromCredentials(username, password)

    public fun bearerToken(token: String): Match = BearerTokenMatcher.fromToken(token)
}
