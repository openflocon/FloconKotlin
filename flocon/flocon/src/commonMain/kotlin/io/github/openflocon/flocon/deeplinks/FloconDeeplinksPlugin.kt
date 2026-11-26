package io.github.openflocon.flocon.deeplinks

import io.github.openflocon.flocon.FloconPlugin
import io.github.openflocon.flocon.deeplinks.model.DeeplinkModel
import io.github.openflocon.flocon.deeplinks.model.DeeplinkSuggestion

class DeeplinkLinkBuilder internal constructor(
    private val link: String
) {
    private val parameters: MutableMap<String, DeeplinkModel.Parameter> = mutableMapOf()

    var label: String? = null
    var description: String? = null

    infix fun String.withAutoComplete(suggestions: List<String>) {
        parameters[this] = DeeplinkModel.Parameter.AutoComplete(
            name = this,
            suggestions = suggestions.toSuggestions()
        )
    }

    /** Suggestions with an optional label, and an optional default one, see [DeeplinkSuggestionsBuilder]. */
    infix fun String.withAutoComplete(block: DeeplinkSuggestionsBuilder.() -> Unit) {
        parameters[this] = DeeplinkModel.Parameter.AutoComplete(
            name = this,
            suggestions = DeeplinkSuggestionsBuilder().apply(block).build()
        )
    }

    infix fun String.withVariable(variableName: String) {
        parameters[this] = DeeplinkModel.Parameter.Variable(
            name = this,
            variableName = variableName
        )
    }

    internal fun build() = DeeplinkModel(
        link = link,
        label = label,
        description = description,
        parameters = parameters.values
            .toList()
    )
}

class DeeplinkVariableBuilder internal constructor(
    private val name: String
) {
    private var mode: DeeplinkVariable.Mode = DeeplinkVariable.Mode.Input

    var description: String? = null

    fun autoComplete(suggestions: List<String>) {
        mode = DeeplinkVariable.Mode.AutoComplete(suggestions.toSuggestions())
    }

    /** Suggestions with an optional label, and an optional default one, see [DeeplinkSuggestionsBuilder]. */
    fun autoComplete(block: DeeplinkSuggestionsBuilder.() -> Unit) {
        mode = DeeplinkVariable.Mode.AutoComplete(DeeplinkSuggestionsBuilder().apply(block).build())
    }

    internal fun build(): DeeplinkVariable = DeeplinkVariable(
        name = name,
        mode = mode,
        description = description
    )
}

internal data class DeeplinkVariable(
    val name: String,
    val mode: Mode = Mode.Input,
    val description: String? = null
) {

    sealed interface Mode {
        object Input : Mode
        data class AutoComplete(val suggestions: List<DeeplinkSuggestion>) : Mode
    }
}

interface FloconDeeplinksPlugin : FloconPlugin {
    /**
     * Registers more deeplinks and variables after [startFlocon], e.g. once the user is logged in.
     * A deeplink with an already registered link, or a variable with an already registered name,
     * replaces the previous one. The desktop is updated right away and after each reconnection.
     */
    fun register(block: FloconDeeplinksConfig.() -> Unit)
}
