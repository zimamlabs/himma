package dev.himma.core

import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus

data class WeightEntry(val date: LocalDate, val kg: Double)

/** The latest weight, the change over the window, and the slope in kg per week (null with fewer than two points). */
data class WeightTrend(val latestKg: Double?, val changeKg: Double?, val kgPerWeek: Double?)

/** Least-squares trend over the entries in the last [windowDays] days up to [today]. */
fun weightTrend(entries: List<WeightEntry>, today: LocalDate, windowDays: Int = 28): WeightTrend {
    val all = entries.sortedBy { it.date }
    val latest = all.lastOrNull { it.date <= today }?.kg
    val from = today.minus(DatePeriod(days = windowDays))
    val pts = all.filter { it.date in from..today }
    if (pts.size < 2) return WeightTrend(latest, null, null)
    val xs = pts.map { from.daysUntil(it.date).toDouble() }
    val ys = pts.map { it.kg }
    val mx = xs.average(); val my = ys.average()
    val den = xs.sumOf { (it - mx) * (it - mx) }
    val slope = if (den == 0.0) null else xs.indices.sumOf { (xs[it] - mx) * (ys[it] - my) } / den * 7
    return WeightTrend(latest, pts.last().kg - pts.first().kg, slope)
}

/** Each entry with the average of it and up to [n] - 1 entries before it, for a smoother chart line. */
fun movingAverage(entries: List<WeightEntry>, n: Int = 7): List<WeightEntry> {
    val all = entries.sortedBy { it.date }
    return all.indices.map { i -> WeightEntry(all[i].date, all.subList(maxOf(0, i - n + 1), i + 1).map { it.kg }.average()) }
}
