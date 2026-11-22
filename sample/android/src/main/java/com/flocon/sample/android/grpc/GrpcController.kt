package com.flocon.sample.android.grpc

import com.flocon.sample.android.wire.GrpcGreeterClient
import com.flocon.sample.android.wire.HelloRequest
import com.squareup.wire.GrpcClient
import io.github.openflocon.flocon.grpc.lite.FloconGrpcInterceptor
import io.github.openflocon.flocon.wire.grpc.withFlocon
import io.grpc.CallOptions
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.examples.helloworld.GreeterGrpcKt
import io.grpc.examples.helloworld.helloRequest
import okhttp3.OkHttpClient
import okhttp3.Protocol

/**
 * Calls the test server of grpc/grpc-test-server (start.sh), reachable on localhost:50051 with `adb reverse tcp:50051 tcp:50051`
 */
object GrpcController {
    private val channel: ManagedChannel by lazy {
        ManagedChannelBuilder
            .forAddress("localhost", 50051)
            .usePlaintext()
            .intercept(
                FloconGrpcInterceptor()
            )
            .build()
    }

    private val greeterClient by lazy {
        GreeterGrpcKt.GreeterCoroutineStub(
            channel = channel,
            callOptions = CallOptions.DEFAULT,
        )
    }

    private val wireGreeter by lazy {
        val transport = OkHttpClient.Builder()
            .protocols(listOf(Protocol.H2_PRIOR_KNOWLEDGE))
            .build()
        val client = GrpcClient.Builder()
            .client(transport)
            .baseUrl("http://localhost:50051")
            .build()
            .withFlocon(authority = "localhost:50051")
        GrpcGreeterClient(client)
    }

    suspend fun sayHelloWithWire(): String =
        wireGreeter.SayHello().execute(HelloRequest(name = "florent")).message

    suspend fun sayHello(): String? {
        return try {
            val request = helloRequest {
                name = "florent"
            }
            greeterClient.sayHello(request).message
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }
}
