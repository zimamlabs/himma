package dev.himma.core

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.serialization.Serializable

/** What one day held, for the Home charts and the weekly summary. */
data class DayRecord(val date: LocalDate, val kcal: Int = 0, val waterMl: Int = 0, val steps: Int = 0, val workouts: Int = 0, val volumeKg: Double = 0.0)

/** The last [n] dates ending at [today], oldest first. */
fun lastDays(today: LocalDate, n: Int): List<LocalDate> = (n - 1 downTo 0).map { today.minus(DatePeriod(days = it)) }

data class WeekSummary(
    val avgKcal: Int, val avgWaterMl: Int, val waterGoalDays: Int, val avgSteps: Int,
    val workouts: Int, val volumeKg: Int, val weightChangeKg: Double?,
)

/** Averages count only the days that have a value (a day with nothing logged is not a zero). */
fun weekSummary(days: List<DayRecord>, waterGoalMl: Int, weightChangeKg: Double?): WeekSummary {
    fun avg(f: (DayRecord) -> Int): Int = days.map(f).filter { it > 0 }.let { if (it.isEmpty()) 0 else it.sum() / it.size }
    return WeekSummary(
        avgKcal = avg { it.kcal }, avgWaterMl = avg { it.waterMl },
        waterGoalDays = days.count { waterGoalMl > 0 && it.waterMl >= waterGoalMl },
        avgSteps = avg { it.steps }, workouts = days.sumOf { it.workouts },
        volumeKg = days.sumOf { it.volumeKg }.toInt(), weightChangeKg = weightChangeKg,
    )
}

/** A meal you eat often, saved to log again in one tap. */
@Serializable
data class MealTemplate(val id: String, val name: String, val kcal: Int, val protein: Int? = null, val carbs: Int? = null, val fat: Int? = null)

/** The distinct recent meals (same name and calories counted once), newest first, at most [limit]. [newestFirst] must already be newest first. */
fun recentMeals(newestFirst: List<Meal>, limit: Int): List<Meal> =
    newestFirst.distinctBy { it.name.trim().lowercase() to it.kcal }.take(limit)
