package dev.himma.core

import kotlinx.datetime.LocalDate
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** H-01: a file that cannot be read is never treated as empty and then overwritten without a copy. */
class CorruptFilesTest {
    private val fs = FakeFileSystem()
    private val v = Vault(fs, "/h".toPath())
    private val day = LocalDate(2026, 10, 14)
    private fun put(path: String, text: String) { val p = path.toPath(); fs.createDirectories(p.parent!!); fs.write(p) { writeUtf8(text) } }
    private fun copies(dir: String) = (fs.listOrNull(dir.toPath()).orEmpty()).filter { ".corrupt-" in it.name }
    private fun text(p: okio.Path) = fs.read(p) { readUtf8() }

    @Test fun brokenMedicinesJsonIsKeptAsACopyAndReported() {
        val bad = """[{"id":"a","name":"Iron","times":["08:00"],}"""
        put("/h/medicines/medicines.json", bad)
        assertEquals(emptyList(), v.medicines())
        val kept = copies("/h/medicines")
        assertEquals(1, kept.size)
        assertEquals(bad, text(kept.single()))
        assertEquals(listOf("medicines/medicines.json"), v.problems().map { it.file })
    }

    @Test fun addingAMedicineAfterwardsDoesNotDestroyTheKeptCopy() {
        val bad = """[{"id":"a","name":"Iron","times":["08:00"],}"""
        put("/h/medicines/medicines.json", bad)
        v.saveMedicines(v.medicines() + Medicine("b", "New", times = listOf("09:00")))
        assertEquals(listOf("b"), v.medicines().map { it.id })
        assertEquals(bad, text(copies("/h/medicines").single()))
    }

    @Test fun readingAgainDoesNotPileUpCopies() {
        put("/h/medicines/medicines.json", "{ nope")
        repeat(3) { v.medicines() }
        assertEquals(1, copies("/h/medicines").size)
    }

    @Test fun aMealRowThatCannotBeParsedKeepsTheWholeFile() {
        val csv = "id,time,name,kcal,protein,carbs,fat\na,08:00,Eggs,300,,,\nb,12:00,Rice,many,,,\n"
        put("/h/meals/2026/$day.csv", csv)
        assertEquals(listOf("a"), v.meals(day).map { it.id })
        assertEquals(csv, text(copies("/h/meals/2026").single()))
        assertTrue(v.problems().single().file.endsWith("$day.csv"))
    }

    @Test fun brokenSettingsAreKeptToo() {
        put("/h/.himma/settings.json", "{ not json")
        assertEquals(Settings(), v.settings())
        assertEquals("{ not json", text(copies("/h/.himma").single()))
    }

    @Test fun goodAndMissingFilesReportNothing() {
        assertEquals(emptyList(), v.medicines())
        v.saveMedicines(listOf(Medicine("a", "A", times = listOf("08:00"))))
        v.medicines(); v.routines(); v.templates(); v.weights(); v.meals(day)
        assertTrue(v.problems().isEmpty())
    }

    @Test fun clearingForgetsTheProblemsButNotTheCopies() {
        put("/h/gym/routines.json", "oops")
        v.routines()
        assertEquals(1, v.problems().size)
        v.clearProblems()
        assertTrue(v.problems().isEmpty())
        assertEquals(1, copies("/h/gym").size)
    }
}
