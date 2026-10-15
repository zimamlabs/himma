package dev.himma.core

import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsRepositoryTest {
    private val fs = FakeFileSystem()
    private val vault = Vault(fs, "/h".toPath())
    private fun put(text: String) { fs.createDirectories("/h/.himma".toPath()); fs.write("/h/.himma/settings.json".toPath()) { writeUtf8(text) } }

    @Test fun defaultsWhenThereIsNoFile() {
        val s = SettingsRepository(vault).current
        assertEquals(Settings(), s)
        assertEquals("zimam", s.palette); assertEquals(1, s.textSize); assertEquals("home", s.startSection)
    }

    @Test fun anUpdateIsSavedAndReadBackByANewRepository() {
        val repo = SettingsRepository(vault)
        repo.update { it.copy(palette = "ocean", textSize = 3, weightUnit = "lb", hiddenSections = setOf("gym"), sectionOrder = listOf("track", "meals")) }
        val again = SettingsRepository(Vault(fs, "/h".toPath())).current
        assertEquals("ocean", again.palette); assertEquals(3, again.textSize); assertEquals("lb", again.weightUnit)
        assertEquals(setOf("gym"), again.hiddenSections); assertEquals(listOf("track", "meals"), again.sectionOrder)
    }

    @Test fun aFileFromAnOlderVersionGetsTheNewDefaults() {
        put("""{ "calorieGoal": 2300, "waterGoalMl": 3000, "themeMode": "dark", "tourDone": true }""")
        val s = SettingsRepository(vault).current
        assertEquals(2300, s.calorieGoal); assertEquals("dark", s.themeMode); assertTrue(s.tourDone)
        assertEquals(Palettes.DEFAULT, s.palette); assertEquals(1, s.textSize); assertTrue(s.hiddenSections.isEmpty())
    }

    @Test fun valuesOutOfRangeAreBroughtBack() {
        put("""{ "calorieGoal": -5, "waterGoalMl": 99999999, "stepGoal": 3, "fastHours": 40, "eatStartMinutes": 5000, "themeMode": "neon",
            "palette": "nope", "textSize": 9, "weightUnit": "stone", "startSection": "settings", "hiddenSections": ["home", "gym", "ghost"], "sectionOrder": ["x", "gym", "gym"] }""")
        val s = SettingsRepository(vault).current
        assertEquals(1, s.calorieGoal); assertEquals(20000, s.waterGoalMl); assertEquals(100, s.stepGoal)
        assertEquals(23, s.fastHours); assertEquals(1439, s.eatStartMinutes); assertEquals("system", s.themeMode)
        assertEquals("zimam", s.palette); assertEquals(3, s.textSize); assertEquals("kg", s.weightUnit)
        assertEquals("home", s.startSection); assertEquals(setOf("gym"), s.hiddenSections); assertEquals(listOf("gym"), s.sectionOrder)
    }

    @Test fun theStartSectionCannotBeAHiddenOne() {
        val s = SettingsRepository(vault).update { it.copy(startSection = "gym", hiddenSections = setOf("gym")) }
        assertEquals("home", s.startSection)
    }

    @Test fun visibleSectionsFollowOrderAndHiddenAndKeepHome() {
        assertEquals(listOf("home", "meals", "gym", "track", "medicine"), Sections.visible(emptyList(), emptySet()))
        assertEquals(listOf("track", "home", "meals", "gym", "medicine"), Sections.visible(listOf("track"), emptySet()))
        assertEquals(listOf("home", "meals", "medicine"), Sections.visible(emptyList(), setOf("gym", "track")))
        assertEquals(listOf("home", "meals", "gym", "track", "medicine"), Sections.visible(emptyList(), emptySet()).let { Sections.visible(it, setOf()) })
        assertTrue("home" in Sections.visible(emptyList(), setOf("home")))
    }

    @Test fun aBrokenFileGivesDefaultsAndKeepsACopy() {
        put("{ nope")
        val repo = SettingsRepository(vault)
        assertEquals(Settings(), repo.current)
        assertEquals(1, vault.problems().size)
    }
}
