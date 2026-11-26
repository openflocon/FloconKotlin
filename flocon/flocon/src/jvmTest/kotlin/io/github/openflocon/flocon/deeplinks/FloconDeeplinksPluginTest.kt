package io.github.openflocon.flocon.deeplinks

import io.github.openflocon.flocon.Protocol
import io.github.openflocon.flocon.core.FloconEncoder
import io.github.openflocon.flocon.core.FloconMessageSender
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class FloconDeeplinksPluginTest {

    private class FakeFloconMessageSender : FloconMessageSender {
        val messages = mutableListOf<Triple<String, String, String>>()
        override fun send(plugin: String, method: String, body: String) {
            messages += Triple(plugin, method, body)
        }
        override fun sendPendingMessages() {}
    }

    @Test
    fun `configured deeplinks are sent when connected`() = runTest {
        val sender = FakeFloconMessageSender()
        val config = FloconDeeplinksConfigImpl().apply {
            variable("userId")
            deeplink("flocon://user/[userId]") {
                label = "User"
                "userId" withAutoComplete listOf("Florent", "David")
            }
        }
        val plugin = FloconDeeplinksPluginImpl(
            deeplinks = config.deeplinks(),
            variables = config.variables(),
            sender = sender,
            encoder = FloconEncoder(FloconDeeplinkEncoding().serializersModule),
        )

        plugin.onConnectedToServer()

        val (pluginName, method, body) = sender.messages.single()
        assertEquals(Protocol.FromDevice.Deeplink.Plugin, pluginName)
        assertEquals(Protocol.FromDevice.Deeplink.Method.GetDeeplinks, method)
        assertContains(body, "flocon://user/[userId]")
        assertContains(body, "Florent")
    }

    private suspend fun sentBody(block: FloconDeeplinksConfig.() -> Unit): String {
        val sender = FakeFloconMessageSender()
        val config = FloconDeeplinksConfigImpl().apply(block)
        val plugin = FloconDeeplinksPluginImpl(
            deeplinks = config.deeplinks(),
            variables = config.variables(),
            sender = sender,
            encoder = FloconEncoder(FloconDeeplinkEncoding().serializersModule),
        )
        plugin.onConnectedToServer()
        return sender.messages.single().third
    }

    @Test
    fun `labelled and default suggestions are sent next to the plain values`() = runTest {
        val body = sentBody {
            variable("env") {
                autoComplete {
                    "Production" suggest "prod"
                    "Staging" suggestDefault "staging"
                }
            }
            deeplink("flocon://home?tab=[tab]") {
                "tab" withAutoComplete {
                    suggest("posts")
                    "Friends list" suggest "friends"
                }
            }
        }

        // values stay in the legacy fields
        assertContains(body, """"suggestions":["prod","staging"]""")
        assertContains(body, """"autoComplete":["posts","friends"]""")
        // labelled items
        assertContains(body, """{"value":"prod","label":"Production"}""")
        assertContains(body, """{"value":"staging","label":"Staging","isDefault":true}""")
        assertContains(body, """{"value":"friends","label":"Friends list"}""")
    }

    @Test
    fun `plain string suggestions have no label and no default`() = runTest {
        val body = sentBody {
            deeplink("flocon://user/[userId]") { "userId" withAutoComplete listOf("Florent", "Florent", "David") }
        }

        assertContains(body, """"autoComplete":["Florent","David"]""")
        assertContains(body, """"suggestions":[{"value":"Florent"},{"value":"David"}]""")
    }

    @Test
    fun `only the first default stays and a duplicated value keeps its last declaration`() {
        val suggestions = DeeplinkSuggestionsBuilder().apply {
            "A" suggestDefault "a"
            "B" suggestDefault "b"
            "A bis" suggest "a"
        }.build()

        assertEquals(listOf("b", "a"), suggestions.map { it.value })
        // "a" was redeclared without default, the first default left is "b"
        assertEquals(listOf(true, false), suggestions.map { it.isDefault })
        assertEquals("A bis", suggestions.last().label)
    }

    @Test
    fun `deeplinks registered after install are sent and kept for reconnection`() = runTest {
        val sender = FakeFloconMessageSender()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val config = FloconDeeplinksConfigImpl().apply { deeplink("flocon://home") }
        val plugin = FloconDeeplinksPluginImpl(
            deeplinks = config.deeplinks(),
            variables = config.variables(),
            sender = sender,
            encoder = FloconEncoder(FloconDeeplinkEncoding().serializersModule),
            dispatcher = dispatcher,
        )

        plugin.register {
            variable("userId")
            deeplink("flocon://user/[userId]") { "userId" withAutoComplete listOf("Florent") }
        }
        assertEquals(1, sender.messages.size)
        assertContains(sender.messages.last().third, "flocon://home")
        assertContains(sender.messages.last().third, "flocon://user/[userId]")

        plugin.onConnectedToServer()
        assertEquals(2, sender.messages.size)
        assertContains(sender.messages.last().third, "flocon://user/[userId]")
    }
}
