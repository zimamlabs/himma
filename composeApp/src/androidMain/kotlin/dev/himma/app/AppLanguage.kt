package dev.himma.app

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/** The language of the app itself: "system" follows the phone; "en" and "ar" are fixed. Kept in plain SharedPreferences so it is known before the first frame. */
object AppLanguagePrefs {
    private const val FILE = "himma-ui"
    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    class Choice(val code: String) {
        /** The context in the chosen language; the system choice leaves it alone. */
        fun wrap(base: Context): Context {
            if (code == "system") return base
            val locale = Locale.forLanguageTag(code)
            Locale.setDefault(locale)
            val config = Configuration(base.resources.configuration)
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            return base.createConfigurationContext(config)
        }
    }

    fun load(context: Context): Choice = Choice(prefs(context).getString("language", "system")?.takeIf { it in setOf("system", "en", "ar") } ?: "system")
    fun save(context: Context, code: String) { prefs(context).edit().putString("language", code).apply() }
    fun arabicDigits(context: Context): Boolean = prefs(context).getBoolean("arabic_digits", false)
    fun saveArabicDigits(context: Context, on: Boolean) { prefs(context).edit().putBoolean("arabic_digits", on).apply() }
}
