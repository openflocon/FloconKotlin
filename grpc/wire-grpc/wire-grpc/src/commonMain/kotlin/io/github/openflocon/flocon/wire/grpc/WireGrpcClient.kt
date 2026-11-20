package io.github.openflocon.flocon.wire.grpc

import com.squareup.wire.GrpcCall
import com.squareup.wire.GrpcClient
import com.squareup.wire.GrpcClientStreamingCall
import com.squareup.wire.GrpcMethod
import com.squareup.wire.GrpcServerStreamingCall
import com.squareup.wire.GrpcStreamingCall
import io.github.openflocon.flocon.Flocon
import io.github.openflocon.flocon.FloconRuntime
import io.github.openflocon.flocon.network.core.networkPluginOrNull

/**
 * Reports unary RPCs to FloconNetwork. [authority] identifies the server in the inspector
 * (for example, "localhost:50051"). Wrap the configured client before creating service stubs.
 */
fun GrpcClient.withFlocon(authority: String): GrpcClient = if (FloconRuntime.isEnabled()) WireGrpcClient(this, authority) else this

private class WireGrpcClient(
    private val delegate: GrpcClient,
    private val authority: String,
) : GrpcClient() {
    override fun <S : Any, R : Any> newCall(method: GrpcMethod<S, R>): GrpcCall<S, R> {
        val call = delegate.newCall(method)
        val plugin = Flocon.networkPluginOrNull ?: return call
        return WireGrpcCall(call, method, authority, plugin)
    }

    override fun <S : Any, R : Any> newStreamingCall(method: GrpcMethod<S, R>): GrpcStreamingCall<S, R> = delegate.newStreamingCall(method)

    override fun <S : Any, R : Any> newClientStreamingCall(method: GrpcMethod<S, R>): GrpcClientStreamingCall<S, R> = delegate.newClientStreamingCall(method)

    override fun <S : Any, R : Any> newServerStreamingCall(method: GrpcMethod<S, R>): GrpcServerStreamingCall<S, R> = delegate.newServerStreamingCall(method)
}
