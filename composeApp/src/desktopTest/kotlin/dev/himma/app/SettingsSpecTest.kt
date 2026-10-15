package dev.himma.app

import kotlin.test.Test
import kotlin.test.assertEquals

/** The Settings list must keep the order fixed in the shared UI spec (ui-spec-shared.md). */
class SettingsSpecTest {
    @Test fun rowsFollowTheSharedSpecOrder() {
        assertEquals(listOf("Appearance", "Sections", "Language", "Notifications", "Units", "Data", "Privacy", "About"), SettingsPage.entries.map { it.name })
    }
}
