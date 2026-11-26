package io.github.openflocon.flocon.deeplinks.model

internal data class DeeplinkModel(
    val link: String,
    val label: String? = null,
    val description: String? = null,
    val parameters: List<Parameter>
) {
    sealed interface Parameter {
        val name: String

        data class AutoComplete(
            override val name: String,
            val suggestions: List<DeeplinkSuggestion>
        ) : Parameter

        data class Variable(
            override val name: String,
            val variableName: String
        ) : Parameter
    }
}

internal data class DeeplinkSuggestion(
    val value: String,
    val label: String? = null,
    val isDefault: Boolean = false
)
