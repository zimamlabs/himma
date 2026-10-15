package dev.himma.app

import dev.himma.app.res.left

import dev.himma.app.res.steps_a_day

import dev.himma.app.res.workouts_label

import dev.himma.app.res.water_goals

import dev.himma.app.res.kcal_a_day

import dev.himma.app.res.n_of_m_taken

import dev.himma.app.res.percent_of_goals

import dev.himma.app.res.n_over

import dev.himma.app.res.calories
import dev.himma.app.res.medicine
import dev.himma.app.res.steps
import dev.himma.app.res.water

import dev.himma.app.res.Res
import dev.himma.app.res.all_of_today_s_goals
import dev.himma.app.res.carbs_g
import dev.himma.app.res.create_a_routine_to_start
import dev.himma.app.res.done_today
import dev.himma.app.res.eating_window_is_open
import dev.himma.app.res.fasting
import dev.himma.app.res.fasting_now
import dev.himma.app.res.fat_g
import dev.himma.app.res.kg_aae5
import dev.himma.app.res.last_days
import dev.himma.app.res.last_workout
import dev.himma.app.res.meal
import dev.himma.app.res.ml
import dev.himma.app.res.next_at
import dev.himma.app.res.no_doses_today
import dev.himma.app.res.no_medicines_added_yet
import dev.himma.app.res.no_more_doses_today
import dev.himma.app.res.of
import dev.himma.app.res.protein_g
import dev.himma.app.res.this_week
import dev.himma.app.res.up_next
import dev.himma.app.res.weight
import dev.himma.app.res.what_s_new_in
import dev.himma.app.res.workout
import dev.himma.app.res.workouts_kg_lifted

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.himma.app.res.lifted
import dev.himma.core.DoseMark
import dev.himma.core.Routine
import dev.himma.core.caloriesLeft
import dev.himma.core.dayScore
import dev.himma.core.WeightUnit
import dev.himma.core.dayTotals
import dev.himma.core.fastState
import dev.himma.core.stepProgress
import dev.himma.core.volume
import dev.himma.core.waterProgress
import dev.himma.core.weekSummary
import dev.himma.core.weightTrend

/** Where a Home card can send you. */
internal class Nav(
    val go: (Tab, TrackPage?) -> Unit,
    val logMeal: () -> Unit,
    val startWorkout: (Routine?) -> Unit,
)

@Composable
private fun SectionTitle(icon: ImageVector, title: String, trailing: String = "") {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(30.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.primary)
        }
        Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
        if (trailing.isNotEmpty()) Text(trailing, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MiniRing(progress: Float, value: String, label: String, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Ring(progress, Modifier.size(84.dp)) { Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold) }
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun HomeTab(store: HimmaStore, nav: Nav) {
    val s = store.settings
    val totals = dayTotals(store.todayMeals)
    val left = caloriesLeft(s.calorieGoal, totals)
    val fast = fastState(s.eatStartMinutes, s.fastHours, store.nowMinutes)
    val doses = store.todayDoses
    val taken = doses.count { store.doseMarks[it.key] == DoseMark.Taken }
    val next = doses.firstOrNull { it.at > store.now }
    val stepsReady = store.platform.stepSupport == StepSupport.Ready || store.stepsToday > 0
    val waterP = waterProgress(store.waterMl, s.waterGoalMl)
    val kcalP = (totals.kcal.toFloat() / s.calorieGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val stepP = stepProgress(store.stepsToday, s.stepGoal)
    val todayWorkouts = store.workouts.filter { it.date == store.today.toString() }
    val score = dayScore(listOf(waterP, if (store.todayMeals.isEmpty()) 0f else kcalP, if (stepsReady) stepP else null, if (doses.isEmpty()) null else taken.toFloat() / doses.size))
    var chart by remember { mutableStateOf(0) }
    val note = dev.himma.core.releaseNotes.lastOrNull()
    val summary = weekSummary(store.week, s.waterGoalMl, weightTrend(store.weights, store.today, 7).changeKg)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = tabPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (store.dataProblems.isNotEmpty()) item { DataProblemsCard(store.dataProblems, store::dismissDataProblems) }
        item {
            Glow(Modifier.clip(RoundedCornerShape(28.dp))) { Panel {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(tr(dev.himma.core.greeting(store.now.hour)), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                        Text(prettyDate(store.today), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (score >= 1f) s(Res.string.all_of_today_s_goals) else s(Res.string.percent_of_goals, "${(score * 100).toInt()}%"), style = MaterialTheme.typography.bodyMedium)
                    }
                    Ring(score, Modifier.size(96.dp)) { Text("${(score * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                }
            } }
        }
        item {
            Panel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    MiniRing(waterP, waterText(store.waterMl), s(Res.string.water)) { nav.go(Tab.Track, TrackPage.Water) }
                    MiniRing(kcalP, "${totals.kcal}", s(Res.string.calories)) { nav.go(Tab.Meals, null) }
                    MiniRing(stepP, if (stepsReady) "${store.stepsToday}" else "-", s(Res.string.steps)) { nav.go(Tab.Track, TrackPage.Steps) }
                }
            }
        }
        item {
            Panel(Modifier.clickable { nav.go(Tab.Meals, null) }) {
                SectionTitle(Icons.Rounded.Restaurant, s(Res.string.calories), if (left >= 0) s(Res.string.left, left) else s(Res.string.n_over, -left))
                MacroBar(totals.protein, totals.carbs, totals.fat)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Legend(MaterialTheme.colorScheme.primary, s(Res.string.protein_g, totals.protein))
                    Legend(MaterialTheme.colorScheme.secondary, s(Res.string.carbs_g, totals.carbs))
                    Legend(MaterialTheme.colorScheme.tertiary, s(Res.string.fat_g, totals.fat))
                }
            }
        }
        item {
            Panel(Modifier.clickable { nav.go(Tab.Track, TrackPage.Fasting) }) {
                SectionTitle(Icons.Rounded.Timer, s(Res.string.fasting, s.fastHours, 24 - s.fastHours), s(Res.string.left, durationText(fast.minutesLeft)))
                Text(if (fast.eating) s(Res.string.eating_window_is_open) else s(Res.string.fasting_now), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                val total = if (fast.eating) (24 - s.fastHours) * 60 else s.fastHours * 60
                LinearProgressIndicator(progress = { 1f - fast.minutesLeft.toFloat() / total }, Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), trackColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            }
        }
        item {
            val dates = store.week.map { it.date }
            Panel {
                SectionTitle(Icons.Rounded.LocalFireDepartment, s(Res.string.last_days))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            Panel(Modifier.clickable { nav.go(Tab.Medicine, null) }) {
                SectionTitle(Icons.Rounded.Medication, s(Res.string.medicine), if (doses.isEmpty()) "" else s(Res.string.n_of_m_taken, taken, doses.size))
                Text(
                    when {
                        store.medicines.isEmpty() -> s(Res.string.no_medicines_added_yet)
                        doses.isEmpty() -> s(Res.string.no_doses_today)
                        next != null -> s(Res.string.next_at, next.medicine.name, next.time)
                        else -> s(Res.string.no_more_doses_today)
                    }, style = MaterialTheme.typography.titleMedium)
            }
        }
        item {
            val lastWorkout = store.workouts.lastOrNull()
            val suggestion = store.routines.firstOrNull()
            Panel(Modifier.clickable { nav.go(Tab.Gym, null) }) {
                SectionTitle(Icons.Rounded.FitnessCenter, s(Res.string.workout), if (todayWorkouts.isNotEmpty()) s(Res.string.done_today) else "")
                when {
                    todayWorkouts.isNotEmpty() -> Text(sp(Res.plurals.workouts_kg_lifted, todayWorkouts.size, volume(todayWorkouts.flatMap { it.sets }).toInt()), style = MaterialTheme.typography.titleMedium)
                    suggestion != null -> {
                        Text(s(Res.string.up_next, suggestion.name), style = MaterialTheme.typography.titleMedium)
                        if (lastWorkout != null) Text(s(Res.string.last_workout, lastWorkout.date), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    else -> Text(s(Res.string.create_a_routine_to_start), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        item {
            Panel {
                SectionTitle(Icons.Rounded.MonitorWeight, s(Res.string.this_week))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Stat("${summary.avgKcal}", s(Res.string.kcal_a_day))
                    Stat(s(Res.string.of, summary.waterGoalDays), s(Res.string.water_goals))
                    Stat("${summary.workouts}", s(Res.string.workouts_label))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Stat(if (summary.avgSteps > 0) "${summary.avgSteps}" else "-", s(Res.string.steps_a_day))
                    Stat(s(Res.string.kg_aae5, summary.volumeKg), s(Res.string.lifted))
                    Stat(summary.weightChangeKg?.let { (if (it > 0) "+" else "") + weightText(it, WeightUnit.fromCode(store.settings.weightUnit)) } ?: "-", s(Res.string.weight))
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = nav.logMeal, label = { Text(s(Res.string.meal)) }, leadingIcon = { Icon(Icons.Rounded.Restaurant, null, Modifier.size(18.dp)) })
                AssistChip(onClick = { store.addWater(250) }, label = { Text(s(Res.string.ml)) }, leadingIcon = { Icon(Icons.Rounded.WaterDrop, null, Modifier.size(18.dp)) })
                AssistChip(onClick = { nav.go(Tab.Track, TrackPage.Weight) }, label = { Text(s(Res.string.weight)) }, leadingIcon = { Icon(Icons.Rounded.MonitorWeight, null, Modifier.size(18.dp)) })
                AssistChip(onClick = { nav.startWorkout(store.routines.firstOrNull()) }, label = { Text(s(Res.string.workout)) }, leadingIcon = { Icon(Icons.Rounded.FitnessCenter, null, Modifier.size(18.dp)) })
            }
        }
        if (note != null) item {
            Panel {
                Text(s(Res.string.what_s_new_in, note.version), style = MaterialTheme.typography.titleSmall)
                note.bullets.forEach { Text("•  $it", style = MaterialTheme.typography.bodyMedium) }
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
