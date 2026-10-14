package dev.himma.app

import dev.himma.core.greeting
import dev.himma.core.releaseNotes
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** English and Arabic must never drift apart: same keys, same placeholders, and every shared English text has a translation. */
class TranslationsTest {
    private fun entries(path: String): Map<String, String> {
        val text = File(path).readText()
        val found = Regex("""<(string|plurals|string-array) name="([^"]+)"[^>]*>(.*?)</\1>""", RegexOption.DOT_MATCHES_ALL).findAll(text)
        return found.associate { "${it.groupValues[1]}:${it.groupValues[2]}" to it.groupValues[3] }
    }

    private fun placeholders(text: String) = Regex("""%\d[$]s""").findAll(text).map { it.value }.sorted().toList()

    private fun compare(en: String, ar: String) {
        val e = entries(en); val a = entries(ar)
        assertEquals(emptySet(), e.keys - a.keys, "missing in Arabic ($ar)")
        assertEquals(emptySet(), a.keys - e.keys, "only in Arabic ($ar)")
        for ((key, text) in e) if (key.startsWith("string:")) assertEquals(placeholders(text), placeholders(a.getValue(key)), "placeholders of $key")
    }

    @Test fun sharedStringsMatch() = compare("src/commonMain/composeResources/values/strings.xml", "src/commonMain/composeResources/values-ar/strings.xml")

    @Test fun androidStringsMatch() = compare("src/androidMain/res/values/strings.xml", "src/androidMain/res/values-ar/strings.xml")

    @Test fun everySharedEnglishTextHasATranslation() {
        val texts = releaseNotes.flatMap { it.bullets } + listOf(5, 12, 18).map { greeting(it) }
        for (t in texts) assertTrue(t in knownTexts, "no translation for: $t")
    }

    @Test fun arabicDigitsConvertOnlyDigits() = assertEquals("٠١٢٣ abc ٤٥", toArabicDigits("0123 abc 45"))
}
