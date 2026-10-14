package dev.himma.app

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
import dev.himma.core.DoseMark
import dev.himma.core.Routine
import dev.himma.core.caloriesLeft
import dev.himma.core.dayScore
import dev.himma.core.dayTotals
import dev.himma.core.fastState
import dev.himma.core.formatDuration
import dev.himma.core.formatKg
import dev.himma.core.formatWater
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
        Box(Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Icon(icon, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
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
        item {
            Panel {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(dev.himma.core.greeting(store.now.hour), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                        Text(prettyDate(store.today), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (score >= 1f) "All of today's goals are done." else "${(score * 100).toInt()}% of today's goals", style = MaterialTheme.typography.bodyMedium)
                    }
                    Ring(score, Modifier.size(96.dp)) { Text("${(score * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                }
            }
        }
        item {
            Panel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    MiniRing(waterP, formatWater(store.waterMl), "Water") { nav.go(Tab.Track, TrackPage.Water) }
                    MiniRing(kcalP, "${totals.kcal}", "Calories") { nav.go(Tab.Meals, null) }
                    MiniRing(stepP, if (stepsReady) "${store.stepsToday}" else "-", "Steps") { nav.go(Tab.Track, TrackPage.Steps) }
                }
            }
        }
        item {
            Panel(Modifier.clickable { nav.go(Tab.Meals, null) }) {
                SectionTitle(Icons.Rounded.Restaurant, "Calories", if (left >= 0) "$left left" else "${-left} over")
                MacroBar(totals.protein, totals.carbs, totals.fat)
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Legend(MaterialTheme.colorScheme.primary, "Protein ${totals.protein} g")
                    Legend(MaterialTheme.colorScheme.secondary, "Carbs ${totals.carbs} g")
                    Legend(MaterialTheme.colorScheme.tertiary, "Fat ${totals.fat} g")
                }
            }
        }
        item {
            Panel(Modifier.clickable { nav.go(Tab.Track, TrackPage.Fasting) }) {
                SectionTitle(Icons.Rounded.Timer, "Fasting ${s.fastHours}:${24 - s.fastHours}", "${formatDuration(fast.minutesLeft)} left")
                Text(if (fast.eating) "Eating window is open" else "Fasting now", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                val total = if (fast.eating) (24 - s.fastHours) * 60 else s.fastHours * 60
                LinearProgressIndicator(progress = { 1f - fast.minutesLeft.toFloat() / total }, Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), trackColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            }
        }
        item {
            val dates = store.week.map { it.date }
            Panel {
                SectionTitle(Icons.Rounded.LocalFireDepartment, "Last 7 days")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Water", "Calories", "Steps").forEachIndexed { i, label -> FilterChip(chart == i, { chart = i }, { Text(label) }) }
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
                SectionTitle(Icons.Rounded.Medication, "Medicine", if (doses.isEmpty()) "" else "$taken of ${doses.size} taken")
                Text(
                    when {
                        store.medicines.isEmpty() -> "No medicines added yet"
                        doses.isEmpty() -> "No doses today"
                        next != null -> "Next: ${next.medicine.name} at ${next.time}"
                        else -> "No more doses today"
                    }, style = MaterialTheme.typography.titleMedium)
            }
        }
        item {
            val lastWorkout = store.workouts.lastOrNull()
            val suggestion = store.routines.firstOrNull()
            Panel(Modifier.clickable { nav.go(Tab.Gym, null) }) {
                SectionTitle(Icons.Rounded.FitnessCenter, "Workout", if (todayWorkouts.isNotEmpty()) "Done today" else "")
                when {
                    todayWorkouts.isNotEmpty() -> Text("${todayWorkouts.size} workout${if (todayWorkouts.size > 1) "s" else ""}, ${volume(todayWorkouts.flatMap { it.sets }).toInt()} kg lifted", style = MaterialTheme.typography.titleMedium)
                    suggestion != null -> {
                        Text("Up next: ${suggestion.name}", style = MaterialTheme.typography.titleMedium)
                        if (lastWorkout != null) Text("Last workout ${lastWorkout.date}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    else -> Text("Create a routine to start tracking workouts", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        item {
            Panel {
                SectionTitle(Icons.Rounded.MonitorWeight, "This week")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Stat("${summary.avgKcal}", "kcal a day")
                    Stat("${summary.waterGoalDays} of 7", "water goals")
                    Stat("${summary.workouts}", "workouts")
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Stat(if (summary.avgSteps > 0) "${summary.avgSteps}" else "-", "steps a day")
                    Stat("${summary.volumeKg} kg", "lifted")
                    Stat(summary.weightChangeKg?.let { (if (it > 0) "+" else "") + formatKg(it) } ?: "-", "weight")
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = nav.logMeal, label = { Text("Meal") }, leadingIcon = { Icon(Icons.Rounded.Restaurant, null, Modifier.size(18.dp)) })
                AssistChip(onClick = { store.addWater(250) }, label = { Text("+250 ml") }, leadingIcon = { Icon(Icons.Rounded.WaterDrop, null, Modifier.size(18.dp)) })
                AssistChip(onClick = { nav.go(Tab.Track, TrackPage.Weight) }, label = { Text("Weight") }, leadingIcon = { Icon(Icons.Rounded.MonitorWeight, null, Modifier.size(18.dp)) })
                AssistChip(onClick = { nav.startWorkout(store.routines.firstOrNull()) }, label = { Text("Workout") }, leadingIcon = { Icon(Icons.Rounded.FitnessCenter, null, Modifier.size(18.dp)) })
            }
        }
        if (note != null) item {
            Panel {
                Text("What's new in ${note.version}", style = MaterialTheme.typography.titleSmall)
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
