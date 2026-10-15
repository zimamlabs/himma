package dev.himma.core

import kotlin.math.abs
import kotlin.math.round

/** Body weight is always stored in kg; the unit only changes what is shown and typed. */
enum class WeightUnit(val code: String, private val perKg: Double) {
    Kg("kg", 1.0), Lb("lb", 2.2046226218);

    fun fromKg(kg: Double): Double = kg * perKg
    fun toKg(value: Double): Double = value / perKg

    companion object { fun fromCode(code: String): WeightUnit = entries.firstOrNull { it.code == code } ?: Kg }
}

/** "62.5" or "60": one decimal, no trailing zero. */
fun numberText(value: Double): String {
    val r = round(value * 10) / 10
    return (if (r == 0.0) 0.0 else r).let { if (abs(it) < 1e-9) "0" else it.toString().removeSuffix(".0") }
}

/** A water amount as a number and whether it is in litres (from 1000 ml up) or millilitres. */
data class WaterAmount(val value: String, val litres: Boolean)

fun waterAmount(ml: Int): WaterAmount =
    if (ml < 1000) WaterAmount(ml.toString(), false) else WaterAmount((round(ml / 1000.0 * 100) / 100).toString().removeSuffix(".0"), true)

/** Hours and minutes of a duration. */
fun durationParts(minutes: Int): Pair<Int, Int> = minutes / 60 to minutes % 60
