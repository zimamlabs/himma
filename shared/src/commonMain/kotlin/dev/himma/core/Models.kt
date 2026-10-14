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
)
