// port-lint: source mock_server/pool.rs
package io.github.kotlinmania.wiremock.mockserver

internal object MockServerPool {
    private val pool: MutableList<BareMockServer> = mutableListOf()

    public fun getPooledMockServer(): BareMockServer {
        return if (pool.isNotEmpty()) {
            pool.removeAt(pool.size - 1)
        } else {
            MockServerBuilder.new().buildBare()
        }
    }

    public fun returnToPool(server: BareMockServer) {
        pool.add(server)
    }
}
