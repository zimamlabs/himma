package dev.himma.app

import dev.himma.core.WeightUnit
import dev.himma.core.weekSummary
import dev.himma.core.weightTrend
import dev.himma.app.res.this_week
import dev.himma.app.res.last_days
import dev.himma.app.res.kcal_a_day
import dev.himma.app.res.of
import dev.himma.app.res.water_goals
import dev.himma.app.res.workouts_label
import dev.himma.app.res.steps_a_day
import dev.himma.app.res.kg_aae5
import dev.himma.app.res.lifted
import dev.himma.app.res.calories
import dev.himma.app.res.water
import dev.himma.app.res.steps
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.himma.app.res.Res
import dev.himma.app.res.add
import dev.himma.app.res.add_water
import dev.himma.app.res.cancel
import dev.himma.app.res.custom
import dev.himma.app.res.custom_amount
import dev.himma.app.res.eat_from_hh_mm
import dev.himma.app.res.eating
import dev.himma.app.res.eating_to
import dev.himma.app.res.fast_hours
import dev.himma.app.res.fasting_label
import dev.himma.app.res.left
import dev.himma.app.res.millilitres
import dev.himma.app.res.ml_glass
import dev.himma.app.res.of_fcd7
import dev.himma.app.res.settings
import dev.himma.app.res.total
import dev.himma.app.res.undo
import dev.himma.app.res.weight
import dev.himma.core.fastState
import dev.himma.core.formatClock
import dev.himma.core.parseClock
import dev.himma.core.waterProgress

@Composable
internal fun TrackTab(store: HimmaStore, page: TrackPage, onPage: (TrackPage) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        LazyRow(Modifier.padding(top = 8.dp), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(TrackPage.entries.toList()) { p -> FilterChip(page == p, { onPage(p) }, { Text(s(p.label)) }) }
        }
        Box(Modifier.weight(1f)) {
            when (page) {
                TrackPage.Water -> WaterTab(store)
                TrackPage.Fasting -> FastingTab(store)
                TrackPage.Steps -> StepsTab(store)
                TrackPage.Weight -> WeightTab(store)
                TrackPage.Week -> WeekTab(store)
            }
        }
    }
}

@Composable
internal fun WaterTab(store: HimmaStore) {
    var custom by remember { mutableStateOf(false) }
    val goal = store.settings.waterGoalMl
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Ring(waterProgress(store.waterMl, goal), Modifier.size(220.dp)) {
            Text(waterText(store.waterMl), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text(s(Res.string.of_fcd7, waterText(goal)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(onClick = { store.addWater(250) }, Modifier.fillMaxWidth()) { Text(s(Res.string.ml_glass)) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { custom = true }) { Text(s(Res.string.custom_amount)) }
            OutlinedButton(onClick = { store.undoWater() }, enabled = store.canUndoWater) {
                Icon(Icons.AutoMirrored.Rounded.Undo, null, Modifier.size(18.dp)); Text(s(Res.string.undo))
            }
        }
    }
    if (custom) {
        var text by remember { mutableStateOf("") }
        val ml = text.trim().toIntOrNull()?.takeIf { it in 1..5000 }
        AlertDialog(
            onDismissRequest = { custom = false },
            title = { Text(s(Res.string.add_water)) },
            text = { OutlinedTextField(text, { text = it }, singleLine = true, label = { Text(s(Res.string.millilitres)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(enabled = ml != null, onClick = { store.addWater(ml!!); custom = false }) { Text(s(Res.string.add)) } },
            dismissButton = { TextButton(onClick = { custom = false }) { Text(s(Res.string.cancel)) } },
        )
    }
}

@Composable
internal fun FastingTab(store: HimmaStore) {
    val s = store.settings
    val fast = fastState(s.eatStartMinutes, s.fastHours, store.nowMinutes)
    var startText by remember(s.eatStartMinutes) { mutableStateOf(formatClock(s.eatStartMinutes)) }
    var customHours by remember(s.fastHours) { mutableStateOf(s.fastHours.toString()) }
    val start = parseClock(startText)
    val presets = listOf(16, 18, 20)
    val eatEnd = (s.eatStartMinutes + (24 - s.fastHours) * 60) % (24 * 60)
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        val total = if (fast.eating) (24 - s.fastHours) * 60 else s.fastHours * 60
        Ring(1f - fast.minutesLeft.toFloat() / total, Modifier.size(220.dp)) {
            Text(if (fast.eating) s(Res.string.eating) else s(Res.string.fasting_label), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text(s(Res.string.left, durationText(fast.minutesLeft)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(s(Res.string.eating_to, formatClock(s.eatStartMinutes), formatClock(eatEnd)), style = MaterialTheme.typography.bodyLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(presets) { h -> FilterChip(s.fastHours == h, { store.saveSettings(s.copy(fastHours = h)) }, { Text(num("$h:${24 - h}")) }) }
            item { FilterChip(s.fastHours !in presets, { customHours.toIntOrNull()?.takeIf { it in 1..23 }?.let { store.saveSettings(s.copy(fastHours = it)) } }, { Text(s(Res.string.custom)) }) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(customHours, { customHours = it; it.toIntOrNull()?.takeIf { h -> h in 1..23 }?.let { h -> store.saveSettings(s.copy(fastHours = h)) } }, singleLine = true, isError = customHours.toIntOrNull()?.let { it in 1..23 } != true,
                label = { Text(s(Res.string.fast_hours)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            OutlinedTextField(startText, { startText = it; parseClock(it)?.let { m -> store.saveSettings(s.copy(eatStartMinutes = m)) } }, singleLine = true, isError = start == null,
                label = { Text(s(Res.string.eat_from_hh_mm)) }, modifier = Modifier.weight(1f))
        }
    }
}

/** The seven-day charts and the weekly summary (they used to crowd Home). */
@Composable
internal fun WeekTab(store: HimmaStore) {
    val s = store.settings
    var chart by remember { mutableStateOf(0) }
    val dates = store.week.map { it.date }
    val summary = weekSummary(store.week, s.waterGoalMl, weightTrend(store.weights, store.today, 7).changeKg)
    val unit = WeightUnit.fromCode(s.weightUnit)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = tabPadding(), verticalArrangement = Arrangement.spacedBy(rowGap)) {
        item {
            Panel {
                Text(s(Res.string.last_days), style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    listOf(s(Res.string.water), s(Res.string.calories), s(Res.string.steps)).forEachIndexed { i, label -> FilterChip(chart == i, { chart = i }, { Text(label) }) }
                }
                when (chart) {
                    0 -> BarChart(store.week.map { it.waterMl.toFloat() }, dates, s.waterGoalMl.toFloat())
                    1 -> BarChart(store.week.map { it.kcal.toFloat() }, dates, s.calorieGoal.toFloat())
                    else -> BarChart(store.week.map { it.steps.toFloat() }, dates, s.stepGoal.toFloat())
                }
            }
        }
        item {
            Panel {
                Text(s(Res.string.this_week), style = MaterialTheme.typography.titleSmall)
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Stat(num(summary.avgKcal), s(Res.string.kcal_a_day))
                    Stat(s(Res.string.of, summary.waterGoalDays), s(Res.string.water_goals))
                    Stat(num(summary.workouts), s(Res.string.workouts_label))
                }
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Stat(if (summary.avgSteps > 0) num(summary.avgSteps) else "-", s(Res.string.steps_a_day))
                    Stat(s(Res.string.kg_aae5, summary.volumeKg), s(Res.string.lifted))
                    Stat(summary.weightChangeKg?.let { (if (it > 0) "+" else "") + weightText(it, unit) } ?: "-", s(Res.string.weight))
                }
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
