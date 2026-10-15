package dev.himma.app

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import dev.himma.core.Vault
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Drives the real shell with pointer events: the pager swipes between sections, in both reading directions, and the drawer opens. */
@OptIn(ExperimentalComposeUiApi::class)
class ShellTest {
    private val w = 412; private val h = 860

    private class Rig(val scene: ImageComposeScene, val pages: MutableList<Tab>, val drawer: DrawerState) {
        var now = 0L
        fun frames(n: Int = 30) { repeat(n) { now += 16_000_000; scene.render(now) } }
        fun drag(from: Offset, to: Offset, steps: Int = 12) {
            scene.sendPointerEvent(PointerEventType.Press, from, type = PointerType.Touch); frames(1)
            for (i in 1..steps) { scene.sendPointerEvent(PointerEventType.Move, Offset(from.x + (to.x - from.x) * i / steps, from.y + (to.y - from.y) * i / steps), type = PointerType.Touch); frames(1) }
            scene.sendPointerEvent(PointerEventType.Release, to, type = PointerType.Touch); frames(40)
        }
    }

    private fun rig(rtl: Boolean, test: (Rig) -> Unit) {
        val old = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag(if (rtl) "ar" else "en"))
        UiText.rtl = rtl
        val store = HimmaStore(Vault(FakeFileSystem(), "/v".toPath()), NoPlatform("/v"))
        val pages = mutableListOf<Tab>()
        val drawer = DrawerState(DrawerValue.Closed)
        val scene = ImageComposeScene(w * 2, h * 2, Density(2f)) {
            CompositionLocalProvider(LocalLayoutDirection provides if (rtl) LayoutDirection.Rtl else LayoutDirection.Ltr) {
                HimmaTheme("dark") { androidx.compose.material3.Surface { MainScreen(store, {}, {}, drawerState = drawer, onPageChanged = { pages += it }) } }
            }
        }
        try { Rig(scene, pages, drawer).also { it.frames(); test(it) } } finally { scene.close(); Locale.setDefault(old); UiText.rtl = null }
    }

    private val mid = h.toFloat() * 2 / 2

    @Test fun swipingTowardTheStartMovesToTheNextSectionInEnglish() = rig(rtl = false) { r ->
        assertEquals(Tab.Home, r.pages.last())
        r.drag(Offset(w * 2 * 0.8f, mid + 300), Offset(w * 2 * 0.2f, mid + 300))
        assertEquals(Tab.Meals, r.pages.last(), "left swipe goes to the next section (drawer ${r.drawer.currentValue} ${r.drawer.currentOffset}, pages ${r.pages})")
        r.drag(Offset(w * 2 * 0.2f, mid + 300), Offset(w * 2 * 0.8f, mid + 300))
        assertEquals(Tab.Home, r.pages.last(), "right swipe goes back")
    }

    @Test fun theSwipeMirrorsInArabic() = rig(rtl = true) { r ->
        assertEquals(Tab.Home, r.pages.last())
        r.drag(Offset(w * 2 * 0.2f, mid + 300), Offset(w * 2 * 0.8f, mid + 300))
        assertEquals(Tab.Meals, r.pages.last(), "right swipe goes to the next section in RTL")
    }

    @Test fun theDrawerOpensByAnEdgeSwipeOnTheFirstSection() = rig(rtl = false) { r ->
        r.drag(Offset(4f, mid + 300), Offset(w * 2 * 0.7f, mid + 300))
        assertTrue(r.drawer.currentValue == DrawerValue.Open || r.drawer.targetValue == DrawerValue.Open, "drawer opened (state ${r.drawer.currentValue}, pages ${r.pages})")
    }

    @Test fun theDrawerOpensFromTheRightEdgeInArabic() = rig(rtl = true) { r ->
        r.drag(Offset(w * 2 - 4f, mid + 300), Offset(w * 2 * 0.3f, mid + 300))
        assertTrue(r.drawer.currentValue == DrawerValue.Open || r.drawer.targetValue == DrawerValue.Open, "drawer opened (state ${r.drawer.currentValue})")
    }

    @Test fun aSwipeInTheMiddleOfTheFirstSectionDoesNotOpenTheDrawer() = rig(rtl = false) { r ->
        r.drag(Offset(w * 2 * 0.3f, mid + 300), Offset(w * 2 * 0.8f, mid + 300))
        assertEquals(DrawerValue.Closed, r.drawer.currentValue)
        assertEquals(Tab.Home, r.pages.last())
    }
}
