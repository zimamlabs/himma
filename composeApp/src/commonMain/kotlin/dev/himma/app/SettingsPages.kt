package dev.himma.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.himma.app.res.Res
import dev.himma.app.res.allow_all_files_access
import dev.himma.app.res.colour_theme
import dev.himma.app.res.daily_calorie_goal
import dev.himma.app.res.daily_step_goal
import dev.himma.app.res.daily_water_goal_ml
import dev.himma.app.res.data_folder
import dev.himma.app.res.delete
import dev.himma.app.res.delete_all_body
import dev.himma.app.res.delete_all_data
import dev.himma.app.res.delete_all_data_question
import dev.himma.app.res.delete_all_word
import dev.himma.app.res.density_compact
import dev.himma.app.res.density_comfortable
import dev.himma.app.res.density_label
import dev.himma.app.res.export_all_data_as_zip
import dev.himma.app.res.home_always_shown
import dev.himma.app.res.keep_it
import dev.himma.app.res.arabic_digits
import dev.himma.app.res.language
import dev.himma.app.res.mode
import dev.himma.app.res.move_down
import dev.himma.app.res.move_up
import dev.himma.app.res.notification_settings
import dev.himma.app.res.privacy_body
import dev.himma.app.res.reduce_animations
import dev.himma.app.res.reminders_all_set
import dev.himma.app.res.remove
import dev.himma.app.res.remove_the_old_copy
import dev.himma.app.res.remove_the_old_copy_495b
import dev.himma.app.res.shown_in_menu_and_bar
import dev.himma.app.res.show_medicine_names_on_the_lock_screen
import dev.himma.app.res.show_the_tour_again
import dev.himma.app.res.start_on
import dev.himma.app.res.system
import dev.himma.app.res.text_preview
import dev.himma.app.res.text_size
import dev.himma.app.res.type_to_confirm
import dev.himma.app.res.weight_unit
import dev.himma.app.res.kg_label
import dev.himma.app.res.lb_label
import dev.himma.app.res.your_files_are_safe_in
import dev.himma.core.Sections
import dev.himma.core.Settings

@Composable
private fun Label(text: String) = Text(text, Modifier.padding(top = 12.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

@Composable
private fun SwitchRow(text: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked, onChange)
    }
}

private fun HimmaStore.change(f: (Settings) -> Settings) = saveSettings(f(settings))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppearancePage(store: HimmaStore) {
    val st = store.settings
    Label(s(Res.string.mode))
    val modes = listOf("system", "light", "dark")
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        modes.forEachIndexed { i, m -> SegmentedButton(st.themeMode == m, { store.change { it.copy(themeMode = m) } }, SegmentedButtonDefaults.itemShape(i, modes.size)) { Text(modeName(m)) } }
    }
    Label(s(Res.string.colour_theme))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        allPalettes.forEach { id ->
            val color = (accents.getValue(id)).let { if (MaterialTheme.colorScheme.background.luminanceIsDark()) it.dark else it.light }
            val name = paletteName(id)
            Box(
                Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.RadioButton) { store.change { it.copy(palette = id) } }
                    .border(if (st.palette == id) 3.dp else 1.dp, if (st.palette == id) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .padding(6.dp).clip(CircleShape).background(color).semantics { contentDescription = name },
                contentAlignment = Alignment.Center,
            ) { if (st.palette == id) Icon(Icons.Rounded.Check, null, tint = MaterialTheme.colorScheme.background) }
        }
    }
    Text(paletteName(st.palette), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Label(s(Res.string.text_size))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("A", style = MaterialTheme.typography.bodySmall)
        Slider(st.textSize.toFloat(), { store.change { s -> s.copy(textSize = it.toInt()) } }, Modifier.weight(1f), valueRange = 0f..3f, steps = 2)
        Text("A", style = MaterialTheme.typography.headlineSmall)
    }
    Panel { Text(textSizeName(st.textSize), style = MaterialTheme.typography.titleSmall); Text(s(Res.string.text_preview), style = MaterialTheme.typography.bodyLarge) }
    Label(s(Res.string.density_label))
    val densities = listOf("comfortable" to Res.string.density_comfortable, "compact" to Res.string.density_compact)
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        densities.forEachIndexed { i, (k, label) -> SegmentedButton(st.density == k, { store.change { it.copy(density = k) } }, SegmentedButtonDefaults.itemShape(i, densities.size)) { Text(s(label)) } }
    }
    SwitchRow(s(Res.string.reduce_animations), st.reduceMotion) { v -> store.change { it.copy(reduceMotion = v) } }
}

private fun androidx.compose.ui.graphics.Color.luminanceIsDark() = this.luminance() < 0.5f

@Composable
internal fun SectionsPage(store: HimmaStore) {
    val st = store.settings
    val order = st.sectionOrder.filter { it in Sections.all }.let { it + (Sections.all - it.toSet()) }
    Text(s(Res.string.shown_in_menu_and_bar), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    order.forEachIndexed { i, id ->
        val tab = Tab.byId(id)
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(tab.icon, null, Modifier.padding(end = 12.dp))
            Column(Modifier.weight(1f)) {
                Text(s(tab.label), style = MaterialTheme.typography.bodyLarge)
                if (id == Sections.HOME) Text(s(Res.string.home_always_shown), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(enabled = i > 0, onClick = { store.change { s -> s.copy(sectionOrder = order.toMutableList().also { it.add(i - 1, it.removeAt(i)) }) } }) { Icon(Icons.Rounded.ArrowUpward, s(Res.string.move_up)) }
            IconButton(enabled = i < order.lastIndex, onClick = { store.change { s -> s.copy(sectionOrder = order.toMutableList().also { it.add(i + 1, it.removeAt(i)) }) } }) { Icon(Icons.Rounded.ArrowDownward, s(Res.string.move_down)) }
            Switch(id == Sections.HOME || id !in st.hiddenSections, { on -> store.change { s -> s.copy(hiddenSections = if (on) s.hiddenSections - id else s.hiddenSections + id) } }, enabled = id != Sections.HOME)
        }
    }
    Label(s(Res.string.start_on))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().then(Modifier)) {
        visibleTabs(st).forEach { t -> FilterChip(st.startSection == t.id, { store.change { it.copy(startSection = t.id) } }, { Text(s(t.label)) }) }
    }
}

@Composable
internal fun LanguagePage(store: HimmaStore) {
    Label(s(Res.string.language))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("system" to s(Res.string.system), "en" to "English", "ar" to "العربية").forEach { (code, label) ->
            FilterChip(store.platform.language == code, { if (store.platform.language != code) store.platform.setLanguage(code) }, { Text(label) })
        }
    }
    SwitchRow(s(Res.string.arabic_digits), UiText.arabicDigits) { store.platform.setArabicDigits(it) }
}

@Composable
internal fun NotificationsPage(store: HimmaStore) {
    if (store.reminderProblems.isEmpty()) Panel { Text(s(Res.string.reminders_all_set), style = MaterialTheme.typography.titleMedium) }
    else RemindersCard(store.reminderProblems, store.platform::openNotificationSettings, store.platform::openExactAlarmSettings)
    SwitchRow(s(Res.string.show_medicine_names_on_the_lock_screen), store.settings.lockScreenNames) { v -> store.change { it.copy(lockScreenNames = v) } }
    OutlinedButton(onClick = store.platform::openNotificationSettings) { Text(s(Res.string.notification_settings)) }
}

@Composable
private fun GoalField(label: String, value: Int, range: IntRange, onValid: (Int) -> Unit) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    val parsed = text.trim().toIntOrNull()?.takeIf { it in range }
    OutlinedTextField(text, { text = it; it.trim().toIntOrNull()?.takeIf { v -> v in range }?.let(onValid) }, Modifier.fillMaxWidth(), singleLine = true, isError = parsed == null, label = { Text(label) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
}

@Composable
internal fun UnitsPage(store: HimmaStore) {
    val st = store.settings
    Label(s(Res.string.weight_unit))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("kg" to s(Res.string.kg_label), "lb" to s(Res.string.lb_label)).forEach { (k, label) -> FilterChip(st.weightUnit == k, { store.change { it.copy(weightUnit = k) } }, { Text(label) }) }
    }
    GoalField(s(Res.string.daily_calorie_goal), st.calorieGoal, 1..20000) { v -> store.change { it.copy(calorieGoal = v) } }
    GoalField(s(Res.string.daily_water_goal_ml), st.waterGoalMl, 1..20000) { v -> store.change { it.copy(waterGoalMl = v) } }
    GoalField(s(Res.string.daily_step_goal), st.stepGoal, 100..100000) { v -> store.change { it.copy(stepGoal = v) } }
}

@Composable
internal fun DataPage(store: HimmaStore, onTour: () -> Unit) {
    var confirmRemove by remember { mutableStateOf(false) }
    var confirmDeleteAll by remember { mutableStateOf(false) }
    var typed by remember { mutableStateOf("") }
    Label(s(Res.string.data_folder))
    Text(store.folderPath, style = MaterialTheme.typography.bodyLarge)
    if (store.platform.canAskAllFiles && !store.sharedFolder) OutlinedButton(onClick = { store.platform.allowAllFiles() }) { Text(s(Res.string.allow_all_files_access)) }
    if (store.canRemoveOld) OutlinedButton(onClick = { confirmRemove = true }) { Text(s(Res.string.remove_the_old_copy)) }
    if (store.platform.canExport) OutlinedButton(onClick = { store.exportZip() }) { Text(s(Res.string.export_all_data_as_zip)) }
    OutlinedButton(onClick = { typed = ""; confirmDeleteAll = true }) { Text(s(Res.string.delete_all_data), color = MaterialTheme.colorScheme.error) }
    TextButton(onClick = onTour) { Text(s(Res.string.show_the_tour_again)) }
    if (confirmDeleteAll) {
        val word = s(Res.string.delete_all_word)
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text(s(Res.string.delete_all_data_question)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s(Res.string.delete_all_body, store.folderPath))
                    OutlinedTextField(typed, { typed = it }, singleLine = true, label = { Text(s(Res.string.type_to_confirm, word)) }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = { TextButton(enabled = typed.trim().equals(word, ignoreCase = true), onClick = { store.deleteAllData(); confirmDeleteAll = false }) { Text(s(Res.string.delete), color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirmDeleteAll = false }) { Text(s(Res.string.keep_it)) } },
        )
    }
    if (confirmRemove) AlertDialog(
        onDismissRequest = { confirmRemove = false },
        title = { Text(s(Res.string.remove_the_old_copy_495b)) },
        text = { Text(s(Res.string.your_files_are_safe_in, store.folderPath)) },
        confirmButton = { TextButton(onClick = { store.removeOldCopy(); confirmRemove = false }) { Text(s(Res.string.remove), color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text(s(Res.string.keep_it)) } },
    )
}

@Composable
internal fun PrivacyPage(store: HimmaStore) {
    Panel { Text(s(Res.string.privacy_body, store.folderPath), style = MaterialTheme.typography.bodyLarge) }
}
