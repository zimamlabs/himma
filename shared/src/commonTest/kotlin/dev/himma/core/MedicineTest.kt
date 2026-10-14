package dev.himma.core

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MedicineTest {
    // 2026-10-12 is a Monday.
    private val mon = LocalDate(2026, 10, 12)
    private val daily = Medicine("d", "Vitamin D", "1 tablet", listOf("20:00", "08:00"))
    private val monWed = Medicine("m", "Iron", "", listOf("09:00"), setOf(1, 3))
    private fun at(date: LocalDate, h: Int, m: Int = 0) = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, h, m)

    @Test fun dosesOnFollowTheWeekdaysAndComeEarliestFirst() {
        assertEquals(listOf("08:00", "09:00", "20:00"), dosesOn(listOf(daily, monWed), mon).map { it.time })
        val tue = LocalDate(2026, 10, 13)
        assertEquals(listOf("08:00", "20:00"), dosesOn(listOf(daily, monWed), tue).map { it.time })
        assertEquals(listOf("09:00"), dosesOn(listOf(monWed), LocalDate(2026, 10, 14)).map { it.time }, "Wednesday is 3")
        assertTrue(dosesOn(listOf(monWed), LocalDate(2026, 10, 18)).isEmpty(), "Sunday is 7")
    }

    @Test fun badTimesAreIgnored() {
        assertEquals(listOf("08:00"), dosesOn(listOf(Medicine("x", "X", times = listOf("8am", "08:00", "25:00"))), mon).map { it.time })
    }

    @Test fun nextDoseIsStrictlyAfterNowAndCrossesMidnight() {
        assertEquals(at(mon, 20), nextDoseTime(listOf(daily), at(mon, 8)))
        assertEquals(at(mon, 8, 0).let { at(LocalDate(2026, 10, 13), 8) }, nextDoseTime(listOf(daily), at(mon, 20)), "after the last dose of the day it is tomorrow morning")
        assertEquals(at(mon, 8), nextDoseTime(listOf(daily), at(LocalDate(2026, 10, 11), 23, 59)), "just before midnight")
    }

    @Test fun nextDoseSkipsDaysWithNoDoseAndGivesUpAfterAWeek() {
        assertEquals(at(LocalDate(2026, 10, 19), 9), nextDoseTime(listOf(monWed), at(LocalDate(2026, 10, 15), 12)), "Thursday to next Monday")
        assertNull(nextDoseTime(emptyList(), at(mon, 8)))
        assertNull(nextDoseTime(listOf(Medicine("z", "Z", times = emptyList())), at(mon, 8)))
    }

    @Test fun dosesBetweenCoversAWindowAcrossMidnightWithOpenStartAndClosedEnd() {
        val night = Medicine("n", "Night", times = listOf("23:30", "00:15"))
        val got = dosesBetween(listOf(night), at(mon, 23, 30), at(LocalDate(2026, 10, 13), 0, 15))
        assertEquals(listOf(at(LocalDate(2026, 10, 13), 0, 15)), got.map { it.at }, "the start is excluded, the end is included")
        assertTrue(dosesBetween(listOf(night), at(mon, 12), at(mon, 12)).isEmpty())
        assertTrue(dosesBetween(listOf(night), at(mon, 13), at(mon, 12)).isEmpty())
    }

    @Test fun statusIsMarkedUpcomingOrMissed() {
        val dose = dosesOn(listOf(daily), mon).first()
        assertEquals(DoseStatus.Upcoming, doseStatus(dose, emptyMap(), at(mon, 7, 59)))
        assertEquals(DoseStatus.Missed, doseStatus(dose, emptyMap(), at(mon, 8, 1)))
        assertEquals(DoseStatus.Taken, doseStatus(dose, mapOf(dose.key to DoseMark.Taken), at(mon, 7)))
        assertEquals(DoseStatus.Skipped, doseStatus(dose, mapOf(dose.key to DoseMark.Skipped), at(mon, 9)))
    }

    @Test fun adherenceCountsOnlyDueDosesAndGivesNoRateWhenNothingIsDue() {
        val doses = dosesOn(listOf(daily), mon)
        val marks = mapOf(doses[0].key to DoseMark.Taken, doses[1].key to DoseMark.Skipped)
        val a = adherence(listOf(daily), marks, mon, mon, at(mon, 21))
        assertEquals(Adherence(1, 1, 0), a)
        assertEquals(0.5f, a.rate)
        assertEquals(Adherence(1, 0, 0), adherence(listOf(daily), mapOf(doses[0].key to DoseMark.Taken), mon, mon, at(mon, 12)), "the evening dose is not due yet")
        assertNull(adherence(listOf(daily), emptyMap(), mon, mon, at(mon, 7)).rate)
        assertEquals(Adherence(0, 0, 2), adherence(listOf(daily), emptyMap(), mon, mon, at(mon, 23)))
    }

    @Test fun timesTextParsesSortsAndRejectsBadInput() {
        assertEquals(listOf("08:00", "20:30"), parseTimes("20:30, 8:00, 08:00"))
        assertNull(parseTimes("08:00, soon"))
        assertNull(parseTimes("  "))
        assertEquals("Every day", formatDays(emptySet()))
        assertEquals("Mon, Wed", formatDays(setOf(3, 1)))
    }

    @Test fun medicinesAndMarksSurviveSavingAndReloading() {
        val fs = FakeFileSystem()
        val v = Vault(fs, "/h".toPath())
        assertTrue(v.medicines().isEmpty())
        v.saveMedicines(listOf(daily, monWed))
        assertEquals(listOf(daily, monWed), Vault(fs, "/h".toPath()).medicines())
        val dose = dosesOn(listOf(daily), mon).first()
        v.setDoseMark(dose, DoseMark.Taken)
        assertEquals(mapOf(dose.key to DoseMark.Taken), Vault(fs, "/h".toPath()).doseMarks())
        v.setDoseMark(dose, DoseMark.Skipped)
        assertEquals(DoseMark.Skipped, v.doseMarks()[dose.key])
        v.setDoseMark(dose, null)
        assertTrue(v.doseMarks().isEmpty())
    }

    @Test fun hasDataIsFalseForAnEmptyFolderAndTrueOnceAnythingIsSaved() {
        val fs = FakeFileSystem()
        val v = Vault(fs, "/h".toPath())
        assertEquals(false, v.hasData())
        v.saveSettings(Settings())
        assertEquals(true, v.hasData())
    }
}
