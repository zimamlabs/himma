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
    HimmaTheme(store.settings.themeMode) {
        var splash by remember { mutableStateOf(true) }
        var about by remember { mutableStateOf(false) }
        var tour by remember { mutableStateOf(store.startup is Startup.Fresh) }
        LaunchedEffect(Unit) { while (true) { delay(30_000); store.tick() } }
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            PlatformBackHandler(about) { about = false }
            when {
                about -> AboutScreen(store, onBack = { about = false }, onTour = { about = false; tour = true })
                tour -> Walkthrough(store) { store.markSeen(); tour = false }
                else -> MainScreen(store, onAbout = { about = true }, onTour = { tour = true })
            }
            val s = store.startup
            if (s is Startup.Updated && !splash && !tour) WhatsNewDialog(s.notes) { store.markSeen() }
            AnimatedVisibility(splash, exit = fadeOut()) { Splash { splash = false } }
        }
    }
    }
    }
}
