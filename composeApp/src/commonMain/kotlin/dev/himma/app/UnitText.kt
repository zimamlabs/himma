package dev.himma.app

import androidx.compose.runtime.Composable
import dev.himma.app.res.Res
import dev.himma.app.res.duration_hours_minutes
import dev.himma.app.res.duration_minutes
import dev.himma.app.res.unit_kg
import dev.himma.app.res.unit_lb
import dev.himma.app.res.water_litres
import dev.himma.app.res.water_ml
import dev.himma.core.WeightUnit
import dev.himma.core.durationParts
import dev.himma.core.numberText
import dev.himma.core.waterAmount

/** Amounts and durations in the words and digits of the chosen language ("1.25 L" or its Arabic form). */
@Composable
fun waterText(ml: Int): String = waterAmount(ml).let { if (it.litres) s(Res.string.water_litres, it.value) else s(Res.string.water_ml, it.value) }

@Composable
fun durationText(minutes: Int): String {
    val (h, m) = durationParts(minutes)
    return if (h == 0) s(Res.string.duration_minutes, m) else s(Res.string.duration_hours_minutes, h, m.toString().padStart(2, '0'))
}

/** A weight stored in kg, shown in [unit]. */
@Composable
fun weightText(kg: Double, unit: WeightUnit = WeightUnit.Kg): String =
    s(if (unit == WeightUnit.Lb) Res.string.unit_lb else Res.string.unit_kg, ltr(numberText(unit.fromKg(kg))))

/** Keeps a number with its sign in one piece inside right-to-left text (a bare minus sign would end up on the wrong side). */
fun ltr(number: String): String = "\u2066$number\u2069"
