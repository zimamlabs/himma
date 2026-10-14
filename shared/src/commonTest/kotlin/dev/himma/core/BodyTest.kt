package dev.himma.core

import kotlinx.datetime.LocalDate
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GymTest {
    private fun w(date: String, vararg s: SetEntry) = Workout("id$date", date, "Push", s.toList())

    @Test fun volumeIsRepsTimesWeightSummed() {
        assertEquals(10 * 40.0 + 8 * 45.0, volume(listOf(SetEntry("Bench", 10, 40.0), SetEntry("Bench", 8, 45.0))))
        assertEquals(0.0, volume(emptyList()))
    }

    @Test fun oneRepMaxUsesEpleyAndASingleIsTheWeight() {
        assertEquals(100.0, oneRepMax(100.0, 1))
        assertEquals(100.0 * (1 + 5 / 30.0), oneRepMax(100.0, 5), 1e-9)
    }

    @Test fun personalBestsTrackEachExerciseCaseInsensitively() {
        val bests = personalBests(listOf(
            w("2026-10-01", SetEntry("Bench", 5, 60.0), SetEntry("squat", 5, 80.0)),
            w("2026-10-08", SetEntry("bench ", 3, 70.0), SetEntry("Bench", 10, 50.0)),
        ))
        val bench = assertNotNull(bests["bench"])
        assertEquals(70.0, bench.maxWeightKg)
        assertEquals(500.0, bench.maxSetVolume)
        assertEquals("2026-10-08", bench.date)
        assertEquals(80.0, bests["squat"]!!.maxWeightKg)
    }

    @Test fun beatsBestOnlyWhenHeavierOrBetterEstimate() {
        val bests = personalBests(listOf(w("2026-10-01", SetEntry("Bench", 5, 60.0))))
        assertTrue(beatsBest(bests, SetEntry("Bench", 1, 65.0)))
        assertTrue(beatsBest(bests, SetEntry("Bench", 8, 60.0)))
        assertFalse(beatsBest(bests, SetEntry("Bench", 3, 55.0)))
        assertTrue(beatsBest(bests, SetEntry("Row", 5, 30.0)))
        assertFalse(beatsBest(bests, SetEntry("Row", 0, 30.0)))
    }

    @Test fun plannedSetsExpandEachExercise() {
        val r = Routine("r", "Push", listOf(Exercise("Bench", 3, 8, 50.0), Exercise("Dip", 2, 12)))
        val sets = plannedSets(r)
        assertEquals(5, sets.size)
        assertEquals(SetEntry("Dip", 12, 0.0), sets.last())
    }

    @Test fun formatKgDropsTheZeroDecimal() {
        assertEquals("60 kg", formatKg(60.0)); assertEquals("62.5 kg", formatKg(62.5))
    }
}

class StepsTest {
    private val d1 = LocalDate(2026, 10, 14)
    private val d2 = LocalDate(2026, 10, 15)

    @Test fun firstReadingOfADayIsZeroSteps() {
        assertEquals(0, stepUpdate(StepState(), d1, 5000).steps)
    }

    @Test fun stepsGrowWithTheCounter() {
        val s = stepUpdate(stepUpdate(StepState(), d1, 5000), d1, 5800)
        assertEquals(800, s.steps)
    }

    @Test fun aRestartKeepsTheStepsSoFarAndCountsOnFromZero() {
        var s = stepUpdate(StepState(), d1, 5000)
        s = stepUpdate(s, d1, 6000)
        s = stepUpdate(s, d1, 120)
        assertEquals(1120, s.steps)
        s = stepUpdate(s, d1, 400)
        assertEquals(1400, s.steps)
    }

    @Test fun aRestartTwiceInADayStillAdds() {
        var s = stepUpdate(StepState(), d1, 100)
        s = stepUpdate(s, d1, 600)
        s = stepUpdate(s, d1, 50)
        s = stepUpdate(s, d1, 300)
        s = stepUpdate(s, d1, 20)
        assertEquals(500 + 300 + 20, s.steps)
    }

    @Test fun aNewDayStartsFromTheNewReading() {
        val day1 = stepUpdate(stepUpdate(StepState(), d1, 1000), d1, 4000)
        assertEquals(3000, day1.steps)
        assertEquals(0, stepUpdate(day1, d2, 4500).steps)
    }

    @Test fun progressStopsAtFull() {
        assertEquals(0.5f, stepProgress(4000, 8000)); assertEquals(1f, stepProgress(20000, 8000)); assertEquals(0f, stepProgress(100, 0))
    }
}

class WeightTest {
    private val today = LocalDate(2026, 10, 14)
    private fun e(day: Int, kg: Double) = WeightEntry(LocalDate(2026, 10, day), kg)

    @Test fun noEntriesGivesNothing() {
        assertEquals(WeightTrend(null, null, null), weightTrend(emptyList(), today))
    }

    @Test fun oneEntryHasALatestButNoTrend() {
        val t = weightTrend(listOf(e(10, 80.0)), today)
        assertEquals(80.0, t.latestKg); assertNull(t.changeKg); assertNull(t.kgPerWeek)
    }

    @Test fun aSteadyDropIsANegativeSlopePerWeek() {
        val t = weightTrend(listOf(e(1, 82.0), e(8, 81.0), e(15, 80.0)).filter { it.date <= today }, today)
        assertEquals(-1.0, t.kgPerWeek!!, 1e-9)
        assertEquals(-1.0, t.changeKg!!, 1e-9)
    }

    @Test fun entriesOutsideTheWindowAreIgnored() {
        val t = weightTrend(listOf(WeightEntry(LocalDate(2026, 5, 1), 99.0), e(10, 80.0), e(12, 80.5)), today, 28)
        assertEquals(0.5, t.changeKg!!, 1e-9)
    }

    @Test fun movingAverageSmoothsFromTheStart() {
        val avg = movingAverage(listOf(e(1, 80.0), e(2, 82.0), e(3, 84.0)), 2)
        assertEquals(listOf(80.0, 81.0, 83.0), avg.map { it.kg })
    }
}

class SummaryTest {
    private val today = LocalDate(2026, 10, 14)

    @Test fun lastDaysEndsTodayOldestFirst() {
        val d = lastDays(today, 3)
        assertEquals(listOf(LocalDate(2026, 10, 12), LocalDate(2026, 10, 13), today), d)
    }

    @Test fun averagesSkipDaysWithNothingLogged() {
        val days = listOf(
            DayRecord(today, kcal = 2000, waterMl = 2500, steps = 8000, workouts = 1, volumeKg = 1500.0),
            DayRecord(LocalDate(2026, 10, 13), kcal = 1000, waterMl = 1000, steps = 4000, workouts = 0),
            DayRecord(LocalDate(2026, 10, 12)),
        )
        val s = weekSummary(days, 2000, -0.4)
        assertEquals(1500, s.avgKcal); assertEquals(1750, s.avgWaterMl); assertEquals(1, s.waterGoalDays)
        assertEquals(6000, s.avgSteps); assertEquals(1, s.workouts); assertEquals(1500, s.volumeKg); assertEquals(-0.4, s.weightChangeKg)
    }

    @Test fun anEmptyWeekIsAllZero() {
        val s = weekSummary(emptyList(), 2000, null)
        assertEquals(0, s.avgKcal); assertEquals(0, s.waterGoalDays); assertNull(s.weightChangeKg)
    }

    @Test fun recentMealsCountEachNameAndCaloriesOnce() {
        val m = listOf(Meal("1", "08:00", "Oats", 300), Meal("2", "07:00", "oats ", 300), Meal("3", "06:00", "Oats", 350), Meal("4", "05:00", "Egg", 80))
        assertEquals(listOf("1", "3", "4"), recentMeals(m, 10).map { it.id })
        assertEquals(2, recentMeals(m, 2).size)
    }
}

class BodyVaultTest {
    private val fs = FakeFileSystem()
    private val root = "/h".toPath()
    private fun vault() = Vault(fs, root)

    @Test fun weightsRoundTripOneADayAndRemove() {
        val v = vault(); val d = LocalDate(2026, 10, 14)
        v.saveWeight(d, 80.5); v.saveWeight(d, 80.0); v.saveWeight(LocalDate(2026, 10, 10), 81.0)
        assertEquals(listOf(81.0, 80.0), vault().weights().map { it.kg })
        v.saveWeight(d, 0.0)
        assertEquals(listOf(81.0), vault().weights().map { it.kg })
    }

    @Test fun workoutsAcrossYearsRoundTripAndDelete() {
        val v = vault()
        val a = v.saveWorkout(Workout("", "2025-12-30", "Legs", listOf(SetEntry("Squat", 5, 100.0))))
        v.saveWorkout(Workout("", "2026-01-02", "Push", listOf(SetEntry("Bench", 5, 60.0)), 45))
        assertEquals(listOf("2025-12-30", "2026-01-02"), vault().allWorkouts().map { it.date })
        v.deleteWorkout(a)
        assertEquals(listOf("2026-01-02"), vault().allWorkouts().map { it.date })
    }

    @Test fun routinesTemplatesAndStepsRoundTrip() {
        val v = vault(); val d = LocalDate(2026, 10, 14)
        v.saveRoutines(listOf(Routine("r", "Pull", listOf(Exercise("Row", 3, 8, 40.0)))))
        v.saveTemplates(listOf(MealTemplate("t", "Oats", 300, 10, 50, 5)))
        v.saveSteps(d, 7000); v.saveStepState(StepState("2026-10-14", 10, 20, 0))
        val w = vault()
        assertEquals("Pull", w.routines().single().name)
        assertEquals(300, w.templates().single().kcal)
        assertEquals(7000, w.steps(d)); assertEquals(0, w.steps(LocalDate(2026, 10, 13)))
        assertEquals(10, w.stepState().steps)
        w.saveSteps(d, 0)
        assertEquals(0, vault().steps(d))
    }

    @Test fun anEmptyFolderHasNoDataAndGymCountsAsData() {
        assertFalse(vault().hasData())
        vault().saveRoutines(emptyList())
        assertTrue(vault().hasData())
    }

    @Test fun oldSettingsFilesStillLoadWithTheNewFieldsDefaulted() {
        val v = vault()
        fs.createDirectories(root / ".himma")
        fs.write(root / ".himma" / "settings.json") { writeUtf8("""{"calorieGoal":1800}""") }
        val s = v.settings()
        assertEquals(1800, s.calorieGoal); assertEquals("system", s.themeMode); assertEquals(8000, s.stepGoal)
    }
}

class DayScoreTest {
    @Test fun averagesOnlyThePartsThatApply() {
        assertEquals(0.5f, dayScore(listOf(1f, 0f, null)))
        assertEquals(0f, dayScore(listOf(null, null)))
        assertEquals(1f, dayScore(listOf(2f, 1f)))
    }
}
