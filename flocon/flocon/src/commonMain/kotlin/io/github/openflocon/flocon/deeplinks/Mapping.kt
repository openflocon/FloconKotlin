package io.github.openflocon.flocon.deeplinks

import io.github.openflocon.flocon.deeplinks.model.DeeplinkModel
import io.github.openflocon.flocon.deeplinks.model.DeeplinkParameterRemote
import io.github.openflocon.flocon.deeplinks.model.DeeplinkRemote
import io.github.openflocon.flocon.deeplinks.model.DeeplinkSuggestion
import io.github.openflocon.flocon.deeplinks.model.DeeplinkSuggestionRemote
import io.github.openflocon.flocon.deeplinks.model.DeeplinkVariableRemote
import io.github.openflocon.flocon.deeplinks.model.DeeplinksRemote

internal fun createRemote(
    deeplinks: List<DeeplinkModel>,
    variables: List<DeeplinkVariable>
) = DeeplinksRemote(
    deeplinks = deeplinks.map(DeeplinkModel::toRemote),
    variables = variables.map(DeeplinkVariable::toRemote)
)

internal fun DeeplinkModel.toRemote(): DeeplinkRemote = DeeplinkRemote(
    label = label,
    link = link,
    description = description,
    parameters = parameters.map { it.toRemote() }
)

internal fun DeeplinkModel.Parameter.toRemote(): DeeplinkParameterRemote = when (this) {
    is DeeplinkModel.Parameter.AutoComplete -> DeeplinkParameterRemote.AutoComplete(
        name = name,
        autoComplete = suggestions.map { it.value },
        suggestions = suggestions.map { it.toRemote() }
    )

    is DeeplinkModel.Parameter.Variable -> DeeplinkParameterRemote.Variable(
        name = name,
        variableName = variableName
    )
}

internal fun DeeplinkSuggestion.toRemote() = DeeplinkSuggestionRemote(
    value = value,
    label = label,
    isDefault = isDefault
)

internal fun DeeplinkVariable.toRemote() = DeeplinkVariableRemote(
    name = name,
    mode = mode.toRemote()
)

internal fun DeeplinkVariable.Mode.toRemote() = when (this) {
    is DeeplinkVariable.Mode.AutoComplete -> DeeplinkVariableRemote.Mode.AutoComplete(
        suggestions = suggestions.map { it.value },
        items = suggestions.map { it.toRemote() }
    )

    DeeplinkVariable.Mode.Input -> DeeplinkVariableRemote.Mode.Input
}
