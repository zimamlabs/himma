package dev.himma.app

import dev.himma.core.greeting
import dev.himma.core.releaseNotes
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** English and Arabic must never drift apart: same keys, same placeholders, and every shared English text has a translation. */
class TranslationsTest {
    // A path ending in strings.xml stands for every strings*.xml next to it (new screens keep their texts in their own file).
    private fun entries(path: String): Map<String, String> {
        val dir = File(path).parentFile
        val text = dir.listFiles { f -> f.name.startsWith("strings") && f.name.endsWith(".xml") }!!.sortedBy { it.name }.joinToString("\n") { it.readText() }
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

    // Arabic has six plural forms; a missing one falls back to "other" and reads wrongly for that count.
    @Test fun everyArabicPluralHasAllSixForms() {
        val plurals = entries("src/commonMain/composeResources/values-ar/strings.xml").filterKeys { it.startsWith("plurals:") }
        assertTrue(plurals.isNotEmpty())
        for ((key, body) in plurals) for (q in listOf("zero", "one", "two", "few", "many", "other")) assertTrue("quantity=\"$q\"" in body, "$key lacks $q")
    }

    @Test fun englishPluralsHaveOneAndOther() {
        val plurals = entries("src/commonMain/composeResources/values/strings.xml").filterKeys { it.startsWith("plurals:") }
        for ((key, body) in plurals) for (q in listOf("one", "other")) assertTrue("quantity=\"$q\"" in body, "$key lacks $q")
    }

    // Shared resources show an escaped apostrophe with its backslash ("What\'s new"); a plain ' is correct there.
    @Test fun sharedStringsHaveNoEscapedApostrophe() {
        for (f in listOf("values", "values-ar")) for (x in File("src/commonMain/composeResources/$f").listFiles()!!) assertTrue("\\'" !in x.readText(), "${x.name} has a backslash before an apostrophe")
    }
}
