package io.github.openflocon.flocon.myapplication.multi.ui.screens

import androidx.compose.runtime.Composable
import io.github.openflocon.flocon.myapplication.multi.ui.SampleFeature
import io.github.openflocon.flocon.myapplication.multi.ui.SampleScreen
import io.github.openflocon.flocon.myapplication.multi.ui.components.ActionGroup
import io.github.openflocon.flocon.myapplication.multi.ui.components.InfoCard
import io.github.openflocon.flocon.myapplication.multi.ui.theme.SampleTheme

/** Calls made without the Flocon interceptors: they only show up in Flocon through its proxy mode. */
@Composable
fun ProxyScreen(onBack: () -> Unit) = PluginScreen(SampleScreen.Proxy, onBack) {
    ActionGroup("Without the SDK", listOf(SampleFeature.ProxyHttps, SampleFeature.ProxyHttp))
    InfoCard(
        text = "Turn on the proxy in Flocon Desktop and set it as the Wi-Fi proxy of the phone " +
            "(127.0.0.1 and the proxy port when the phone is plugged with adb). The calls show up in Flocon " +
            "under the \"Proxy\" app of this phone, not under this app.",
        tint = SampleTheme.colors.accent,
    )
    PlatformNote(SampleScreen.Proxy)
}
