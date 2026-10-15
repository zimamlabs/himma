package dev.himma.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import dev.himma.core.Medicine
import kotlinx.datetime.minus
import dev.himma.core.Vault
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertTrue

/** A phone whose reminders are blocked, to see the warnings. */
private class BlockedPhone(override val notificationsOn: Boolean = false, override val exactAlarmsOn: Boolean = false) : PlatformActions {
    override val folderPath = "/v"
}

/**
 * Draws screens offscreen into build/screenshots so the layout can be looked at without a device.
 * It also proves each screen composes with realistic data in English and Arabic without throwing.
 */
@OptIn(ExperimentalComposeUiApi::class)
class ScreenshotTest {
    private fun store(platform: PlatformActions = NoPlatform("/v")): HimmaStore {
        val vault = Vault(FakeFileSystem(), "/v".toPath())
        vault.saveMedicines(listOf(Medicine("a", "Vitamin D", "1 tablet", listOf("08:00", "20:00")), Medicine("b", "Iron", "", listOf("09:00"), setOf(1, 3))))
        return HimmaStore(vault, platform)
    }

    private fun shot(name: String, lang: String = "en", dark: Boolean = true, content: @Composable () -> Unit) {
        val old = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag(lang))
        UiText.rtl = lang == "ar"
        val scene = ImageComposeScene(412 * 2, 860 * 2, Density(2f)) {
            CompositionLocalProvider(LocalLayoutDirection provides if (lang == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr) {
                HimmaTheme(if (dark) "dark" else "light") { androidx.compose.material3.Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { content() } }
            }
        }
        try {
            scene.render(0)
            val data = scene.render(100_000_000).encodeToData(EncodedImageFormat.PNG)!!
            File("build/screenshots").apply { mkdirs() }.resolve("$name.png").writeBytes(data.bytes)
            assertTrue(data.bytes.size > 1_000)
        } finally {
            scene.close(); Locale.setDefault(old); UiText.rtl = null
        }
    }

    @Test fun medicineTabWithBlockedReminders() {
        val s = store(BlockedPhone())
        assertTrue(s.reminderProblems.size == 2)
        shot("medicine-blocked-en") { MedicineTab(s) {} }
        shot("medicine-blocked-light-en", dark = false) { MedicineTab(s) {} }
        shot("medicine-blocked-ar", "ar") { MedicineTab(s) {} }
    }

    @Test fun medicineTabWhenAllIsWell() {
        val s = store()
        assertTrue(s.reminderProblems.isEmpty())
        shot("medicine-ok-en") { MedicineTab(s) {} }
    }

    @Test fun confirmDeleteAndAbout() {
        shot("confirm-delete-en") { ConfirmDelete("Delete this medicine?", "Vitamin D and its reminders will be removed. This cannot be undone.", {}, {}) }
        shot("confirm-delete-ar", "ar") { ConfirmDelete("حذف هذا الدواء؟", "سيُحذف Vitamin D وتذكيراته. لا يمكن التراجع عن ذلك.", {}, {}) }
        shot("about-en") { AboutScreen(store(), {}, {}) }
        shot("about-ar", "ar") { AboutScreen(store(), {}, {}) }
    }

    @Test fun unreadableFileCard() {
        val problems = listOf(dev.himma.core.DataProblem("medicines/medicines.json", "medicines.json.corrupt-20261015-101500"))
        shot("data-problem-en") { DataProblemsCard(problems) {} }
        shot("data-problem-ar", "ar") { DataProblemsCard(problems) {} }
    }

    @Test fun homeAndWeightWithUnits() {
        val s = store()
        s.addWater(1250)
        s.saveSettings(s.settings.copy(weightUnit = "lb"))
        s.saveWeight(80.0, s.today.minus(kotlinx.datetime.DatePeriod(days = 7))); s.saveWeight(78.5)
        val nav = Nav(go = { _, _ -> }, logMeal = {}, startWorkout = {})
        shot("home-en") { HomeTab(s, nav) }
        shot("home-ar", "ar") { HomeTab(s, nav) }
        shot("weight-lb-en") { WeightTab(s) }
        shot("weight-lb-ar", "ar") { WeightTab(s) }
    }
}
