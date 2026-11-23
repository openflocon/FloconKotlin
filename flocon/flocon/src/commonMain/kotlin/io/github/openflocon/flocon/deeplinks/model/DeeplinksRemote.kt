package io.github.openflocon.flocon.deeplinks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
internal class DeeplinkRemote(
    val label: String? = null,
    val link: String,
    val description: String? = null,
    val parameters: List<DeeplinkParameterRemote>,
)

@Serializable
internal class DeeplinksRemote(
    val deeplinks: List<DeeplinkRemote>,
    val variables: List<DeeplinkVariableRemote>
)

@Serializable
internal data class DeeplinkVariableRemote(
    val name: String,
    val mode: Mode = Mode.Input,
    val description: String? = null
) {

    // same format as the desktop DeeplinkVariableReceivedDataModel
    @Serializable
    @JsonClassDiscriminator("type")
    sealed interface Mode {
        @Serializable
        @SerialName("input")
        object Input : Mode

        // `suggestions` (the values) stays for desktops that don't know `items` yet
        @Serializable
        @SerialName("auto_complete")
        data class AutoComplete(
            val suggestions: List<String>,
            val items: List<DeeplinkSuggestionRemote>? = null
        ) : Mode
    }
}

@Serializable
internal data class DeeplinkSuggestionRemote(
    val value: String,
    val label: String? = null,
    val isDefault: Boolean = false
)

@JsonClassDiscriminator("type")
internal sealed interface DeeplinkParameterRemote {
    val name: String

    @Serializable
    @SerialName("auto_complete")
    data class AutoComplete(
        override val name: String,
        // the values, for desktops that don't know `suggestions` yet
        val autoComplete: List<String>,
        val suggestions: List<DeeplinkSuggestionRemote>? = null
    ) : DeeplinkParameterRemote

    @Serializable
    @SerialName("variable")
    data class Variable(
        override val name: String,
        val variableName: String
    ) : DeeplinkParameterRemote
}
