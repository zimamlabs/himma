package dev.himma.core

import okio.FileSystem
import okio.ForwardingFileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MigrateTest {
    private val from = "/private/Himma".toPath()
    private val to = "/phone/Zimam/Himma".toPath()

    private fun FileSystem.put(path: Path, text: String) { path.parent?.let { createDirectories(it) }; write(path) { writeUtf8(text) } }
    private fun FileSystem.text(path: Path) = read(path) { readUtf8() }
    private fun seeded(): FakeFileSystem = FakeFileSystem().also {
        it.put(from / "meals/2026/2026-10-14.csv", "id,time\na,08:00\n")
        it.put(from / "water/2026.csv", "date,ml\n2026-10-14,750\n")
        it.put(from / ".himma/settings.json", "{}")
    }

    @Test fun copiesEverythingVerifiesWritesMarkerAndKeepsTheOldFolder() {
        val fs = seeded()
        assertEquals(Migration.Done(3, 0), migrateFolder(fs, from, to))
        assertEquals("id,time\na,08:00\n", fs.text(to / "meals/2026/2026-10-14.csv"))
        assertTrue(fs.exists(from / "water/2026.csv"), "the old copy is never deleted by the move")
        assertTrue(isMigrated(fs, from, to))
    }

    @Test fun runningTwiceChangesNothingAndMakesNoDuplicates() {
        val fs = seeded()
        migrateFolder(fs, from, to)
        assertEquals(Migration.Done(0, 0), migrateFolder(fs, from, to))
        assertEquals(setOf("2026-10-14.csv"), fs.list(to / "meals/2026").map { it.name }.toSet())
    }

    @Test fun aDifferentFileWithTheSameNameIsNeverOverwrittenBothAreKept() {
        val fs = seeded()
        fs.put(to / "water/2026.csv", "date,ml\n2026-10-14,2000\n")
        assertEquals(Migration.Done(3, 1), migrateFolder(fs, from, to))
        assertEquals("date,ml\n2026-10-14,2000\n", fs.text(to / "water/2026.csv"))
        assertEquals("date,ml\n2026-10-14,750\n", fs.text(to / "water/2026 (old copy).csv"))
        assertEquals(Migration.Done(0, 0), migrateFolder(fs, from, to), "the kept copy is recognised the second time")
    }

    @Test fun anIdenticalFileAlreadyThereIsLeftAlone() {
        val fs = seeded()
        fs.put(to / "water/2026.csv", "date,ml\n2026-10-14,750\n")
        assertEquals(Migration.Done(2, 0), migrateFolder(fs, from, to))
        assertFalse(fs.exists(to / "water/2026 (old copy).csv"))
    }

    @Test fun aCopyThatStopsHalfWayFailsLeavesTheOldDataAndFinishesOnTheNextRun() {
        val base = seeded()
        var copies = 0
        val flaky = object : ForwardingFileSystem(base) {
            override fun copy(source: Path, target: Path) { if (++copies == 2) throw java.io.IOException("disk gone"); super.copy(source, target) }
        }
        assertTrue(migrateFolder(flaky, from, to) is Migration.Failed)
        assertFalse(isMigrated(base, from, to), "no marker, so the old folder stays in use")
        assertTrue(base.exists(from / "meals/2026/2026-10-14.csv"))
        assertEquals(Migration.Done(2, 0), migrateFolder(base, from, to), "the file copied before the stop is not copied again")
        assertTrue(isMigrated(base, from, to))
    }

    @Test fun aLeftoverPartialTempFileFromACrashIsIgnoredAndReplaced() {
        val fs = seeded()
        fs.put(to / "water/2026.csv.part", "garbage")
        assertTrue(migrateFolder(fs, from, to) is Migration.Done)
        assertEquals("date,ml\n2026-10-14,750\n", fs.text(to / "water/2026.csv"))
    }

    @Test fun aCopyThatDoesNotMatchTheOriginalFailsAndWritesNoMarker() {
        val base = seeded()
        val corrupting = object : ForwardingFileSystem(base) {
            override fun copy(source: Path, target: Path) { super.copy(source, target); if (target.name.startsWith("2026.csv")) base.put(target, "date,ml\nXXXXXXXXXX,750\n") }
        }
        val result = migrateFolder(corrupting, from, to)
        assertTrue(result is Migration.Failed)
        assertFalse(isMigrated(base, from, to))
        assertTrue(base.exists(from / "water/2026.csv"))
    }

    @Test fun theMarkerIsOnlyWrittenForAFinishedCopy() {
        val fs = seeded()
        assertFalse(isMigrated(fs, from, to))
        migrateFolder(fs, from, to)
        assertTrue(isMigrated(fs, from, to))
        assertFalse(isMigrated(fs, "/elsewhere".toPath(), to))
    }

    @Test fun verifyCatchesAMissingChangedOrShorterFile() {
        val fs = seeded()
        migrateFolder(fs, from, to)
        assertTrue(verifyCopy(fs, from, to))
        fs.put(to / "water/2026.csv", "date,ml\n2026-10-14,751\n")
        assertFalse(verifyCopy(fs, from, to), "same length, different bytes")
        fs.delete(to / "meals/2026/2026-10-14.csv")
        assertFalse(verifyCopy(fs, from, to), "missing file")
    }

    @Test fun nothingHappensWithoutAnOldFolderOrWhenBothAreTheSame() {
        val fs = FakeFileSystem()
        assertEquals(Migration.Nothing, migrateFolder(fs, from, to))
        assertEquals(Migration.Nothing, migrateFolder(seeded(), from, from))
    }

    @Test fun theOldCopyIsRemovedOnlyWhenNothingWouldBeLost() {
        val fs = seeded()
        assertFalse(removeOldCopy(fs, from, to), "not copied yet")
        migrateFolder(fs, from, to)
        assertTrue(canRemoveOldCopy(fs, from, to))
        assertTrue(removeOldCopy(fs, from, to))
        assertFalse(fs.exists(from))
        assertTrue(fs.exists(to / "meals/2026/2026-10-14.csv"))
    }

    @Test fun newerDataInTheNewFolderDoesNotBlockRemovalButAMissingFileDoes() {
        val fs = seeded()
        migrateFolder(fs, from, to)
        fs.put(to / "water/2026.csv", "date,ml\n2026-10-14,1500\n")
        assertTrue(canRemoveOldCopy(fs, from, to), "edited since the copy is newer data, the old file is still covered")
        fs.delete(to / "meals/2026/2026-10-14.csv")
        assertFalse(canRemoveOldCopy(fs, from, to), "a file that is gone from the new folder must stop the removal")
        assertTrue(fs.exists(from))
    }

    @Test fun anOldFolderThatChangedAfterTheCopyIsNeverRemoved() {
        val fs = seeded()
        migrateFolder(fs, from, to)
        fs.put(from / "water/2026.csv", "date,ml\n2026-10-14,999\n")
        assertFalse(canRemoveOldCopy(fs, from, to), "changed")
        fs.put(from / "water/2026.csv", "date,ml\n2026-10-14,750\n")
        assertTrue(canRemoveOldCopy(fs, from, to))
        fs.put(from / "meals/extra.csv", "x")
        assertFalse(canRemoveOldCopy(fs, from, to), "a new file in the old folder was never copied")
        assertTrue(fs.exists(from / "meals/extra.csv"))
    }

    @Test fun clashCopiesAreCoveredWhenCheckingRemoval() {
        val fs = seeded()
        fs.put(to / "water/2026.csv", "other")
        migrateFolder(fs, from, to)
        assertTrue(canRemoveOldCopy(fs, from, to))
        fs.delete(to / "water/2026 (old copy).csv")
        assertFalse(canRemoveOldCopy(fs, from, to))
    }
}
