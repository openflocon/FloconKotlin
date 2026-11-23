package io.github.openflocon.flocon.deeplinks

import io.github.openflocon.flocon.deeplinks.model.DeeplinkSuggestion

@DslMarker
annotation class DeeplinkSuggestionsDsl

/**
 * Declares the suggestions of a deeplink parameter or variable.
 *
 * ```
 * "tab" withAutoComplete {
 *     suggest("posts")                  // value only
 *     "Friends list" suggest "friends"  // label suggest value
 *     "All" suggestDefault "all"        // pre-selected on the desktop
 * }
 * ```
 */
@DeeplinkSuggestionsDsl
class DeeplinkSuggestionsBuilder internal constructor() {
    private val suggestions = mutableListOf<DeeplinkSuggestion>()

    /** Suggests [value], displayed as is. */
    fun suggest(value: String) {
        suggestions += DeeplinkSuggestion(value = value)
    }

    /** Suggests [value] and pre-selects it when the field is empty. */
    fun suggestDefault(value: String) {
        suggestions += DeeplinkSuggestion(value = value, isDefault = true)
    }

    /** Suggests [value], displayed with this label. */
    infix fun String.suggest(value: String) {
        suggestions += DeeplinkSuggestion(value = value, label = this)
    }

    /** Suggests [value], displayed with this label, and pre-selects it when the field is empty. */
    infix fun String.suggestDefault(value: String) {
        suggestions += DeeplinkSuggestion(value = value, label = this, isDefault = true)
    }

    /** A value declared twice keeps its last declaration; only the first default stays a default. */
    internal fun build(): List<DeeplinkSuggestion> = suggestions.normalized()
}

internal fun List<String>.toSuggestions(): List<DeeplinkSuggestion> = map { DeeplinkSuggestion(value = it) }
    .normalized()

internal fun List<DeeplinkSuggestion>.normalized(): List<DeeplinkSuggestion> {
    val unique = asReversed().distinctBy { it.value }.asReversed()
    val defaultValue = unique.firstOrNull { it.isDefault }?.value
    return unique.map { it.copy(isDefault = defaultValue != null && it.value == defaultValue) }
}
