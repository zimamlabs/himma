package dev.himma.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/** Choices that change how text is shown; set before the first screen and again when the user changes them. */
object UiText {
    /** Show 0123 as Arabic-Indic digits. */
    var arabicDigits by mutableStateOf(false)

    /** Forces right-to-left or left-to-right where the system does not (the desktop); null leaves it to the system. */
    var rtl by mutableStateOf<Boolean?>(null)

    /** Bumped when the language changes, so the screens start over in it. */
    var generation by mutableStateOf(0)
}

fun toArabicDigits(text: String): String {
    val out = StringBuilder(text.length)
    for (c in text) out.append(if (c in '0'..'9') '\u0660' + (c - '0') else c)
    return out.toString()
}

/** A string from the resources of the current language, with the Arabic-Indic digits when that choice is on. */
@Composable
fun s(id: StringResource, vararg args: Any?): String {
    val text = stringResource(id, *args.map { it.toString() }.toTypedArray())
    return if (UiText.arabicDigits) toArabicDigits(text) else text
}

/** A plural string; the count is the first format argument. */
@Composable
fun sp(id: PluralStringResource, count: Int, vararg args: Any?): String {
    val text = pluralStringResource(id, count, count, *args.map { it.toString() }.toTypedArray())
    return if (UiText.arabicDigits) toArabicDigits(text) else text
}
