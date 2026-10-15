package dev.himma.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import dev.himma.core.Startup
import kotlinx.coroutines.delay

const val APP_VERSION = "0.4.1"

@Composable
fun HimmaApp(store: HimmaStore) {
    val direction = when (UiText.rtl) { true -> LayoutDirection.Rtl; false -> LayoutDirection.Ltr; null -> LocalLayoutDirection.current }
    androidx.compose.runtime.key(UiText.generation) {
    androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides direction) {
    androidx.compose.runtime.CompositionLocalProvider(LocalCompact provides (store.settings.density == "compact")) {
    HimmaTheme(store.settings.themeMode, store.settings.palette, store.settings.textSize) {
        var splash by remember { mutableStateOf(!store.settings.reduceMotion) }
        // The screen is kept as text so it survives the activity restart a language change causes.
        var screen by rememberSaveable { mutableStateOf("main") }
        val about = screen == "about"
        var tour by remember { mutableStateOf(store.startup is Startup.Fresh) }
        LaunchedEffect(Unit) { while (true) { delay(30_000); store.tick() } }
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            PlatformBackHandler(about) { screen = "main" }
            when {
                about -> AboutScreen(store, onBack = { screen = "main" }, onTour = { screen = "main"; tour = true })
                tour -> Walkthrough(store) { store.markSeen(); tour = false }
                screen.startsWith("settings") -> SettingsScreen(
                    store, page = SettingsPage.entries.firstOrNull { screen == "settings:" + it.name },
                    onPage = { screen = if (it == null) "settings" else "settings:" + it.name },
                    onBack = { screen = "main" }, onAbout = { screen = "about" }, onTour = { screen = "main"; tour = true },
                )
                else -> MainScreen(store, onAbout = { screen = "about" }, onTour = { tour = true }, onSettings = { screen = "settings" })
            }
            val s = store.startup
            if (s is Startup.Updated && !splash && !tour) WhatsNewDialog(s.notes) { store.markSeen() }
            AnimatedVisibility(splash, exit = fadeOut()) { Splash { splash = false } }
        }
    }
    }
    }
    }
}
