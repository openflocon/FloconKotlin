package io.github.openflocon.flocon.myapplication.multi.tester

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/** Calls made with an OkHttp client without the Flocon interceptor: only the Flocon proxy sees them. */
object ProxyOnlyTester {

    // no FloconOkhttpInterceptor: it uses the proxy of the phone like any other client
    private val client = OkHttpClient()

    suspend fun callHttps(): String = call("https://jsonplaceholder.typicode.com/todos/2")

    suspend fun callHttp(): String = call("http://httpbin.org/get?from=flocon-sample")

    private suspend fun call(url: String): String = withContext(Dispatchers.IO) {
        client.newCall(Request.Builder().url(url).get().build()).execute().use { "HTTP ${it.code}" }
    }
}
