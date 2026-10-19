package com.example.reminder

import java.util.Locale

private val TR = Locale("tr", "TR")

/**
 * Turkish-safe case folding for comparisons: `.lowercase()` alone treats i/ı and İ/I
 * as unrelated letters under the Turkish locale, which silently breaks matches
 * (e.g. "İş" vs "iş"). Fold to the Turkish locale first, then flatten the
 * dotless/dotted i distinction so either spelling matches.
 */
fun String.normalizeForSearch(): String =
    lowercase(TR).replace('ı', 'i')
