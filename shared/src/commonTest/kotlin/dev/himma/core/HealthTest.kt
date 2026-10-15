package dev.himma.core

import kotlinx.datetime.LocalDate
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HealthTest {
    private fun vault() = Vault(FakeFileSystem(), "/h".toPath())
    private val day = LocalDate(2026, 10, 14)

    @Test fun dayTotalsSumMealsAndTreatMissingMacrosAsZero() {
        val t = dayTotals(listOf(Meal("a", "08:00", "Eggs", 300, 20, 1, 22), Meal("b", "13:00", "Rice", 500)))
        assertEquals(DayTotals(800, 20, 1, 22), t)
        assertEquals(1200, caloriesLeft(2000, t))
        assertEquals(-100, caloriesLeft(700, t))
        assertEquals(DayTotals(0, 0, 0, 0), dayTotals(emptyList()))
    }

    @Test fun waterProgressStopsAtFullAndUndoNeverGoesBelowZero() {
        assertEquals(0.5f, waterProgress(1000, 2000))
        assertEquals(1f, waterProgress(2750, 2000))
        assertEquals(0f, waterProgress(500, 0))
        assertEquals(750, addWater(500, 250))
        assertEquals(0, addWater(100, -250))
    }

    @Test fun fastingWindowInsideTheDay() {
        // 16:8 with eating from 12:00 to 20:00.
        assertEquals(FastState(true, 8 * 60), fastState(12 * 60, 16, 12 * 60))
        assertEquals(FastState(true, 60), fastState(12 * 60, 16, 19 * 60))
        assertEquals(FastState(false, 16 * 60), fastState(12 * 60, 16, 20 * 60))
        assertEquals(FastState(false, 60), fastState(12 * 60, 16, 11 * 60))
        assertEquals(FastState(false, 12 * 60), fastState(12 * 60, 16, 0))
    }

    @Test fun fastingWindowCrossingMidnight() {
        // Eating from 20:00 to 04:00 the next day (16:8).
        assertEquals(FastState(true, 30), fastState(20 * 60, 16, 3 * 60 + 30))
        assertEquals(FastState(true, 4 * 60), fastState(20 * 60, 16, 0))
        assertEquals(FastState(false, 16 * 60), fastState(20 * 60, 16, 4 * 60))
        assertEquals(FastState(false, 60), fastState(20 * 60, 16, 19 * 60))
        // 20:4 starting 22:00 ends 02:00.
        assertTrue(fastState(22 * 60, 20, 1 * 60).eating)
        assertFalse(fastState(22 * 60, 20, 2 * 60).eating)
    }

    @Test fun clockTextRoundTrips() {
        assertEquals("09:05", formatClock(9 * 60 + 5))
        assertEquals(9 * 60 + 5, parseClock("9:05"))
        assertEquals(0, parseClock("00:00"))
        assertNull(parseClock("24:00"))
        assertNull(parseClock("12:60"))
        assertNull(parseClock("noon"))
    }

    @Test fun mealsRoundTripThroughCsvIncludingCommasQuotesAndMissingMacros() {
        val v = vault()
        v.saveMeal(day, Meal("", "13:10", "Rice, \"extra\" chicken", 650, 40, null, 12))
        v.saveMeal(day, Meal("", "08:00", "Eggs", 300))
        val list = v.meals(day)
        assertEquals(listOf("Eggs", "Rice, \"extra\" chicken"), list.map { it.name })
        val rice = list.last()
        assertEquals(Meal(rice.id, "13:10", "Rice, \"extra\" chicken", 650, 40, null, 12), rice)
        assertEquals(8, rice.id.length)
        assertTrue(v.fs.exists("/h/meals/2026/2026-10-14.csv".toPath()))
    }

    @Test fun editingAndDeletingMeals() {
        val v = vault()
        val m = v.saveMeal(day, Meal("", "08:00", "Eggs", 300))
        v.saveMeal(day, m.copy(kcal = 350))
        assertEquals(350, v.meals(day).single().kcal)
        v.deleteMeal(day, m.id)
        assertTrue(v.meals(day).isEmpty())
        assertFalse(v.fs.exists("/h/meals/2026/2026-10-14.csv".toPath()))
        assertTrue(v.meals(LocalDate(2026, 1, 1)).isEmpty())
    }

    @Test fun waterRoundTripPerDayAndYear() {
        val v = vault()
        assertEquals(0, v.water(day))
        v.saveWater(day, 750)
        v.saveWater(LocalDate(2026, 10, 15), 250)
        v.saveWater(LocalDate(2025, 12, 31), 2000)
        assertEquals(750, v.water(day))
        assertEquals(250, v.water(LocalDate(2026, 10, 15)))
        assertEquals(2000, v.water(LocalDate(2025, 12, 31)))
        v.saveWater(day, 0)
        assertEquals(0, v.water(day))
        assertEquals(250, v.water(LocalDate(2026, 10, 15)))
    }

    @Test fun settingsRoundTripAndDefaultsWhenMissingOrBroken() {
        val v = vault()
        assertEquals(Settings(), v.settings())
        val s = Settings(calorieGoal = 2400, waterGoalMl = 3000, fastHours = 18, eatStartMinutes = 20 * 60)
        v.saveSettings(s)
        assertEquals(s, v.settings())
        v.fs.write("/h/.himma/settings.json".toPath()) { writeUtf8("{ not json") }
        assertEquals(Settings(), v.settings())
    }

    @Test fun medicineNamesStayOffTheLockScreenByDefaultAndForOldFiles() {
        assertEquals(false, Settings().lockScreenNames)
        val v = vault()
        v.fs.createDirectories("/h/.himma".toPath())
        v.fs.write("/h/.himma/settings.json".toPath()) { writeUtf8("""{ "calorieGoal": 2100 }""") }
        assertEquals(2100, v.settings().calorieGoal)
        assertEquals(false, v.settings().lockScreenNames, "a file written by an older version")
        v.saveSettings(Settings(lockScreenNames = true))
        assertEquals(true, v.settings().lockScreenNames)
    }
}
