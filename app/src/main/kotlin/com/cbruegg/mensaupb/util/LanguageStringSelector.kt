package com.cbruegg.mensaupb.util

import java.util.Locale

/**
 * @param [values] Locale.language to value
 */
class LanguageStringSelector<out S: String?>(private val values: Map<String, S>) {

    /**
     * Return the translated String for the supplied Locale.
     */
    operator fun invoke(locale: Locale = Locale.getDefault()): S =
            values.getOrElse(locale.language) { values["en"] } as S
}

/**
 * @see [LanguageStringSelector]
 */
fun <S: String?> LanguageStringSelector(vararg values: Pair<String, S>) =
        LanguageStringSelector(mapOf(*values))