package com.cbruegg.mensaupb.util

import java.util.Locale

/**
 * @param [values] Locale.language to value
 */
class LanguageStringSelector<out S : String?>
private constructor(private val values: Map<String, S>, private val sCaster: (S?) -> S) {

    companion object {
        @JvmName("LanguageStringSelector")
        operator fun invoke(vararg values: Pair<String, String>) = LanguageStringSelector(mapOf(*values), sCaster = { it!! })

        @JvmName("NullableLanguageStringSelector")
        operator fun invoke(vararg values: Pair<String, String?>) = LanguageStringSelector(mapOf(*values), sCaster = { it })
    }

    /**
     * Return the translated String for the supplied Locale.
     */
    operator fun invoke(locale: Locale = Locale.getDefault()): S =
        sCaster(values.getOrElse(locale.language) { values["en"] })
}