package dev.himma.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.ViewQuilt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.himma.app.res.Res
import dev.himma.app.res.about
import dev.himma.app.res.appearance
import dev.himma.app.res.back
import dev.himma.app.res.dark
import dev.himma.app.res.data_and_storage
import dev.himma.app.res.digits_arabic
import dev.himma.app.res.digits_latin
import dev.himma.app.res.language
import dev.himma.app.res.light
import dev.himma.app.res.notifications_and_reminders
import dev.himma.app.res.palette_forest
import dev.himma.app.res.palette_graphite
import dev.himma.app.res.palette_ocean
import dev.himma.app.res.palette_rose
import dev.himma.app.res.palette_violet
import dev.himma.app.res.palette_zimam
import dev.himma.app.res.privacy_and_security
import dev.himma.app.res.privacy_summary
import dev.himma.app.res.reminders_all_set
import dev.himma.app.res.reminders_need_attention
import dev.himma.app.res.sections_and_layout
import dev.himma.app.res.sections_summary
import dev.himma.app.res.settings
import dev.himma.app.res.summary2
import dev.himma.app.res.summary3
import dev.himma.app.res.system
import dev.himma.app.res.text_size_0
import dev.himma.app.res.text_size_1
import dev.himma.app.res.text_size_2
import dev.himma.app.res.text_size_3
import dev.himma.app.res.goals_summary
import dev.himma.app.res.units_and_goals
import dev.himma.app.res.version
import dev.himma.core.Palettes
import org.jetbrains.compose.resources.StringResource

/** The Settings pages, in the order of the shared spec (ui-spec-shared.md). About leaves Settings for the About screen. */
internal enum class SettingsPage(val title: StringResource, val icon: ImageVector) {
    Appearance(Res.string.appearance, Icons.Rounded.Palette),
    Sections(Res.string.sections_and_layout, Icons.Rounded.ViewQuilt),
    Language(Res.string.language, Icons.Rounded.Language),
    Notifications(Res.string.notifications_and_reminders, Icons.Rounded.Notifications),
    Units(Res.string.units_and_goals, Icons.Rounded.Straighten),
    Data(Res.string.data_and_storage, Icons.Rounded.Cloud),
    Privacy(Res.string.privacy_and_security, Icons.Rounded.Lock),
    About(Res.string.about, Icons.Rounded.Info),
}

@Composable
internal fun paletteName(id: String): String = s(when (id) {
    "ocean" -> Res.string.palette_ocean; "forest" -> Res.string.palette_forest; "violet" -> Res.string.palette_violet
    "rose" -> Res.string.palette_rose; "graphite" -> Res.string.palette_graphite; else -> Res.string.palette_zimam
})

@Composable
internal fun textSizeName(step: Int): String = s(when (step) { 0 -> Res.string.text_size_0; 2 -> Res.string.text_size_2; 3 -> Res.string.text_size_3; else -> Res.string.text_size_1 })

@Composable
internal fun modeName(mode: String): String = s(when (mode) { "light" -> Res.string.light; "dark" -> Res.string.dark; else -> Res.string.system })

/** Settings as a list of labelled rows (icon, title, one line about the current value); a row opens its page. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(store: HimmaStore, page: SettingsPage?, onPage: (SettingsPage?) -> Unit, onBack: () -> Unit, onAbout: () -> Unit, onTour: () -> Unit) {
    PlatformBackHandler(true) { if (page != null) onPage(null) else onBack() }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(s(page?.title ?: Res.string.settings), fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = { if (page != null) onPage(null) else onBack() }) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, s(Res.string.back)) } },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (page) {
                null -> SettingsRows(store) { if (it == SettingsPage.About) onAbout() else onPage(it) }
                SettingsPage.Appearance -> AppearancePage(store)
                SettingsPage.Sections -> SectionsPage(store)
                SettingsPage.Language -> LanguagePage(store)
                SettingsPage.Notifications -> NotificationsPage(store)
                SettingsPage.Units -> UnitsPage(store)
                SettingsPage.Data -> DataPage(store, onTour)
                SettingsPage.Privacy -> PrivacyPage(store)
                SettingsPage.About -> Unit
            }
        }
    }
}

@Composable
private fun SettingsRows(store: HimmaStore, onOpen: (SettingsPage) -> Unit) {
    val st = store.settings
    val summaries = mapOf(
        SettingsPage.Appearance to s(Res.string.summary3, modeName(st.themeMode), paletteName(st.palette), textSizeName(st.textSize)),
        SettingsPage.Sections to s(Res.string.sections_summary, s(Tab.byId(st.startSection).label), visibleTabs(st).size),
        SettingsPage.Language to s(Res.string.summary2, when (store.platform.language) { "en" -> "English"; "ar" -> "العربية"; else -> s(Res.string.system) }, s(if (UiText.arabicDigits) Res.string.digits_arabic else Res.string.digits_latin)),
        SettingsPage.Notifications to s(if (store.reminderProblems.isEmpty()) Res.string.reminders_all_set else Res.string.reminders_need_attention),
        SettingsPage.Units to s(Res.string.goals_summary, st.calorieGoal, waterText(st.waterGoalMl), st.stepGoal),
        SettingsPage.Data to store.folderPath,
        SettingsPage.Privacy to s(Res.string.privacy_summary),
        SettingsPage.About to s(Res.string.version, APP_VERSION),
    )
    SettingsPage.entries.forEach { p -> SettingsRow(p.icon, s(p.title), summaries.getValue(p)) { onOpen(p) } }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, summary: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 72.dp).clip20().clickable(role = Role.Button, onClick = onClick).background(MaterialTheme.colorScheme.surfaceContainer).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(Modifier.size(44.dp).clip20().background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun Modifier.clip20() = this.then(Modifier.clip(RoundedCornerShape(20.dp)))

internal val settingsBorder @Composable get() = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
internal val allPalettes = Palettes.ids
