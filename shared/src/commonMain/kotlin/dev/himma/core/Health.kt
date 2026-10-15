package dev.himma.core

/** Calories and macros for one day. */
data class DayTotals(val kcal: Int, val protein: Int, val carbs: Int, val fat: Int)

fun dayTotals(meals: List<Meal>) = DayTotals(
    kcal = meals.sumOf { it.kcal },
    protein = meals.sumOf { it.protein ?: 0 },
    carbs = meals.sumOf { it.carbs ?: 0 },
    fat = meals.sumOf { it.fat ?: 0 },
)

/** A meal above this is almost certainly a typo (a missing decimal point), so the form refuses it. */
const val MAX_MEAL_KCAL = 10000

/** Calories typed for one meal, or null when it is not a whole number from 0 to [MAX_MEAL_KCAL]. */
fun mealKcal(text: String): Int? = text.trim().toIntOrNull()?.takeIf { it in 0..MAX_MEAL_KCAL }

/** Calories left for the day; negative once the goal is passed. */
fun caloriesLeft(goal: Int, totals: DayTotals): Int = goal - totals.kcal

/** Water as a share of the goal, 0 to 1 (the ring stops at full). */
fun waterProgress(ml: Int, goalMl: Int): Float = if (goalMl <= 0) 0f else (ml.toFloat() / goalMl).coerceIn(0f, 1f)

/** Adds [deltaMl] to a day's total, never going below zero (so undo cannot overshoot). */
fun addWater(totalMl: Int, deltaMl: Int): Int = (totalMl + deltaMl).coerceAtLeast(0)

private const val DAY = 24 * 60

/** Where the fasting clock is: [eating] now or fasting, and [minutesLeft] until it flips. */
data class FastState(val eating: Boolean, val minutesLeft: Int)

/**
 * The eating window opens at [eatStartMinutes] (minutes after midnight) and lasts 24 - [fastHours] hours;
 * the rest of the day is the fast. Works when the window crosses midnight.
 */
fun fastState(eatStartMinutes: Int, fastHours: Int, nowMinutes: Int): FastState {
    val eatMinutes = DAY - fastHours.coerceIn(1, 23) * 60
    val offset = ((nowMinutes - eatStartMinutes) % DAY + DAY) % DAY
    return if (offset < eatMinutes) FastState(true, eatMinutes - offset) else FastState(false, DAY - offset)
}

fun formatClock(minutesOfDay: Int): String {
    val m = ((minutesOfDay % DAY) + DAY) % DAY
    return (m / 60).toString().padStart(2, '0') + ":" + (m % 60).toString().padStart(2, '0')
}

/** Reads "HH:MM" (or "H:MM") as minutes after midnight; null when it is not a time. */
fun parseClock(text: String): Int? {
    val parts = text.trim().split(':')
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    return if (h in 0..23 && m in 0..59) h * 60 + m else null
}
