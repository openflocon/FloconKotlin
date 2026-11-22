package io.github.openflocon.flocon.wire.grpc

import com.squareup.wire.GrpcCall
import com.squareup.wire.GrpcException
import com.squareup.wire.GrpcMethod
import com.squareup.wire.GrpcStatus
import io.github.openflocon.flocon.FloconLogger
import io.github.openflocon.flocon.network.core.FloconNetworkPlugin
import io.github.openflocon.flocon.network.core.model.FloconNetworkCallRequest
import io.github.openflocon.flocon.network.core.model.FloconNetworkCallResponse
import io.github.openflocon.flocon.network.core.model.FloconNetworkRequest
import io.github.openflocon.flocon.network.core.model.FloconNetworkResponse
import okio.IOException
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.DurationUnit
import kotlin.time.TimeSource
import kotlin.uuid.Uuid

internal class WireGrpcCall<S : Any, R : Any>(
    private val delegate: GrpcCall<S, R>,
    override val method: GrpcMethod<S, R>,
    private val authority: String,
    private val plugin: FloconNetworkPlugin,
) : GrpcCall<S, R> by delegate {

    override suspend fun execute(request: S): R = observe(request) { delegate.execute(request) }

    override fun executeBlocking(request: S): R = observe(request) { delegate.executeBlocking(request) }

    override fun enqueue(request: S, callback: GrpcCall.Callback<S, R>) {
        val exchange = Exchange(request)
        try {
            delegate.enqueue(
                request,
                object : GrpcCall.Callback<S, R> {
                    override fun onSuccess(call: GrpcCall<S, R>, response: R) {
                        exchange.finish(response, null)
                        callback.onSuccess(this@WireGrpcCall, response)
                    }

                    override fun onFailure(call: GrpcCall<S, R>, exception: IOException) {
                        exchange.finish(null, exception)
                        callback.onFailure(this@WireGrpcCall, exception)
                    }
                }
            )
        } catch (e: Exception) {
            exchange.finish(null, e)
            throw e
        }
    }

    override fun clone(): GrpcCall<S, R> = WireGrpcCall(delegate.clone(), method, authority, plugin)

    private inline fun observe(request: S, execute: () -> R): R {
        val exchange = Exchange(request)
        return try {
            execute().also { exchange.finish(it, null) }
        } catch (e: Exception) {
            exchange.finish(null, e)
            throw e
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    private inner class Exchange(request: S) {
        private val completed = AtomicBoolean(false)
        private val id = Uuid.random().toString()
        private val start = TimeSource.Monotonic.markNow()

        init {
            report {
                val data = FloconNetworkRequest(
                    url = authority,
                    method = method.path.removePrefix("/"),
                    startTime = Clock.System.now().toEpochMilliseconds(),
                    headers = requestMetadata,
                    body = method.requestAdapter.toString(request),
                    size = method.requestAdapter.encodedSize(request).toLong(),
                    isMocked = false,
                )
                plugin.logRequest(FloconNetworkCallRequest(id, data, "grpc", isMocked = false))
            }
        }

        fun finish(response: R?, exception: Exception?) = report {
            // A delegate may invoke the callback inline, and the caller’s callback can throw.
            if (!completed.compareAndSet(false, true)) return@report
            val durationMs = start.elapsedNow().toDouble(DurationUnit.MILLISECONDS)
            val rpcError = exception as? GrpcException
            val status = when {
                exception == null -> GrpcStatus.OK
                rpcError != null -> rpcError.grpcStatus
                exception is CancellationException || isCanceled() -> GrpcStatus.CANCELLED
                else -> null
            }
            val data = FloconNetworkResponse(
                httpCode = null,
                grpcStatus = status?.name,
                contentType = "grpc",
                body = response?.let(method.responseAdapter::toString) ?: rpcError?.grpcMessage,
                size = response?.let { method.responseAdapter.encodedSize(it).toLong() },
                headers = responseMetadata.orEmpty(),
                requestHeaders = null,
                error = if (status == null) exception.toString() else null,
                isImage = false,
            )
            plugin.logResponse(FloconNetworkCallResponse(id, data, durationMs, "grpc", isMocked = false))
        }
    }

    private inline fun report(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            FloconLogger.logError("Could not report Wire RPC ${method.path}", e)
        }
    }
}
