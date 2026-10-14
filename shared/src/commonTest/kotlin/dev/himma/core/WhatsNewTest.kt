package dev.himma.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WhatsNewTest {
    private val notes = listOf(ReleaseNote("0.1.0", listOf("a")), ReleaseNote("0.2.0", listOf("b")), ReleaseNote("0.10.0", listOf("c")))

    @Test fun nothingSavedAndNoDataIsAFreshInstall() {
        assertEquals(Startup.Fresh, startupFor(null, "0.2.0", hasData = false, notes = notes))
    }

    @Test fun nothingSavedButExistingDataCountsAsAnUpdateFromTheFirstRelease() {
        assertEquals(Startup.Updated(listOf(notes[1])), startupFor(null, "0.2.0", hasData = true, notes = notes))
    }

    @Test fun sameVersionShowsNothing() {
        assertEquals(Startup.Nothing, startupFor("0.2.0", "0.2.0", hasData = true, notes = notes))
    }

    @Test fun anUpdateShowsEveryNoteSinceTheLastSeenVersionNewestFirst() {
        val r = startupFor("0.1.0", "0.10.0", hasData = true, notes = notes) as Startup.Updated
        assertEquals(listOf("0.10.0", "0.2.0"), r.notes.map { it.version }, "0.10.0 is newer than 0.2.0")
    }

    @Test fun aDowngradeShowsNothing() {
        assertEquals(Startup.Nothing, startupFor("0.10.0", "0.2.0", hasData = true, notes = notes))
    }

    @Test fun anUpdateWithNoBundledNotesShowsNothing() {
        assertEquals(Startup.Nothing, startupFor("0.2.0", "0.2.1", hasData = true, notes = notes))
    }

    @Test fun versionsCompareByNumberNotByText() {
        assertTrue(compareVersions("0.10.0", "0.9.0") > 0)
        assertEquals(0, compareVersions("1.0", "1.0.0"))
        assertTrue(compareVersions("v0.2.0", "0.2.1") < 0)
    }

    @Test fun theBundledNotesCoverTheCurrentRelease() {
        assertTrue(releaseNotes.any { it.version == "0.2.0" && it.bullets.size in 2..4 })
    }

    @Test fun greetingFollowsTheHour() {
        assertEquals("Good morning", greeting(5)); assertEquals("Good afternoon", greeting(12))
        assertEquals("Good evening", greeting(18)); assertEquals("Good evening", greeting(2))
    }
}
