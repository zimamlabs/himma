package dev.himma.app

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
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
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

private enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Rounded.Home),
    Meals("Meals", Icons.Rounded.Restaurant),
    Water("Water", Icons.Rounded.WaterDrop),
    Fasting("Fasting", Icons.Rounded.Timer),
    Medicine("Medicine", Icons.Rounded.Medication),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(store: HimmaStore, onAbout: () -> Unit, onTour: () -> Unit) {
    var tab by remember { mutableStateOf(Tab.Home) }
    var menu by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Meal?>(null) }
    var adding by remember { mutableStateOf(false) }
    var editingMedicine by remember { mutableStateOf<dev.himma.core.Medicine?>(null) }
    var addingMedicine by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(title = { Text("Himma") }, actions = {
                IconButton(onClick = { menu = true }) { Icon(Icons.Rounded.MoreVert, "Menu") }
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem(text = { Text("Settings") }, onClick = { menu = false; settings = true })
                    DropdownMenuItem(text = { Text("About") }, onClick = { menu = false; onAbout() })
                }
            })
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { t -> NavigationBarItem(tab == t, { tab = t }, { Icon(t.icon, t.label) }, label = { Text(t.label) }) }
            }
        },
        floatingActionButton = {
            if (tab == Tab.Meals) FloatingActionButton(onClick = { adding = true }) { Icon(Icons.Rounded.Add, "Log a meal") }
            if (tab == Tab.Medicine) FloatingActionButton(onClick = { addingMedicine = true }) { Icon(Icons.Rounded.Add, "Add a medicine") }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (tab) {
                Tab.Home -> HomeTab(store, onGo = { tab = it }, onLogMeal = { tab = Tab.Meals; adding = true })
                Tab.Meals -> MealsTab(store) { editing = it }
                Tab.Water -> WaterTab(store)
                Tab.Fasting -> FastingTab(store)
                Tab.Medicine -> MedicineTab(store) { editingMedicine = it }
            }
        }
    }
    if (addingMedicine || editingMedicine != null) MedicineDialog(editingMedicine, onDismiss = { addingMedicine = false; editingMedicine = null },
        onDelete = editingMedicine?.let { m -> { store.deleteMedicine(m.id); editingMedicine = null } }) { store.saveMedicine(it); addingMedicine = false; editingMedicine = null }
    if (settings) SettingsDialog(store, { settings = false }, onTour = { settings = false; onTour() }) { store.saveSettings(it); settings = false }
    if (adding || editing != null) MealDialog(editing, store.nowTime(), onDismiss = { adding = false; editing = null },
        onDelete = editing?.let { m -> { store.deleteMeal(m.id); editing = null } }) { store.saveMeal(it); adding = false; editing = null }
}

private val tabPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)

/** A ring that fills as [progress] goes 0 to 1, with [content] in the middle. */
@Composable
private fun Ring(progress: Float, modifier: Modifier = Modifier.size(180.dp), content: @Composable () -> Unit) {
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
private fun HomeTab(store: HimmaStore, onGo: (Tab) -> Unit, onLogMeal: () -> Unit) {
    val s = store.settings
    val totals = dayTotals(store.todayMeals)
    val left = caloriesLeft(s.calorieGoal, totals)
    val fast = fastState(s.eatStartMinutes, s.fastHours, store.nowMinutes)
    val note = dev.himma.core.releaseNotes.lastOrNull()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = tabPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(dev.himma.core.greeting(store.now.hour), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text(prettyDate(store.today), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Panel(Modifier.clickable { onGo(Tab.Meals) }) {
                Text("Calories", style = MaterialTheme.typography.titleSmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { Text("Eaten", style = MaterialTheme.typography.labelMedium); Text("${totals.kcal}", style = MaterialTheme.typography.titleLarge) }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(if (left >= 0) "Left" else "Over", style = MaterialTheme.typography.labelMedium)
                        Text("${kotlin.math.abs(left)}", style = MaterialTheme.typography.titleLarge, color = if (left >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
                    }
                }
                LinearProgressIndicator(progress = { (totals.kcal.toFloat() / s.calorieGoal.coerceAtLeast(1)).coerceIn(0f, 1f) }, Modifier.fillMaxWidth().height(6.dp), trackColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            }
        }
        item {
            Panel(Modifier.clickable { onGo(Tab.Water) }) {
                Text("Water", style = MaterialTheme.typography.titleSmall)
                Text("${formatWater(store.waterMl)} of ${formatWater(s.waterGoalMl)}", style = MaterialTheme.typography.titleLarge)
                LinearProgressIndicator(progress = { waterProgress(store.waterMl, s.waterGoalMl) }, Modifier.fillMaxWidth().height(6.dp), trackColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            }
        }
        item {
            Panel(Modifier.clickable { onGo(Tab.Fasting) }) {
                Text("Fasting ${s.fastHours}:${24 - s.fastHours}", style = MaterialTheme.typography.titleSmall)
                Text(if (fast.eating) "Eating now" else "Fasting now", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Text("${formatDuration(fast.minutesLeft)} left", style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            val doses = store.todayDoses
            val next = doses.firstOrNull { it.at > store.now }
            Panel(Modifier.clickable { onGo(Tab.Medicine) }) {
                Text("Medicine", style = MaterialTheme.typography.titleSmall)
                when {
                    store.medicines.isEmpty() -> Text("No medicines added", style = MaterialTheme.typography.titleLarge)
                    doses.isEmpty() -> Text("No doses today", style = MaterialTheme.typography.titleLarge)
                    else -> {
                        Text("${doses.count { store.doseMarks[it.key] == dev.himma.core.DoseMark.Taken }} of ${doses.size} taken", style = MaterialTheme.typography.titleLarge)
                        Text(if (next != null) "Next: ${next.medicine.name} at ${next.time}" else "No more doses today", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onLogMeal, Modifier.weight(1f)) { Text("Log a meal") }
                OutlinedButton(onClick = { store.addWater(250) }, Modifier.weight(1f)) { Text("+ 250 ml") }
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
private fun MealsTab(store: HimmaStore, onEdit: (Meal) -> Unit) {
    val totals = dayTotals(store.meals)
    val left = caloriesLeft(store.settings.calorieGoal, totals)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = tabPadding, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { store.shiftMealDay(-1) }) { Icon(Icons.Rounded.ChevronLeft, "Previous day") }
                Text(if (store.mealDay == store.today) "Today" else prettyDate(store.mealDay), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { store.shiftMealDay(1) }) { Icon(Icons.Rounded.ChevronRight, "Next day") }
            }
        }
        item {
            Panel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { Text("Total", style = MaterialTheme.typography.labelMedium); Text("${totals.kcal} kcal", style = MaterialTheme.typography.titleLarge) }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(if (left >= 0) "Left" else "Over", style = MaterialTheme.typography.labelMedium)
                        Text("${kotlin.math.abs(left)} kcal", style = MaterialTheme.typography.titleLarge, color = if (left >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
                    }
                }
                Text("Protein ${totals.protein} g  ·  Carbs ${totals.carbs} g  ·  Fat ${totals.fat} g", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (store.meals.isEmpty()) item { EmptyHint("No meals logged", "Tap + to add one. Each day is saved in meals/<year>/<date>.csv.") }
        items(store.meals, key = { it.id }) { m ->
            Row(Modifier.fillMaxWidth().clickable { onEdit(m) }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(m.name, style = MaterialTheme.typography.bodyLarge)
                    val macros = listOfNotNull(m.protein?.let { "P $it" }, m.carbs?.let { "C $it" }, m.fat?.let { "F $it" }).joinToString("  ")
                    Text(m.time + if (macros.isNotEmpty()) "  ·  $macros g" else "", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Text("${m.kcal} kcal", style = MaterialTheme.typography.titleSmall)
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
            Text("of ${formatWater(goal)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(onClick = { store.addWater(250) }, Modifier.fillMaxWidth()) { Text("+ 250 ml glass") }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { custom = true }) { Text("Custom amount") }
            OutlinedButton(onClick = { store.undoWater() }, enabled = store.canUndoWater) {
                Icon(Icons.AutoMirrored.Rounded.Undo, null, Modifier.size(18.dp)); Text("  Undo")
            }
        }
    }
    if (custom) {
        var text by remember { mutableStateOf("") }
        val ml = text.trim().toIntOrNull()?.takeIf { it in 1..5000 }
        AlertDialog(
            onDismissRequest = { custom = false },
            title = { Text("Add water") },
            text = { OutlinedTextField(text, { text = it }, singleLine = true, label = { Text("Millilitres") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(enabled = ml != null, onClick = { store.addWater(ml!!); custom = false }) { Text("Add") } },
            dismissButton = { TextButton(onClick = { custom = false }) { Text("Cancel") } },
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
            Text(if (fast.eating) "Eating" else "Fasting", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text("${formatDuration(fast.minutesLeft)} left", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("Eating ${formatClock(s.eatStartMinutes)} to ${formatClock(eatEnd)}", style = MaterialTheme.typography.bodyLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(presets) { h -> FilterChip(s.fastHours == h, { store.saveSettings(s.copy(fastHours = h)) }, { Text("$h:${24 - h}") }) }
            item { FilterChip(s.fastHours !in presets, { customHours.toIntOrNull()?.takeIf { it in 1..23 }?.let { store.saveSettings(s.copy(fastHours = it)) } }, { Text("Custom") }) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(customHours, { customHours = it; it.toIntOrNull()?.takeIf { h -> h in 1..23 }?.let { h -> store.saveSettings(s.copy(fastHours = h)) } }, singleLine = true, isError = customHours.toIntOrNull()?.let { it in 1..23 } != true,
                label = { Text("Fast hours") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            OutlinedTextField(startText, { startText = it; parseClock(it)?.let { m -> store.saveSettings(s.copy(eatStartMinutes = m)) } }, singleLine = true, isError = start == null,
                label = { Text("Eat from (HH:MM)") }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MealDialog(existing: Meal?, nowTime: String, onDismiss: () -> Unit, onDelete: (() -> Unit)?, onSave: (Meal) -> Unit) {
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Log a meal" else "Edit meal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, singleLine = true, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(kcal, { kcal = it }, singleLine = true, isError = kcalValue == null, label = { Text("Calories") }, keyboardOptions = number, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(protein, { protein = it }, singleLine = true, isError = !pOk, label = { Text("Protein g") }, keyboardOptions = number, modifier = Modifier.weight(1f))
                    OutlinedTextField(carbs, { carbs = it }, singleLine = true, isError = !cOk, label = { Text("Carbs g") }, keyboardOptions = number, modifier = Modifier.weight(1f))
                    OutlinedTextField(fat, { fat = it }, singleLine = true, isError = !fOk, label = { Text("Fat g") }, keyboardOptions = number, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(time, { time = it }, singleLine = true, isError = timeValue == null, label = { Text("Time (HH:MM)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank() && kcalValue != null && timeValue != null && pOk && cOk && fOk, onClick = {
                onSave(Meal(existing?.id ?: "", formatClock(timeValue!!), name.trim(), kcalValue!!, p, c, f))
            }) { Text("Save") }
        },
        dismissButton = {
            Row {
                onDelete?.let { TextButton(onClick = it) { Text("Delete", color = MaterialTheme.colorScheme.error) } }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}

@Composable
private fun SettingsDialog(store: HimmaStore, onDismiss: () -> Unit, onTour: () -> Unit, onSave: (Settings) -> Unit) {
    val current = store.settings
    var kcal by remember { mutableStateOf(current.calorieGoal.toString()) }
    var water by remember { mutableStateOf(current.waterGoalMl.toString()) }
    var confirmRemove by remember { mutableStateOf(false) }
    val kcalValue = kcal.trim().toIntOrNull()?.takeIf { it in 1..20000 }
    val waterValue = water.trim().toIntOrNull()?.takeIf { it in 1..20000 }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(kcal, { kcal = it }, singleLine = true, isError = kcalValue == null, label = { Text("Daily calorie goal") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(water, { water = it }, singleLine = true, isError = waterValue == null, label = { Text("Daily water goal (ml)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Text("Data folder", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(store.folderPath, style = MaterialTheme.typography.bodyMedium)
                if (store.platform.canAskAllFiles && !store.sharedFolder) OutlinedButton(onClick = { store.platform.allowAllFiles() }) { Text("Allow All files access") }
                if (store.canRemoveOld) OutlinedButton(onClick = { confirmRemove = true }) { Text("Remove the old copy") }
                TextButton(onClick = onTour) { Text("Show the tour again") }
            }
        },
        confirmButton = { TextButton(enabled = kcalValue != null && waterValue != null, onClick = { onSave(current.copy(calorieGoal = kcalValue!!, waterGoalMl = waterValue!!)) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
    if (confirmRemove) AlertDialog(
        onDismissRequest = { confirmRemove = false },
        title = { Text("Remove the old copy?") },
        text = { Text("Your files are safe in ${store.folderPath}. Himma checked again that every file in the old private folder is there. The old copy will be deleted and cannot be brought back.") },
        confirmButton = { TextButton(onClick = { store.removeOldCopy(); confirmRemove = false }) { Text("Remove", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text("Keep it") } },
    )
}
