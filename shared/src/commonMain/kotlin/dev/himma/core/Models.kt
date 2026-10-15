package dev.himma.core

import kotlinx.serialization.Serializable

/** One logged meal. Protein, carbs and fat are optional grams. */
data class Meal(
    val id: String,
    val time: String,
    val name: String,
    val kcal: Int,
    val protein: Int? = null,
    val carbs: Int? = null,
    val fat: Int? = null,
)

/** Goals and the fasting window, kept in `.himma/settings.json`. The eating window is 24 - [fastHours] hours long. */
@Serializable
data class Settings(
    val calorieGoal: Int = 2000,
    val waterGoalMl: Int = 2000,
    val fastHours: Int = 16,
    val eatStartMinutes: Int = 12 * 60,
    /** The version whose notes were last shown; null until the first run records it. */
    val lastSeenVersion: String? = null,
    val tourDone: Boolean = false,
    /** "system", "light" or "dark". */
    val themeMode: String = "system",
    val stepGoal: Int = 8000,
    /** "kg" or "lb": how body weight is shown and typed (stored in kg either way). */
    val weightUnit: String = "kg",
    /** Whether a dose notification shows the medicine name on a locked screen. Off unless the user turns it on. */
    val lockScreenNames: Boolean = false,
    /** Colour theme id, one of [Palettes.ids]. */
    val palette: String = Palettes.DEFAULT,
    /** Text size step, 0 (small) to 3 (largest); 1 is normal. */
    val textSize: Int = 1,
    /** The section shown on opening: one of [Sections.all]. */
    val startSection: String = Sections.HOME,
    /** Sections in the order the user wants; empty means the default order. Unknown names are ignored. */
    val sectionOrder: List<String> = emptyList(),
    /** Sections hidden from the bottom bar and the side menu. Home cannot be hidden. */
    val hiddenSections: Set<String> = emptySet(),
)
