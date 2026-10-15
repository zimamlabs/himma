package dev.himma.core

import kotlinx.datetime.LocalDate
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExportTest {
    private val fs = FakeFileSystem()
    private val v = Vault(fs, "/h".toPath())
    private val day = LocalDate(2026, 10, 14)

    @Test fun crcMatchesTheKnownCheckValue() {
        assertEquals(0xCBF43926.toInt(), Zip.crc32("123456789".encodeToByteArray()))
    }

    @Test fun exportListsOnlyHimmaFilesWithRelativeNames() {
        v.saveMeal(day, Meal("", "08:00", "Eggs", 300))
        v.saveWater(day, 500)
        v.saveSettings(Settings())
        fs.write("/h/notes-from-me.txt".toPath()) { writeUtf8("not mine") }
        val names = v.exportEntries().map { it.first }
        assertTrue("meals/2026/2026-10-14.csv" in names)
        assertTrue("water/2026.csv" in names)
        assertTrue(".himma/settings.json" in names)
        assertTrue("notes-from-me.txt" !in names)
    }

    @Test fun deleteAllRemovesHimmaFilesAndLeavesOtherFiles() {
        v.saveMeal(day, Meal("", "08:00", "Eggs", 300))
        v.saveMedicines(listOf(Medicine("a", "A", times = listOf("08:00"))))
        fs.write("/h/notes-from-me.txt".toPath()) { writeUtf8("not mine") }
        v.deleteAllData()
        assertTrue(v.exportEntries().isEmpty())
        assertEquals(emptyList(), v.medicines())
        assertTrue(fs.exists("/h/notes-from-me.txt".toPath()))
    }
}
