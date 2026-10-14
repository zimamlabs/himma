package dev.himma.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.himma.core.formatKg
import dev.himma.core.movingAverage
import dev.himma.core.stepProgress
import dev.himma.core.weightTrend

@Composable
internal fun StepsTab(store: HimmaStore) {
    val goal = store.settings.stepGoal
    val support = store.platform.stepSupport
    LazyColumn(Modifier.fillMaxSize(), contentPadding = tabPadding, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Ring(stepProgress(store.stepsToday, goal), Modifier.size(220.dp)) {
                Text("${store.stepsToday}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text("of $goal steps", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            when (support) {
                StepSupport.Ready -> Text("Counting with your phone's step sensor while Himma is open or recently used. Steps taken on a day you never open Himma cannot be split by day, so they are not counted.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                StepSupport.NeedsPermission -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Himma needs permission to read the step sensor. It never leaves your phone.", style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { store.platform.allowStepCounting() }) { Text("Allow step counting") }
                }
                StepSupport.NoSensor -> Text("This phone has no step sensor, so steps cannot be counted.", style = MaterialTheme.typography.bodyMedium)
                StepSupport.Unavailable -> Text("Step counting is only available on the phone.", style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            Panel {
                Text("Last 7 days", style = MaterialTheme.typography.titleSmall)
                BarChart(store.week.map { it.steps.toFloat() }, store.week.map { it.date }, goal.toFloat())
            }
        }
    }
}

@Composable
internal fun WeightTab(store: HimmaStore) {
    var text by remember { mutableStateOf("") }
    val kg = text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it in 20.0..500.0 }
    val trend = weightTrend(store.weights, store.today, 28)
    val last30 = store.weights.filter { it.date >= kotlinx.datetime.LocalDate.fromEpochDays(store.today.toEpochDays() - 90) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = tabPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Panel {
                Text(trend.latestKg?.let { formatKg(it) } ?: "No weight logged", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                trend.changeKg?.let { Text("${if (it > 0) "+" else ""}${formatKg(it)} over 4 weeks", style = MaterialTheme.typography.bodyMedium) }
                trend.kgPerWeek?.let { Text("Trend ${if (it > 0) "+" else ""}${kotlin.math.round(it * 100) / 100} kg a week", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(text, { text = it }, Modifier.weight(1f), singleLine = true, isError = text.isNotBlank() && kg == null, label = { Text("Today's weight (kg)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                Button(enabled = kg != null, onClick = { store.saveWeight(kg!!); text = "" }) { Text("Save") }
            }
        }
        if (last30.size >= 2) item {
            Panel {
                Text("Last 90 days", style = MaterialTheme.typography.titleSmall)
                LineChart(last30.map { it.kg.toFloat() }, smooth = movingAverage(last30, 7).map { it.kg.toFloat() })
            }
        }
        items(store.weights.reversed().take(20), key = { it.date.toString() }) { e ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(prettyDate(e.date), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Text(formatKg(e.kg), style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = { store.saveWeight(0.0, e.date) }) { Icon(Icons.Rounded.Close, "Remove") }
            }
        }
    }
}
