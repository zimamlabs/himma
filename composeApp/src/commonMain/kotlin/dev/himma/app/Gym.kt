package dev.himma.app

import dev.himma.app.res.n_min_after

import dev.himma.app.res.done
import dev.himma.app.res.workout

import dev.himma.app.res.Res
import dev.himma.app.res.a_routine_lists_your_exercises
import dev.himma.app.res.add
import dev.himma.app.res.add_a_set
import dev.himma.app.res.add_an_exercise
import dev.himma.app.res.cancel
import dev.himma.app.res.delete
import dev.himma.app.res.delete_this_workout
import dev.himma.app.res.discard
import dev.himma.app.res.edit_routine
import dev.himma.app.res.empty_workout
import dev.himma.app.res.exercise
import dev.himma.app.res.finish
import dev.himma.app.res.history
import dev.himma.app.res.keep_going
import dev.himma.app.res.keep_it
import dev.himma.app.res.kg
import dev.himma.app.res.leave_this_workout
import dev.himma.app.res.mark_done
import dev.himma.app.res.name
import dev.himma.app.res.new_personal_best
import dev.himma.app.res.new_routine
import dev.himma.app.res.no_exercises
import dev.himma.app.res.no_routines_yet
import dev.himma.app.res.on_will_be_removed_from
import dev.himma.app.res.personal_bests
import dev.himma.app.res.remove_exercise
import dev.himma.app.res.reps
import dev.himma.app.res.rest
import dev.himma.app.res.rest_timer
import dev.himma.app.res.rm
import dev.himma.app.res.routine_name
import dev.himma.app.res.routines
import dev.himma.app.res.save
import dev.himma.app.res.sets
import dev.himma.app.res.sets_kg_lifted
import dev.himma.app.res.start
import dev.himma.app.res.the_sets_you_ticked_off

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.himma.core.Exercise
import dev.himma.core.Routine
import dev.himma.core.SetEntry
import dev.himma.core.Workout
import dev.himma.core.beatsBest
import dev.himma.core.formatKg
import dev.himma.core.personalBests
import dev.himma.core.plannedSets
import dev.himma.core.volume
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock

@Composable
internal fun GymTab(store: HimmaStore, onStart: (Routine?) -> Unit) {
    var editing by remember { mutableStateOf<Routine?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<Workout?>(null) }
    val bests = remember(store.workouts) { personalBests(store.workouts) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = tabPadding, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onStart(null) }, Modifier.weight(1f)) { Text(s(Res.string.empty_workout)) }
                OutlinedButton(onClick = { creating = true }, Modifier.weight(1f)) { Text(s(Res.string.new_routine)) }
            }
        }
        item { Text(s(Res.string.routines), style = MaterialTheme.typography.titleMedium) }
        if (store.routines.isEmpty()) item { EmptyHint(s(Res.string.no_routines_yet), s(Res.string.a_routine_lists_your_exercises)) }
        items(store.routines, key = { it.id }) { r ->
            Panel(Modifier.clickable { editing = r }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(r.name, style = MaterialTheme.typography.titleMedium)
                        Text(r.exercises.joinToString(", ") { it.name }.ifEmpty { s(Res.string.no_exercises) }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                    }
                    Button(onClick = { onStart(r) }) { Icon(Icons.Rounded.PlayArrow, null, Modifier.size(18.dp)); Text(s(Res.string.start)) }
                }
            }
        }
        if (store.workouts.isNotEmpty()) item { Text(s(Res.string.history), style = MaterialTheme.typography.titleMedium) }
        items(store.workouts.takeLast(12).reversed(), key = { it.id }) { w ->
            Panel(Modifier.clickable { deleting = w }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(w.name, style = MaterialTheme.typography.titleSmall)
                    Text(w.date, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(s(Res.string.sets_kg_lifted, w.sets.size, volume(w.sets).toInt()) + if (w.minutes > 0) s(Res.string.n_min_after, w.minutes) else "", style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (bests.isNotEmpty()) {
            item { Text(s(Res.string.personal_bests), style = MaterialTheme.typography.titleMedium) }
            item {
                Panel {
                    bests.entries.sortedByDescending { it.value.oneRepMaxKg }.take(10).forEach { (name, b) ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.EmojiEvents, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("  " + name.replaceFirstChar { it.uppercase() }, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            Text(s(Res.string.rm, formatKg(b.maxWeightKg), formatKg(b.oneRepMaxKg)), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
    if (creating || editing != null) RoutineDialog(editing, onDismiss = { creating = false; editing = null },
        onDelete = editing?.let { r -> { store.deleteRoutine(r.id); editing = null } }) { store.saveRoutine(it); creating = false; editing = null }
    deleting?.let { w ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text(s(Res.string.delete_this_workout)) }, text = { Text(s(Res.string.on_will_be_removed_from, w.name, w.date)) },
            confirmButton = { TextButton(onClick = { store.deleteWorkout(w); deleting = null }) { Text(s(Res.string.delete), color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text(s(Res.string.keep_it)) } })
    }
}

private class ExerciseRow(name: String, sets: String, reps: String, kg: String) {
    var name by mutableStateOf(name); var sets by mutableStateOf(sets); var reps by mutableStateOf(reps); var kg by mutableStateOf(kg)
}

private val number = KeyboardOptions(keyboardType = KeyboardType.Number)
private val decimal = KeyboardOptions(keyboardType = KeyboardType.Decimal)

private fun kgText(v: Double) = if (v == 0.0) "" else v.toString().removeSuffix(".0")

@Composable
private fun RoutineDialog(existing: Routine?, onDismiss: () -> Unit, onDelete: (() -> Unit)?, onSave: (Routine) -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    val rows = remember { mutableStateListOf<ExerciseRow>().apply { (existing?.exercises ?: listOf(Exercise(""))).forEach { add(ExerciseRow(it.name, it.sets.toString(), it.reps.toString(), kgText(it.weightKg))) } } }
    fun parsed() = rows.filter { it.name.isNotBlank() }.map { Exercise(it.name.trim(), it.sets.toIntOrNull()?.coerceIn(1, 20) ?: 3, it.reps.toIntOrNull()?.coerceIn(1, 100) ?: 10, it.kg.replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) s(Res.string.new_routine) else s(Res.string.edit_routine)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, singleLine = true, label = { Text(s(Res.string.routine_name)) }, modifier = Modifier.fillMaxWidth())
                rows.forEachIndexed { i, r ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(r.name, { r.name = it }, singleLine = true, label = { Text(s(Res.string.exercise, i + 1)) }, modifier = Modifier.weight(1f))
                            IconButton(onClick = { rows.removeAt(i) }) { Icon(Icons.Rounded.Close, s(Res.string.remove_exercise)) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(r.sets, { r.sets = it }, singleLine = true, label = { Text(s(Res.string.sets)) }, keyboardOptions = number, modifier = Modifier.weight(1f))
                            OutlinedTextField(r.reps, { r.reps = it }, singleLine = true, label = { Text(s(Res.string.reps)) }, keyboardOptions = number, modifier = Modifier.weight(1f))
                            OutlinedTextField(r.kg, { r.kg = it }, singleLine = true, label = { Text(s(Res.string.kg)) }, keyboardOptions = decimal, modifier = Modifier.weight(1f))
                        }
                    }
                }
                TextButton(onClick = { rows.add(ExerciseRow("", "3", "10", "")) }) { Text(s(Res.string.add_an_exercise)) }
            }
        },
        confirmButton = { TextButton(enabled = name.isNotBlank() && parsed().isNotEmpty(), onClick = { onSave(Routine(existing?.id ?: "", name.trim(), parsed())) }) { Text(s(Res.string.save)) } },
        dismissButton = { Row { onDelete?.let { TextButton(onClick = it) { Text(s(Res.string.delete), color = MaterialTheme.colorScheme.error) } }; TextButton(onClick = onDismiss) { Text(s(Res.string.cancel)) } } },
    )
}

private class SetUi(val exercise: String, reps: String, kg: String) {
    var reps by mutableStateOf(reps); var kg by mutableStateOf(kg); var done by mutableStateOf(false)
    fun entry() = SetEntry(exercise, reps.toIntOrNull() ?: 0, kg.replace(',', '.').toDoubleOrNull() ?: 0.0)
}

/** A workout in progress: tick sets off as you do them, with a rest timer that starts on each tick. */
@Composable
internal fun WorkoutScreen(store: HimmaStore, routine: Routine?, onClose: () -> Unit) {
    val sets = remember { mutableStateListOf<SetUi>().apply { routine?.let { r -> plannedSets(r).forEach { add(SetUi(it.exercise, it.reps.toString(), kgText(it.weightKg))) } } } }
    val startedAt = remember { Clock.System.now().toEpochMilliseconds() }
    val bests = remember { personalBests(store.workouts) }
    var restLen by remember { mutableStateOf(90) }
    var restLeft by remember { mutableStateOf(0) }
    var restKey by remember { mutableStateOf(0) }
    var addExercise by remember { mutableStateOf(false) }
    var leave by remember { mutableStateOf(false) }
    LaunchedEffect(restKey) {
        if (restKey == 0) return@LaunchedEffect
        restLeft = restLen
        while (restLeft > 0) { delay(1000); restLeft-- }
    }
    val workoutName = s(Res.string.workout)
    val doneSets = sets.filter { it.done && it.entry().reps > 0 }
    PlatformBackHandler(true) { if (doneSets.isEmpty()) onClose() else leave = true }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(routine?.name ?: s(Res.string.workout), Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = { if (doneSets.isEmpty()) onClose() else leave = true }) { Text(s(Res.string.cancel)) }
            Button(enabled = doneSets.isNotEmpty(), onClick = {
                val minutes = ((Clock.System.now().toEpochMilliseconds() - startedAt) / 60_000).toInt()
                store.saveWorkout(Workout("", store.today.toString(), routine?.name ?: workoutName, doneSets.map { it.entry() }, minutes))
                onClose()
            }) { Text(s(Res.string.finish)) }
        }
        Panel {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (restLeft > 0) s(Res.string.rest, restLeft / 60, (restLeft % 60).toString().padStart(2, '0')) else s(Res.string.rest_timer), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(listOf(60, 90, 120, 180)) { s -> FilterChip(restLen == s, { restLen = s; if (restLeft > 0) restLeft = s }, { Text("${s}s") }) }
                }
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            sets.map { it.exercise }.distinct().forEach { ex ->
                Text(ex, Modifier.padding(top = 8.dp), style = MaterialTheme.typography.titleMedium)
                val mine = sets.filter { it.exercise == ex }
                mine.forEachIndexed { i, s ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${i + 1}", Modifier.size(20.dp), style = MaterialTheme.typography.labelLarge)
                        OutlinedTextField(s.reps, { s.reps = it }, singleLine = true, label = { Text(s(Res.string.reps)) }, keyboardOptions = number, modifier = Modifier.weight(1f))
                        OutlinedTextField(s.kg, { s.kg = it }, singleLine = true, label = { Text(s(Res.string.kg)) }, keyboardOptions = decimal, modifier = Modifier.weight(1f))
                        if (s.done && beatsBest(bests, s.entry())) Icon(Icons.Rounded.EmojiEvents, s(Res.string.new_personal_best), tint = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { s.done = !s.done; if (s.done) restKey++ }) {
                            Icon(Icons.Rounded.Check, if (s.done) s(Res.string.done) else s(Res.string.mark_done), tint = if (s.done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
                TextButton(onClick = { val last = mine.last(); sets.add(sets.indexOf(last) + 1, SetUi(ex, last.reps, last.kg)) }) { Text(s(Res.string.add_a_set)) }
            }
            TextButton(onClick = { addExercise = true }) { Text(s(Res.string.add_an_exercise)) }
        }
        if (doneSets.isNotEmpty()) Text(s(Res.string.sets_kg_lifted, doneSets.size, volume(doneSets.map { it.entry() }).toInt()), style = MaterialTheme.typography.titleSmall)
    }
    if (addExercise) {
        var name by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { addExercise = false }, title = { Text(s(Res.string.add_an_exercise)) },
            text = { OutlinedTextField(name, { name = it }, singleLine = true, label = { Text(s(Res.string.name)) }, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { sets.add(SetUi(name.trim(), "10", "")); addExercise = false }) { Text(s(Res.string.add)) } },
            dismissButton = { TextButton(onClick = { addExercise = false }) { Text(s(Res.string.cancel)) } })
    }
    if (leave) AlertDialog(onDismissRequest = { leave = false }, title = { Text(s(Res.string.leave_this_workout)) }, text = { Text(s(Res.string.the_sets_you_ticked_off, doneSets.size)) },
        confirmButton = { TextButton(onClick = { leave = false; onClose() }) { Text(s(Res.string.discard), color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = { leave = false }) { Text(s(Res.string.keep_going)) } })
}
