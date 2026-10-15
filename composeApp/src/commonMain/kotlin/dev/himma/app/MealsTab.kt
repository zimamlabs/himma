package dev.himma.app

import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.foundation.layout.heightIn
import dev.himma.app.res.menu_duplicate
import dev.himma.app.res.menu_edit
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import dev.himma.app.res.calories
import dev.himma.app.res.cancel
import dev.himma.app.res.carbs_g
import dev.himma.app.res.carbs_g_ce6a
import dev.himma.app.res.delete
import dev.himma.app.res.delete_this_meal
import dev.himma.app.res.done
import dev.himma.app.res.edit_meal
import dev.himma.app.res.edit_saved_meals
import dev.himma.app.res.fat_g
import dev.himma.app.res.fat_g_e7c0
import dev.himma.app.res.kcal
import dev.himma.app.res.kcal_06b1
import dev.himma.app.res.left
import dev.himma.app.res.left_label
import dev.himma.app.res.log_a_meal
import dev.himma.app.res.log_again
import dev.himma.app.res.meal_will_be_removed
import dev.himma.app.res.meals
import dev.himma.app.res.name
import dev.himma.app.res.next_day
import dev.himma.app.res.no_meals_logged
import dev.himma.app.res.nothing_saved_open_a_meal
import dev.himma.app.res.over
import dev.himma.app.res.previous_day
import dev.himma.app.res.protein_g
import dev.himma.app.res.protein_g_29f5
import dev.himma.app.res.remove_x
import dev.himma.app.res.save
import dev.himma.app.res.save_as_meal
import dev.himma.app.res.saved_meals
import dev.himma.app.res.settings
import dev.himma.app.res.tap_to_add_one_each
import dev.himma.app.res.time_hh_mm
import dev.himma.app.res.today
import dev.himma.app.res.total
import dev.himma.app.res.weight
import dev.himma.core.Meal
import dev.himma.core.caloriesLeft
import dev.himma.core.dayTotals
import dev.himma.core.formatClock
import dev.himma.core.parseClock

@Composable
internal fun MealsTab(store: HimmaStore, onTemplates: () -> Unit, onEdit: (Meal) -> Unit) {
    val totals = dayTotals(store.meals)
    val left = caloriesLeft(store.settings.calorieGoal, totals)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = tabPadding(), verticalArrangement = Arrangement.spacedBy(rowGap)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { store.shiftMealDay(-1) }) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, s(Res.string.previous_day)) }
                Text(if (store.mealDay == store.today) s(Res.string.today) else prettyDate(store.mealDay), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { store.shiftMealDay(1) }) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, s(Res.string.next_day)) }
            }
        }
        item {
            Panel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { Text(s(Res.string.total), style = MaterialTheme.typography.labelMedium); Text(s(Res.string.kcal, totals.kcal), style = MaterialTheme.typography.titleLarge) }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(if (left >= 0) s(Res.string.left_label) else s(Res.string.over), style = MaterialTheme.typography.labelMedium)
                        Text(s(Res.string.kcal, kotlin.math.abs(left)), style = MaterialTheme.typography.titleLarge, color = if (left >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
                    }
                }
                MacroBar(totals.protein, totals.carbs, totals.fat)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Legend(MaterialTheme.colorScheme.primary, s(Res.string.protein_g, totals.protein))
                    Legend(MaterialTheme.colorScheme.secondary, s(Res.string.carbs_g, totals.carbs))
                    Legend(MaterialTheme.colorScheme.tertiary, s(Res.string.fat_g, totals.fat))
                }
            }
        }
        if (store.templates.isNotEmpty() || store.recent.isNotEmpty()) item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(s(Res.string.log_again), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(store.templates, key = { "t" + it.id }) { t -> AssistChip(onClick = { store.logAgain(t.name, t.kcal, t.protein, t.carbs, t.fat) }, label = { Text("${t.name} · ${num(t.kcal)}") }) }
                    items(store.recent.filter { r -> store.templates.none { it.name.equals(r.name, true) && it.kcal == r.kcal } }, key = { "r" + it.id }) { m ->
                        AssistChip(onClick = { store.logAgain(m.name, m.kcal, m.protein, m.carbs, m.fat) }, label = { Text("${m.name} · ${num(m.kcal)}") })
                    }
                }
                if (store.templates.isNotEmpty()) TextButton(onClick = onTemplates) { Text(s(Res.string.edit_saved_meals)) }
            }
        }
        if (store.meals.isEmpty()) item { EmptyHint(s(Res.string.no_meals_logged), s(Res.string.tap_to_add_one_each)) }
        items(store.meals, key = { it.id }) { m ->
            val actions = listOf(
                MenuAction(s(Res.string.menu_edit), Icons.Rounded.Edit) { onEdit(m) },
                MenuAction(s(Res.string.menu_duplicate), Icons.Rounded.ContentCopy) { store.logAgain(m.name, m.kcal, m.protein, m.carbs, m.fat) },
                MenuAction(s(Res.string.delete), Icons.Rounded.Delete, destructive = true, confirm = s(Res.string.delete_this_meal) to s(Res.string.meal_will_be_removed, m.name, m.kcal)) { store.deleteMeal(m.id) },
            )
            ContextMenuBox(actions, { onEdit(m) }, Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(m.name, style = MaterialTheme.typography.bodyLarge)
                    val macros = listOfNotNull(m.protein?.let { s(Res.string.protein_g, it) }, m.carbs?.let { s(Res.string.carbs_g, it) }, m.fat?.let { s(Res.string.fat_g, it) }).joinToString("  ")
                    Text(num(m.time) + if (macros.isNotEmpty()) "  ·  $macros" else "", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Text(s(Res.string.kcal, m.kcal), style = MaterialTheme.typography.titleSmall)
            } }
        }
    }
}

@Composable
internal fun MealDialog(existing: Meal?, nowTime: String, onDismiss: () -> Unit, onDelete: (() -> Unit)?, onTemplate: (Meal) -> Unit, onSave: (Meal) -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var kcal by remember { mutableStateOf(existing?.kcal?.toString() ?: "") }
    var protein by remember { mutableStateOf(existing?.protein?.toString() ?: "") }
    var carbs by remember { mutableStateOf(existing?.carbs?.toString() ?: "") }
    var fat by remember { mutableStateOf(existing?.fat?.toString() ?: "") }
    var time by remember { mutableStateOf(existing?.time ?: nowTime) }
    val kcalValue = dev.himma.core.mealKcal(kcal)
    val timeValue = parseClock(time)
    fun optional(t: String): Pair<Boolean, Int?> = if (t.isBlank()) true to null else (t.trim().toIntOrNull()?.takeIf { it >= 0 }).let { (it != null) to it }
    val (pOk, p) = optional(protein); val (cOk, c) = optional(carbs); val (fOk, f) = optional(fat)
    val number = KeyboardOptions(keyboardType = KeyboardType.Number)
    var confirmDelete by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) s(Res.string.log_a_meal) else s(Res.string.edit_meal)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, singleLine = true, label = { Text(s(Res.string.name)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(kcal, { kcal = it }, singleLine = true, isError = kcalValue == null, label = { Text(s(Res.string.calories)) }, keyboardOptions = number, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(protein, { protein = it }, singleLine = true, isError = !pOk, label = { Text(s(Res.string.protein_g_29f5)) }, keyboardOptions = number, modifier = Modifier.weight(1f))
                    OutlinedTextField(carbs, { carbs = it }, singleLine = true, isError = !cOk, label = { Text(s(Res.string.carbs_g_ce6a)) }, keyboardOptions = number, modifier = Modifier.weight(1f))
                    OutlinedTextField(fat, { fat = it }, singleLine = true, isError = !fOk, label = { Text(s(Res.string.fat_g_e7c0)) }, keyboardOptions = number, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(time, { time = it }, singleLine = true, isError = timeValue == null, label = { Text(s(Res.string.time_hh_mm)) }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank() && kcalValue != null && timeValue != null && pOk && cOk && fOk, onClick = {
                onSave(Meal(existing?.id ?: "", formatClock(timeValue!!), name.trim(), kcalValue!!, p, c, f))
            }) { Text(s(Res.string.save)) }
        },
        dismissButton = {
            Row {
                TextButton(enabled = name.isNotBlank() && kcalValue != null && pOk && cOk && fOk, onClick = { onTemplate(Meal("", "", name.trim(), kcalValue!!, p, c, f)); onDismiss() }) { Text(s(Res.string.save_as_meal)) }
                onDelete?.let { TextButton(onClick = { confirmDelete = true }) { Text(s(Res.string.delete), color = MaterialTheme.colorScheme.error) } }
                TextButton(onClick = onDismiss) { Text(s(Res.string.cancel)) }
            }
        },
    )
    if (confirmDelete && existing != null) ConfirmDelete(s(Res.string.delete_this_meal), s(Res.string.meal_will_be_removed, existing.name, existing.kcal), { confirmDelete = false; onDelete?.invoke() }, { confirmDelete = false })
}

@Composable
internal fun TemplatesDialog(store: HimmaStore, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s(Res.string.saved_meals)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (store.templates.isEmpty()) Text(s(Res.string.nothing_saved_open_a_meal))
                store.templates.forEach { t ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(s(Res.string.kcal_06b1, t.name, t.kcal), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { store.deleteTemplate(t.id) }) { Icon(Icons.Rounded.Close, s(Res.string.remove_x, t.name)) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(s(Res.string.done)) } },
    )
}
