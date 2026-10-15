package dev.himma.core

/** The colour themes the user can pick; the colours themselves are defined once in the app's theme. */
object Palettes {
    const val DEFAULT = "zimam"
    val ids = listOf(DEFAULT, "ocean", "forest", "violet", "rose", "graphite")
}

/** The main sections of the app, in their default order. */
object Sections {
    const val HOME = "home"
    val all = listOf(HOME, "meals", "gym", "track", "medicine")

    /** The sections to show: the user's [order] first (unknown names dropped), then any missing ones, minus [hidden]. Home is always kept. */
    fun visible(order: List<String>, hidden: Set<String>): List<String> {
        val ordered = order.filter { it in all }.distinct().let { it + (all - it.toSet()) }
        return ordered.filter { it == HOME || it !in hidden }
    }
}

/** The cards Home can show, in their default order; Home shows at most these four. */
object HomeCards {
    val all = listOf("water", "medicine", "meals", "steps")

    fun visible(order: List<String>, hidden: Set<String>): List<String> =
        order.filter { it in all }.distinct().let { it + (all - it.toSet()) }.filter { it !in hidden }
}

/** The values a settings file is allowed to hold; anything else (a hand edit, an older or newer version) is brought back into range. */
fun Settings.normalized(): Settings = copy(
    calorieGoal = calorieGoal.coerceIn(1, 20000),
    waterGoalMl = waterGoalMl.coerceIn(1, 20000),
    stepGoal = stepGoal.coerceIn(100, 100000),
    fastHours = fastHours.coerceIn(1, 23),
    eatStartMinutes = eatStartMinutes.coerceIn(0, 24 * 60 - 1),
    themeMode = themeMode.takeIf { it in setOf("system", "light", "dark") } ?: "system",
    weightUnit = WeightUnit.fromCode(weightUnit).code,
    density = density.takeIf { it in setOf("comfortable", "compact") } ?: "comfortable",
    palette = palette.takeIf { it in Palettes.ids } ?: Palettes.DEFAULT,
    textSize = textSize.coerceIn(0, 3),
    startSection = startSection.takeIf { it in Sections.all && (it == Sections.HOME || it !in hiddenSections) } ?: Sections.HOME,
    homeCardOrder = homeCardOrder.filter { it in HomeCards.all }.distinct(),
    hiddenHomeCards = hiddenHomeCards.filter { it in HomeCards.all }.toSet(),
    sectionOrder = sectionOrder.filter { it in Sections.all }.distinct(),
    hiddenSections = hiddenSections.filter { it in Sections.all && it != Sections.HOME }.toSet(),
)

/** The one place settings are read and written: always normalised, always saved through the vault's atomic write. */
class SettingsRepository(private val vault: Vault) {
    var current: Settings = vault.settings().normalized()
        private set

    fun reload(): Settings { current = vault.settings().normalized(); return current }

    fun update(change: (Settings) -> Settings): Settings {
        val next = change(current).normalized()
        vault.saveSettings(next)
        current = next
        return next
    }
}
