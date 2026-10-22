package io.github.kotlinmania.wiremock

import io.github.kotlinmania.wiremock.mockserver.BareMockServer
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MockGuardSatisfactionTest {
    private fun expected(count: ULong): Mock =
        Mock
            .given(Matchers.method("POST"))
            .respondWith(ResponseTemplate.new(200))
            .expect(count)

    @Test
    fun waitsForTheExpectedNumberOfMatchingRequests() =
        runTest {
            val server = BareMockServer.start()
            val guard = server.registerAsScoped(expected(2uL))
            val waiter = async(start = CoroutineStart.UNDISPATCHED) { guard.waitUntilSatisfied() }
            assertFalse(waiter.isCompleted)
            server.handleRequest(Request(url = "/", method = "GET"))
            assertFalse(waiter.isCompleted)
            server.handleRequest(Request(url = "/", method = "POST"))
            assertFalse(waiter.isCompleted)
            server.handleRequest(Request(url = "/", method = "POST"))
            waiter.await()
            assertTrue(guard.verify().isSatisfied())
            assertEquals(2, guard.receivedRequests().size)
        }

    @Test
    fun satisfactionRecordedBeforeWaitingIsRetained() =
        runTest {
            val server = BareMockServer.start()
            val guard = server.registerAsScoped(expected(1uL))
            server.handleRequest(Request(url = "/", method = "POST"))
            val waiter = async(start = CoroutineStart.UNDISPATCHED) { guard.waitUntilSatisfied() }
            assertTrue(waiter.isCompleted)
            waiter.await()
        }

    @Test
    fun allRegisteredWaitersObserveSatisfaction() =
        runTest {
            val server = BareMockServer.start()
            val guard = server.registerAsScoped(expected(1uL))
            val first = async(start = CoroutineStart.UNDISPATCHED) { guard.waitUntilSatisfied() }
            val second = async(start = CoroutineStart.UNDISPATCHED) { guard.waitUntilSatisfied() }
            assertFalse(first.isCompleted)
            assertFalse(second.isCompleted)
            server.handleRequest(Request(url = "/", method = "POST"))
            first.await()
            second.await()
        }

    @Test
    fun cancellingOneWaiterDoesNotCancelTheSharedNotification() =
        runTest {
            val server = BareMockServer.start()
            val guard = server.registerAsScoped(expected(1uL))
            val cancelled = async(start = CoroutineStart.UNDISPATCHED) { guard.waitUntilSatisfied() }
            cancelled.cancel()
            cancelled.join()
            val remaining = async(start = CoroutineStart.UNDISPATCHED) { guard.waitUntilSatisfied() }
            assertFalse(remaining.isCompleted)
            server.handleRequest(Request(url = "/", method = "POST"))
            remaining.await()
        }

    @Test
    fun historicalSatisfactionSurvivesAnExceededExactExpectation() =
        runTest {
            val server = BareMockServer.start()
            val guard = server.registerAsScoped(expected(1uL))
            repeat(2) { server.handleRequest(Request(url = "/", method = "POST")) }
            assertFalse(guard.verify().isSatisfied())
            val waiter = async(start = CoroutineStart.UNDISPATCHED) { guard.waitUntilSatisfied() }
            assertTrue(waiter.isCompleted)
            waiter.await()
        }

    @Test
    fun receivedRequestSnapshotsDoNotGrowAfterLaterMatches() =
        runTest {
            val server = BareMockServer.start()
            val guard = server.registerAsScoped(expected(2uL))
            server.handleRequest(Request(url = "/first", method = "POST"))
            val snapshot = guard.receivedRequests()
            server.handleRequest(Request(url = "/second", method = "POST"))
            assertEquals(1, snapshot.size)
            assertEquals(2, guard.receivedRequests().size)
        }
}
