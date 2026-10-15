package dev.himma.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.himma.app.res.Res
import dev.himma.app.res.allow_all_files_access
import dev.himma.app.res.arabic_digits
import dev.himma.app.res.cancel
import dev.himma.app.res.daily_calorie_goal
import dev.himma.app.res.daily_step_goal
import dev.himma.app.res.daily_water_goal_ml
import dev.himma.app.res.dark
import dev.himma.app.res.data_folder
import dev.himma.app.res.delete
import dev.himma.app.res.delete_all_body
import dev.himma.app.res.delete_all_data
import dev.himma.app.res.delete_all_data_question
import dev.himma.app.res.delete_all_word
import dev.himma.app.res.export_all_data_as_zip
import dev.himma.app.res.kcal
import dev.himma.app.res.keep_it
import dev.himma.app.res.kg_label
import dev.himma.app.res.language
import dev.himma.app.res.lb_label
import dev.himma.app.res.light
import dev.himma.app.res.remove
import dev.himma.app.res.remove_the_old_copy
import dev.himma.app.res.remove_the_old_copy_495b
import dev.himma.app.res.save
import dev.himma.app.res.settings
import dev.himma.app.res.show_medicine_names_on_the_lock_screen
import dev.himma.app.res.show_the_tour_again
import dev.himma.app.res.steps
import dev.himma.app.res.system
import dev.himma.app.res.theme
import dev.himma.app.res.type_to_confirm
import dev.himma.app.res.water
import dev.himma.app.res.weight
import dev.himma.app.res.weight_unit
import dev.himma.app.res.your_files_are_safe_in
import dev.himma.core.Settings

@Composable
internal fun SettingsDialog(store: HimmaStore, onDismiss: () -> Unit, onTour: () -> Unit, onSave: (Settings) -> Unit) {
    val current = store.settings
    var kcal by remember { mutableStateOf(current.calorieGoal.toString()) }
    var water by remember { mutableStateOf(current.waterGoalMl.toString()) }
    var confirmRemove by remember { mutableStateOf(false) }
    var steps by remember { mutableStateOf(current.stepGoal.toString()) }
    var theme by remember { mutableStateOf(current.themeMode) }
    var confirmDeleteAll by remember { mutableStateOf(false) }
    var typed by remember { mutableStateOf("") }
    var weightUnit by remember { mutableStateOf(current.weightUnit) }
    var lockNames by remember { mutableStateOf(current.lockScreenNames) }
    val stepsValue = steps.trim().toIntOrNull()?.takeIf { it in 100..100000 }
    val kcalValue = kcal.trim().toIntOrNull()?.takeIf { it in 1..20000 }
    val waterValue = water.trim().toIntOrNull()?.takeIf { it in 1..20000 }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s(Res.string.settings)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(kcal, { kcal = it }, singleLine = true, isError = kcalValue == null, label = { Text(s(Res.string.daily_calorie_goal)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(water, { water = it }, singleLine = true, isError = waterValue == null, label = { Text(s(Res.string.daily_water_goal_ml)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(steps, { steps = it }, singleLine = true, isError = stepsValue == null, label = { Text(s(Res.string.daily_step_goal)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Text(s(Res.string.weight_unit), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("kg" to s(Res.string.kg_label), "lb" to s(Res.string.lb_label)).forEach { (k, label) -> FilterChip(weightUnit == k, { weightUnit = k }, { Text(label) }) }
                }
                Text(s(Res.string.theme), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("system" to s(Res.string.system), "light" to s(Res.string.light), "dark" to s(Res.string.dark)).forEach { (k, label) -> FilterChip(theme == k, { theme = k }, { Text(label) }) }
                }
                Text(s(Res.string.language), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("system" to s(Res.string.system), "en" to "English", "ar" to "العربية").forEach { (code, label) ->
                        FilterChip(store.platform.language == code, { if (store.platform.language != code) store.platform.setLanguage(code) }, { Text(label) })
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(s(Res.string.arabic_digits), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    androidx.compose.material3.Switch(UiText.arabicDigits, { store.platform.setArabicDigits(it) })
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(s(Res.string.show_medicine_names_on_the_lock_screen), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    androidx.compose.material3.Switch(lockNames, { lockNames = it })
                }
                Text(s(Res.string.data_folder), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(store.folderPath, style = MaterialTheme.typography.bodyMedium)
                if (store.platform.canAskAllFiles && !store.sharedFolder) OutlinedButton(onClick = { store.platform.allowAllFiles() }) { Text(s(Res.string.allow_all_files_access)) }
                if (store.canRemoveOld) OutlinedButton(onClick = { confirmRemove = true }) { Text(s(Res.string.remove_the_old_copy)) }
                if (store.platform.canExport) OutlinedButton(onClick = { store.exportZip() }) { Text(s(Res.string.export_all_data_as_zip)) }
                OutlinedButton(onClick = { typed = ""; confirmDeleteAll = true }) { Text(s(Res.string.delete_all_data), color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onTour) { Text(s(Res.string.show_the_tour_again)) }
            }
        },
        confirmButton = { TextButton(enabled = kcalValue != null && waterValue != null && stepsValue != null, onClick = { onSave(current.copy(calorieGoal = kcalValue!!, waterGoalMl = waterValue!!, stepGoal = stepsValue!!, themeMode = theme, lockScreenNames = lockNames, weightUnit = weightUnit)) }) { Text(s(Res.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s(Res.string.cancel)) } },
    )
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
            confirmButton = { TextButton(enabled = typed.trim().equals(word, ignoreCase = true), onClick = { store.deleteAllData(); confirmDeleteAll = false; onDismiss() }) { Text(s(Res.string.delete), color = MaterialTheme.colorScheme.error) } },
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
