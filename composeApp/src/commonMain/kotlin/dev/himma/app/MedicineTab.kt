package dev.himma.app

import kotlinx.datetime.DayOfWeek

import dev.himma.app.res.n_of_m_taken

import dev.himma.app.res.missed
import dev.himma.app.res.skipped
import dev.himma.app.res.upcoming

import dev.himma.app.res.Res
import dev.himma.app.res.delete_this_medicine
import dev.himma.app.res.medicine_will_be_removed
import dev.himma.app.res.not_medical_advice
import dev.himma.app.res.add_a_medicine
import dev.himma.app.res.cancel
import dev.himma.app.res.days_none_selected_means_every
import dev.himma.app.res.delete
import dev.himma.app.res.dose_optional_for_example_tablet
import dev.himma.app.res.edit_medicine
import dev.himma.app.res.last_days_doses_taken
import dev.himma.app.res.name
import dev.himma.app.res.no_doses_today
import dev.himma.app.res.no_medicines_yet
import dev.himma.app.res.save
import dev.himma.app.res.skip
import dev.himma.app.res.taken
import dev.himma.app.res.tap_to_add_one_with
import dev.himma.app.res.times_hh_mm_separated_by
import dev.himma.app.res.today
import dev.himma.app.res.undo_1cdc
import dev.himma.app.res.your_medicines

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.himma.core.Dose
import dev.himma.core.DoseMark
import dev.himma.core.DoseStatus
import dev.himma.core.Medicine
import dev.himma.core.adherence
import dev.himma.core.doseStatus
import dev.himma.core.formatDays
import dev.himma.core.parseTimes
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus


@Composable
fun MedicineTab(store: HimmaStore, onEdit: (Medicine) -> Unit) {
    val doses = store.todayDoses
    val week = adherence(store.medicines, store.doseMarks, store.today.minus(DatePeriod(days = 6)), store.today, store.now)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = tabPadding, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (store.dataProblems.isNotEmpty()) item { DataProblemsCard(store.dataProblems, store::dismissDataProblems) }
        if (store.reminderProblems.isNotEmpty()) item { RemindersCard(store.reminderProblems, store.platform::openNotificationSettings, store.platform::openExactAlarmSettings) }
        item {
            Panel {
                Text(s(Res.string.today), style = MaterialTheme.typography.titleSmall)
                Text(if (doses.isEmpty()) s(Res.string.no_doses_today) else s(Res.string.n_of_m_taken, doses.count { doseStatus(it, store.doseMarks, store.now) == DoseStatus.Taken }, doses.size), style = MaterialTheme.typography.titleLarge)
                week.rate?.let { Text(spq(Res.plurals.last_days_doses_taken, week.due, week.taken, week.due), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        items(doses, key = { it.key }) { d -> DoseRow(store, d) }
        item { Text(s(Res.string.your_medicines), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp)) }
        if (store.medicines.isEmpty()) item { EmptyHint(s(Res.string.no_medicines_yet), s(Res.string.tap_to_add_one_with)) }
        items(store.medicines, key = { "m" + it.id }) { m ->
            Column(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = androidx.compose.ui.semantics.Role.Button) { onEdit(m) }.padding(vertical = 6.dp), verticalArrangement = Arrangement.Center) {
                Text(m.name + if (m.dose.isNotBlank()) "  ·  ${m.dose}" else "", style = MaterialTheme.typography.bodyLarge)
                Text(num("${m.times.joinToString(", ")}") + "  ·  ${daysLabel(m.days)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DoseRow(store: HimmaStore, d: Dose) {
    val status = doseStatus(d, store.doseMarks, store.now)
    Panel {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("${num(d.time)}  ${d.medicine.name}", style = MaterialTheme.typography.titleMedium)
                if (d.medicine.dose.isNotBlank()) Text(d.medicine.dose, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(when (status) { DoseStatus.Taken -> s(Res.string.taken); DoseStatus.Skipped -> s(Res.string.skipped); DoseStatus.Missed -> s(Res.string.missed); DoseStatus.Upcoming -> s(Res.string.upcoming) },
                style = MaterialTheme.typography.labelLarge, color = if (status == DoseStatus.Missed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (status == DoseStatus.Taken || status == DoseStatus.Skipped) OutlinedButton(onClick = { store.markDose(d, null) }) { Text(s(Res.string.undo_1cdc)) }
            else {
                OutlinedButton(onClick = { store.markDose(d, DoseMark.Taken) }) { Text(s(Res.string.taken)) }
                OutlinedButton(onClick = { store.markDose(d, DoseMark.Skipped) }) { Text(s(Res.string.skip)) }
            }
        }
    }
}

@Composable
fun MedicineDialog(existing: Medicine?, onDismiss: () -> Unit, onDelete: (() -> Unit)?, onSave: (Medicine) -> Unit) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var dose by remember { mutableStateOf(existing?.dose ?: "") }
    var times by remember { mutableStateOf(existing?.times?.joinToString(", ") ?: "08:00") }
    var days by remember { mutableStateOf(existing?.days ?: emptySet()) }
    val parsed = parseTimes(times)
    var confirmDelete by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) s(Res.string.add_a_medicine) else s(Res.string.edit_medicine)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, singleLine = true, label = { Text(s(Res.string.name)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(dose, { dose = it }, singleLine = true, label = { Text(s(Res.string.dose_optional_for_example_tablet)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(times, { times = it }, singleLine = true, isError = parsed == null, label = { Text(s(Res.string.times_hh_mm_separated_by)) }, modifier = Modifier.fillMaxWidth())
                Text(s(Res.string.not_medical_advice), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(s(Res.string.days_none_selected_means_every), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items((1..7).toList()) { n ->
                        FilterChip(n in days, { days = if (n in days) days - n else days + n }, { Text(dayName(DayOfWeek(n))) })
                    }
                }
            }
        },
        confirmButton = { TextButton(enabled = name.isNotBlank() && parsed != null, onClick = { onSave(Medicine(existing?.id ?: "", name.trim(), dose.trim(), parsed!!, if (days.size == 7) emptySet() else days)) }) { Text(s(Res.string.save)) } },
        dismissButton = {
            Row {
                onDelete?.let { TextButton(onClick = { confirmDelete = true }) { Text(s(Res.string.delete), color = MaterialTheme.colorScheme.error) } }
                TextButton(onClick = onDismiss) { Text(s(Res.string.cancel)) }
            }
        },
    )
    if (confirmDelete && existing != null) ConfirmDelete(s(Res.string.delete_this_medicine), s(Res.string.medicine_will_be_removed, existing.name), { confirmDelete = false; onDelete?.invoke() }, { confirmDelete = false })
}
