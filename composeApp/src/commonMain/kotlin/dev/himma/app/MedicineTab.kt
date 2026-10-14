package dev.himma.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
        item {
            Panel {
                Text("Today", style = MaterialTheme.typography.titleSmall)
                Text(if (doses.isEmpty()) "No doses today" else "${doses.count { doseStatus(it, store.doseMarks, store.now) == DoseStatus.Taken }} of ${doses.size} taken", style = MaterialTheme.typography.titleLarge)
                week.rate?.let { Text("Last 7 days: ${week.taken} of ${week.due} doses taken", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        items(doses, key = { it.key }) { d -> DoseRow(store, d) }
        item { Text("Your medicines", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp)) }
        if (store.medicines.isEmpty()) item { EmptyHint("No medicines yet", "Tap + to add one with its times and days. Himma reminds you when a dose is due.") }
        items(store.medicines, key = { "m" + it.id }) { m ->
            Column(Modifier.fillMaxWidth().clickable { onEdit(m) }.padding(vertical = 6.dp)) {
                Text(m.name + if (m.dose.isNotBlank()) "  ·  ${m.dose}" else "", style = MaterialTheme.typography.bodyLarge)
                Text("${m.times.joinToString(", ")}  ·  ${formatDays(m.days)}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Text("${d.time}  ${d.medicine.name}", style = MaterialTheme.typography.titleMedium)
                if (d.medicine.dose.isNotBlank()) Text(d.medicine.dose, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(when (status) { DoseStatus.Taken -> "Taken"; DoseStatus.Skipped -> "Skipped"; DoseStatus.Missed -> "Missed"; DoseStatus.Upcoming -> "Upcoming" },
                style = MaterialTheme.typography.labelLarge, color = if (status == DoseStatus.Missed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (status == DoseStatus.Taken || status == DoseStatus.Skipped) OutlinedButton(onClick = { store.markDose(d, null) }) { Text("Undo") }
            else {
                OutlinedButton(onClick = { store.markDose(d, DoseMark.Taken) }) { Text("Taken") }
                OutlinedButton(onClick = { store.markDose(d, DoseMark.Skipped) }) { Text("Skip") }
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add a medicine" else "Edit medicine") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, singleLine = true, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(dose, { dose = it }, singleLine = true, label = { Text("Dose (optional), for example 1 tablet") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(times, { times = it }, singleLine = true, isError = parsed == null, label = { Text("Times, HH:MM separated by commas") }, modifier = Modifier.fillMaxWidth())
                Text("Days (none selected means every day)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items((1..7).toList()) { n ->
                        FilterChip(n in days, { days = if (n in days) days - n else days + n }, { Text(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")[n - 1]) })
                    }
                }
            }
        },
        confirmButton = { TextButton(enabled = name.isNotBlank() && parsed != null, onClick = { onSave(Medicine(existing?.id ?: "", name.trim(), dose.trim(), parsed!!, if (days.size == 7) emptySet() else days)) }) { Text("Save") } },
        dismissButton = {
            Row {
                onDelete?.let { TextButton(onClick = it) { Text("Delete", color = MaterialTheme.colorScheme.error) } }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        },
    )
}
