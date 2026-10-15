package dev.himma.core

import kotlin.test.Test
import kotlin.test.assertEquals

class UnitsTest {
    @Test fun numbersDropAZeroDecimalAndRoundToOne() {
        assertEquals("60", numberText(60.0))
        assertEquals("62.5", numberText(62.46))
        assertEquals("0", numberText(0.0))
        assertEquals("-0.3", numberText(-0.26))
    }

    @Test fun waterIsMillilitresUnderALitreAndLitresAbove() {
        assertEquals(WaterAmount("250", false), waterAmount(250))
        assertEquals(WaterAmount("1.25", true), waterAmount(1250))
        assertEquals(WaterAmount("2", true), waterAmount(2000))
        assertEquals(WaterAmount("999", false), waterAmount(999))
    }

    @Test fun durationSplitsIntoHoursAndMinutes() {
        assertEquals(0 to 42, durationParts(42))
        assertEquals(6 to 5, durationParts(365))
        assertEquals(1 to 0, durationParts(60))
    }

    @Test fun poundsConvertBothWaysAndKgStaysOnDisk() {
        assertEquals("165.3", numberText(WeightUnit.Lb.fromKg(75.0)))
        assertEquals(75.0, WeightUnit.Lb.toKg(WeightUnit.Lb.fromKg(75.0)), 1e-9)
        assertEquals(75.0, WeightUnit.Kg.fromKg(75.0))
        assertEquals(WeightUnit.Kg, WeightUnit.fromCode("nonsense"))
        assertEquals(WeightUnit.Lb, WeightUnit.fromCode("lb"))
    }

    @Test fun theWeightUnitIsASettingAndOldFilesAreKg() {
        assertEquals("kg", Settings().weightUnit)
    }
}
