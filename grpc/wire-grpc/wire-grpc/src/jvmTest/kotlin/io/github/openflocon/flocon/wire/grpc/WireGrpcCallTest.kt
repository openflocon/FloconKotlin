package io.github.openflocon.flocon.wire.grpc

import com.squareup.wire.GrpcCall
import com.squareup.wire.GrpcClient
import com.squareup.wire.GrpcException
import com.squareup.wire.GrpcMethod
import com.squareup.wire.GrpcStatus
import com.squareup.wire.ProtoAdapter
import io.github.openflocon.flocon.network.core.FloconNetworkPlugin
import io.github.openflocon.flocon.network.core.model.BadQualityConfig
import io.github.openflocon.flocon.network.core.model.FloconNetworkCallRequest
import io.github.openflocon.flocon.network.core.model.FloconNetworkCallResponse
import io.github.openflocon.flocon.network.core.model.FloconWebSocketEvent
import io.github.openflocon.flocon.network.core.model.FloconWebSocketMockListener
import io.github.openflocon.flocon.network.core.model.MockNetworkResponse
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.Headers.Companion.headersOf
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okio.Buffer
import okio.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class WireGrpcCallTest {
    private val method = GrpcMethod("/sample.Echo/Echo", ProtoAdapter.STRING, ProtoAdapter.STRING)
    private val requests = mutableListOf<FloconNetworkCallRequest>()
    private val responses = mutableListOf<FloconNetworkCallResponse>()
    private val recorder = object : FloconNetworkPlugin {
        override val key = "Network"
        override val badQualityConfig: BadQualityConfig? = null
        override val mocks = emptyList<MockNetworkResponse>()
        override fun logWebSocket(event: FloconWebSocketEvent) = Unit
        override fun registerWebSocketMockListener(id: String, listener: FloconWebSocketMockListener) = Unit
        override suspend fun onMessageReceived(method: String, body: String) = Unit
        override suspend fun onConnectedToServer() = Unit
        override fun logRequest(request: FloconNetworkCallRequest) {
            requests.add(request)
        }
        override fun logResponse(response: FloconNetworkCallResponse) {
            responses.add(response)
        }
    }

    @Test
    fun `successful call and clone have distinct correlated entries`() = runTest {
        val call = WireGrpcCall(GrpcCall<String, String> { it.uppercase() }, method, "example.test", recorder)
        call.requestMetadata = mapOf("request-id" to "abc")
        assertEquals("HELLO", call.execute("hello"))
        assertEquals("AGAIN", call.clone().executeBlocking("again"))

        assertEquals(listOf("hello", "again"), requests.map { it.request.body })
        assertEquals("sample.Echo/Echo", requests.first().request.method)
        assertEquals("example.test", requests.first().request.url)
        assertEquals("abc", requests.first().request.headers["request-id"])
        assertEquals(listOf("HELLO", "AGAIN"), responses.map { it.response.body })
        assertEquals(listOf("OK", "OK"), responses.map { it.response.grpcStatus })
        assertEquals(requests.map { it.floconCallId }, responses.map { it.floconCallId })
        assertNotEquals(requests.first().floconCallId, requests.last().floconCallId)
    }

    @Test
    fun `HTTP 200 with unavailable in the trailers is a failed RPC`() {
        MockWebServer().use { server ->
            server.protocols = listOf(Protocol.H2_PRIOR_KNOWLEDGE)
            server.start()
            server.enqueue(
                MockResponse.Builder()
                    .code(200)
                    .addHeader("content-type", "application/grpc")
                    .body(Buffer())
                    .trailers(headersOf("grpc-status", "14", "grpc-message", "maintenance"))
                    .build()
            )
            val client = GrpcClient.Builder()
                .client(OkHttpClient.Builder().protocols(listOf(Protocol.H2_PRIOR_KNOWLEDGE)).build())
                .baseUrl(server.url("/"))
                .build()
            val call = WireGrpcCall(client.newCall(method), method, server.hostName, recorder)

            val error = assertFailsWith<GrpcException> { call.executeBlocking("hello") }

            assertEquals(GrpcStatus.UNAVAILABLE, error.grpcStatus)
            val entry = responses.single()
            assertEquals("grpc", entry.floconNetworkType)
            assertEquals(requests.single().floconCallId, entry.floconCallId)
            assertEquals("UNAVAILABLE", entry.response.grpcStatus)
            assertEquals("maintenance", entry.response.body)
            assertNull(entry.response.error)
        }
    }

    @Test
    fun `inline callback throwing does not overwrite a successful RPC`() {
        val call = WireGrpcCall(GrpcCall<String, String> { it }, method, "example.test", recorder)
        val callbackError = IllegalStateException("callback failed")
        val callback = object : GrpcCall.Callback<String, String> {
            override fun onSuccess(call: GrpcCall<String, String>, response: String) = throw callbackError
            override fun onFailure(call: GrpcCall<String, String>, exception: IOException) = error("unexpected failure")
        }

        assertSame(callbackError, assertFailsWith<IllegalStateException> { call.enqueue("one", callback) })
        assertEquals("OK", responses.single().response.grpcStatus)

        // Wire rejects the second enqueue synchronously, before invoking either callback.
        assertFailsWith<IllegalStateException> { call.enqueue("two", callback) }
        assertEquals(requests.map { it.floconCallId }, responses.map { it.floconCallId })
        assertNull(responses.last().response.grpcStatus)
    }

    @Test
    fun `callback receives the original transport failure and the wrapped call`() {
        val failure = IOException("connection closed")
        val wrapped = WireGrpcCall(GrpcCall<String, String> { throw failure }, method, "example.test", recorder)
        wrapped.enqueue(
            "hello",
            object : GrpcCall.Callback<String, String> {
                override fun onSuccess(call: GrpcCall<String, String>, response: String) = error("unexpected success")
                override fun onFailure(call: GrpcCall<String, String>, exception: IOException) {
                    assertSame(wrapped, call)
                    assertSame(failure, exception)
                }
            }
        )
        assertNull(responses.single().response.grpcStatus)
        assertEquals(failure.toString(), responses.single().response.error)
    }
}
