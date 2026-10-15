package dev.himma.app

import dev.himma.app.res.arabic_digits

import dev.himma.app.res.language

import dev.himma.app.res.remove_x

import dev.himma.app.res.dark
import dev.himma.app.res.eating
import dev.himma.app.res.fasting_label
import dev.himma.app.res.gym
import dev.himma.app.res.home
import dev.himma.app.res.left_label
import dev.himma.app.res.light
import dev.himma.app.res.meals
import dev.himma.app.res.medicine
import dev.himma.app.res.menu
import dev.himma.app.res.over
import dev.himma.app.res.steps
import dev.himma.app.res.system
import dev.himma.app.res.today
import dev.himma.app.res.track
import dev.himma.app.res.water
import dev.himma.app.res.weight

import dev.himma.app.res.Res
import dev.himma.app.res.about
import dev.himma.app.res.add
import dev.himma.app.res.add_a_medicine
import dev.himma.app.res.add_water
import dev.himma.app.res.allow_all_files_access
import dev.himma.app.res.calories
import dev.himma.app.res.cancel
import dev.himma.app.res.carbs_g_ce6a
import dev.himma.app.res.custom
import dev.himma.app.res.custom_amount
import dev.himma.app.res.daily_calorie_goal
import dev.himma.app.res.daily_step_goal
import dev.himma.app.res.daily_water_goal_ml
import dev.himma.app.res.data_folder
import dev.himma.app.res.delete
import dev.himma.app.res.done
import dev.himma.app.res.eat_from_hh_mm
import dev.himma.app.res.eating_to
import dev.himma.app.res.edit_meal
import dev.himma.app.res.edit_saved_meals
import dev.himma.app.res.fast_hours
import dev.himma.app.res.fat_g_e7c0
import dev.himma.app.res.himma
import dev.himma.app.res.kcal
import dev.himma.app.res.kcal_06b1
import dev.himma.app.res.keep_it
import dev.himma.app.res.left
import dev.himma.app.res.log_a_meal
import dev.himma.app.res.log_again
import dev.himma.app.res.millilitres
import dev.himma.app.res.ml_glass
import dev.himma.app.res.name
import dev.himma.app.res.next_day
import dev.himma.app.res.no_meals_logged
import dev.himma.app.res.nothing_saved_open_a_meal
import dev.himma.app.res.of_fcd7
import dev.himma.app.res.previous_day
import dev.himma.app.res.protein_g_29f5
import dev.himma.app.res.protein_g_carbs_g_fat
import dev.himma.app.res.remove
import dev.himma.app.res.remove_the_old_copy
import dev.himma.app.res.remove_the_old_copy_495b
import dev.himma.app.res.save
import dev.himma.app.res.save_as_meal
import dev.himma.app.res.saved_meals
import dev.himma.app.res.settings
import dev.himma.app.res.delete_this_meal
import dev.himma.app.res.meal_will_be_removed
import dev.himma.app.res.show_medicine_names_on_the_lock_screen
import dev.himma.app.res.show_the_tour_again
import dev.himma.app.res.tap_to_add_one_each
import dev.himma.app.res.theme
import dev.himma.app.res.time_hh_mm
import dev.himma.app.res.total
import dev.himma.app.res.undo
import dev.himma.app.res.your_files_are_safe_in

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import org.jetbrains.compose.resources.StringResource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.himma.core.Meal
import dev.himma.core.Settings
import dev.himma.core.caloriesLeft
import dev.himma.core.dayTotals
import dev.himma.core.fastState
import dev.himma.core.formatClock
import dev.himma.core.formatDuration
import dev.himma.core.formatWater
import dev.himma.core.parseClock
import dev.himma.core.waterProgress

internal enum class Tab(val label: StringResource, val icon: ImageVector) {
    Home(Res.string.home, Icons.Rounded.Home),
    Meals(Res.string.meals, Icons.Rounded.Restaurant),
    Gym(Res.string.gym, Icons.Rounded.FitnessCenter),
    Track(Res.string.track, Icons.Rounded.MonitorHeart),
    Medicine(Res.string.medicine, Icons.Rounded.Medication),
}

/** The pages inside the Track tab. */
internal enum class TrackPage(val label: StringResource) { Water(Res.string.water), Fasting(Res.string.fasting_label), Steps(Res.string.steps), Weight(Res.string.weight) }

/** A workout about to start: from a routine, or empty. */
class WorkoutStart(val routine: dev.himma.core.Routine?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(store: HimmaStore, onAbout: () -> Unit, onTour: () -> Unit) {
    var tab by remember { mutableStateOf(Tab.Home) }
    var track by remember { mutableStateOf(TrackPage.Water) }
    var workout by remember { mutableStateOf<WorkoutStart?>(null) }
    var menu by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Meal?>(null) }
    var adding by remember { mutableStateOf(false) }
    var editingMedicine by remember { mutableStateOf<dev.himma.core.Medicine?>(null) }
    var addingMedicine by remember { mutableStateOf(false) }
    var templatesDialog by remember { mutableStateOf(false) }
    workout?.let { w -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()) { WorkoutScreen(store, w.routine) { workout = null } }; return }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text(s(Res.string.himma), fontWeight = FontWeight.SemiBold) }, colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background), actions = {
                IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, s(Res.string.menu)) }
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem(text = { Text(s(Res.string.settings)) }, onClick = { menu = false; settings = true })
                    DropdownMenuItem(text = { Text(s(Res.string.about)) }, onClick = { menu = false; onAbout() })
                }
            })
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 0.dp) {
                Tab.entries.forEach { t -> NavigationBarItem(tab == t, { tab = t }, { Icon(t.icon, s(t.label)) }, label = { Text(s(t.label)) }) }
            }
        },
        floatingActionButton = {
            if (tab == Tab.Meals) FloatingActionButton(onClick = { adding = true }) { Icon(Icons.Rounded.Add, s(Res.string.log_a_meal)) }
            if (tab == Tab.Medicine) FloatingActionButton(onClick = { addingMedicine = true }) { Icon(Icons.Rounded.Add, s(Res.string.add_a_medicine)) }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                Tab.Home -> HomeTab(store, Nav(go = { t, p -> tab = t; if (p != null) track = p }, logMeal = { tab = Tab.Meals; adding = true }, startWorkout = { workout = WorkoutStart(it) }))
                Tab.Meals -> MealsTab(store, onTemplates = { templatesDialog = true }) { editing = it }
                Tab.Gym -> GymTab(store) { workout = WorkoutStart(it) }
                Tab.Track -> TrackTab(store, track) { track = it }
                Tab.Medicine -> MedicineTab(store) { editingMedicine = it }
            }
        }
    }
    if (addingMedicine || editingMedicine != null) MedicineDialog(editingMedicine, onDismiss = { addingMedicine = false; editingMedicine = null },
        onDelete = editingMedicine?.let { m -> { store.deleteMedicine(m.id); editingMedicine = null } }) { store.saveMedicine(it); addingMedicine = false; editingMedicine = null }
    if (templatesDialog) TemplatesDialog(store) { templatesDialog = false }
    if (settings) SettingsDialog(store, { settings = false }, onTour = { settings = false; onTour() }) { store.saveSettings(it); settings = false }
    if (adding || editing != null) MealDialog(editing, store.nowTime(), onDismiss = { adding = false; editing = null },
        onDelete = editing?.let { m -> { store.deleteMeal(m.id); editing = null } }, onTemplate = { store.saveTemplate(dev.himma.core.MealTemplate("", it.name, it.kcal, it.protein, it.carbs, it.fat)) }) { store.saveMeal(it); adding = false; editing = null }
}

internal val tabPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)

/** A ring that fills as [progress] goes 0 to 1, with [content] in the middle. */
@Composable
internal fun Ring(progress: Float, modifier: Modifier = Modifier.size(180.dp), content: @Composable () -> Unit) {
    val track = MaterialTheme.colorScheme.surfaceContainerHigh
    val fill = MaterialTheme.colorScheme.primary
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.minDimension * 0.09f
            val inset = w / 2
            val box = Size(size.width - w, size.height - w)
            drawArc(track, 0f, 360f, false, Offset(inset, inset), box, style = Stroke(w))
            if (progress > 0f) drawArc(fill, -90f, 360f * progress, false, Offset(inset, inset), box, style = Stroke(w, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) { content() }
    }
}

@Composable
private fun TrackTab(store: HimmaStore, page: TrackPage, onPage: (TrackPage) -> Unit) {
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
            }
        }
    }
}

@Composable
private fun MealsTab(store: HimmaStore, onTemplates: () -> Unit, onEdit: (Meal) -> Unit) {
    val totals = dayTotals(store.meals)
    val left = caloriesLeft(store.settings.calorieGoal, totals)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = tabPadding, verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                Text(s(Res.string.protein_g_carbs_g_fat, totals.protein, totals.carbs, totals.fat), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (store.templates.isNotEmpty() || store.recent.isNotEmpty()) item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(s(Res.string.log_again), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(store.templates, key = { "t" + it.id }) { t -> AssistChip(onClick = { store.logAgain(t.name, t.kcal, t.protein, t.carbs, t.fat) }, label = { Text("${t.name} · ${t.kcal}") }) }
                    items(store.recent.filter { r -> store.templates.none { it.name.equals(r.name, true) && it.kcal == r.kcal } }, key = { "r" + it.id }) { m ->
                        AssistChip(onClick = { store.logAgain(m.name, m.kcal, m.protein, m.carbs, m.fat) }, label = { Text("${m.name} · ${m.kcal}") })
                    }
                }
                if (store.templates.isNotEmpty()) TextButton(onClick = onTemplates) { Text(s(Res.string.edit_saved_meals)) }
            }
        }
        if (store.meals.isEmpty()) item { EmptyHint(s(Res.string.no_meals_logged), s(Res.string.tap_to_add_one_each)) }
        items(store.meals, key = { it.id }) { m ->
            Row(Modifier.fillMaxWidth().clickable { onEdit(m) }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(m.name, style = MaterialTheme.typography.bodyLarge)
                    val macros = listOfNotNull(m.protein?.let { "P $it" }, m.carbs?.let { "C $it" }, m.fat?.let { "F $it" }).joinToString("  ")
                    Text(m.time + if (macros.isNotEmpty()) "  ·  $macros g" else "", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Text(s(Res.string.kcal, m.kcal), style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun WaterTab(store: HimmaStore) {
    var custom by remember { mutableStateOf(false) }
    val goal = store.settings.waterGoalMl
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Ring(waterProgress(store.waterMl, goal), Modifier.size(220.dp)) {
            Text(formatWater(store.waterMl), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text(s(Res.string.of_fcd7, formatWater(goal)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun FastingTab(store: HimmaStore) {
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
            Text(s(Res.string.left, formatDuration(fast.minutesLeft)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(s(Res.string.eating_to, formatClock(s.eatStartMinutes), formatClock(eatEnd)), style = MaterialTheme.typography.bodyLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(presets) { h -> FilterChip(s.fastHours == h, { store.saveSettings(s.copy(fastHours = h)) }, { Text("$h:${24 - h}") }) }
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

@Composable
private fun MealDialog(existing: Meal?, nowTime: String, onDismiss: () -> Unit, onDelete: (() -> Unit)?, onTemplate: (Meal) -> Unit, onSave: (Meal) -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var kcal by remember { mutableStateOf(existing?.kcal?.toString() ?: "") }
    var protein by remember { mutableStateOf(existing?.protein?.toString() ?: "") }
    var carbs by remember { mutableStateOf(existing?.carbs?.toString() ?: "") }
    var fat by remember { mutableStateOf(existing?.fat?.toString() ?: "") }
    var time by remember { mutableStateOf(existing?.time ?: nowTime) }
    val kcalValue = kcal.trim().toIntOrNull()?.takeIf { it >= 0 }
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
private fun SettingsDialog(store: HimmaStore, onDismiss: () -> Unit, onTour: () -> Unit, onSave: (Settings) -> Unit) {
    val current = store.settings
    var kcal by remember { mutableStateOf(current.calorieGoal.toString()) }
    var water by remember { mutableStateOf(current.waterGoalMl.toString()) }
    var confirmRemove by remember { mutableStateOf(false) }
    var steps by remember { mutableStateOf(current.stepGoal.toString()) }
    var theme by remember { mutableStateOf(current.themeMode) }
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
                TextButton(onClick = onTour) { Text(s(Res.string.show_the_tour_again)) }
            }
        },
        confirmButton = { TextButton(enabled = kcalValue != null && waterValue != null && stepsValue != null, onClick = { onSave(current.copy(calorieGoal = kcalValue!!, waterGoalMl = waterValue!!, stepGoal = stepsValue!!, themeMode = theme, lockScreenNames = lockNames)) }) { Text(s(Res.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s(Res.string.cancel)) } },
    )
    if (confirmRemove) AlertDialog(
        onDismissRequest = { confirmRemove = false },
        title = { Text(s(Res.string.remove_the_old_copy_495b)) },
        text = { Text(s(Res.string.your_files_are_safe_in, store.folderPath)) },
        confirmButton = { TextButton(onClick = { store.removeOldCopy(); confirmRemove = false }) { Text(s(Res.string.remove), color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text(s(Res.string.keep_it)) } },
    )
}

@Composable
private fun TemplatesDialog(store: HimmaStore, onDismiss: () -> Unit) {
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
