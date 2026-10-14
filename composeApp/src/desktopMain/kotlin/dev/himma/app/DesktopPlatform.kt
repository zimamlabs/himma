package dev.himma.app

import java.io.File
import java.util.Locale
import java.util.Properties

/** The desktop build: the language and digit choices are kept in a small file inside the data folder. */
class DesktopPlatform(override val folderPath: String) : PlatformActions {
    private val file = File(folderPath, "ui.properties")
    private val props = Properties().apply { runCatching { file.inputStream().use { load(it) } } }
    private var code = props.getProperty("language", "system").takeIf { it in setOf("system", "en", "ar") } ?: "system"

    init { apply() }

    private fun apply() {
        if (code != "system") Locale.setDefault(Locale.forLanguageTag(code))
        UiText.rtl = when (code) { "ar" -> true; "en" -> false; else -> null }
        UiText.arabicDigits = props.getProperty("arabicDigits") == "true"
        UiText.generation++
    }

    private fun save() { runCatching { File(folderPath).mkdirs(); file.outputStream().use { props.store(it, "Himma interface choices") } } }

    override val language: String get() = code
    override fun setLanguage(code: String) { this.code = code; props.setProperty("language", code); save(); apply() }
    override val arabicDigits: Boolean get() = UiText.arabicDigits
    override fun setArabicDigits(on: Boolean) { props.setProperty("arabicDigits", on.toString()); save(); apply() }
}
