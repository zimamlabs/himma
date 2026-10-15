package dev.himma.app

import dev.himma.app.res.remove

import dev.himma.app.res.Res
import dev.himma.app.res.allow_step_counting
import dev.himma.app.res.counting_with_your_phone_s
import dev.himma.app.res.himma_needs_permission_to_read
import dev.himma.app.res.last_days
import dev.himma.app.res.last_days_a369
import dev.himma.app.res.no_weight_logged
import dev.himma.app.res.of_steps
import dev.himma.app.res.over_weeks
import dev.himma.app.res.save
import dev.himma.app.res.step_counting_is_only_available
import dev.himma.app.res.this_phone_has_no_step
import dev.himma.app.res.today_s_weight_kg
import dev.himma.app.res.trend_kg_a_week

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
import dev.himma.app.res.kg_label
import dev.himma.app.res.lb_label
import dev.himma.app.res.today_s_weight_unit
import dev.himma.app.res.trend_unit_a_week
import dev.himma.core.WeightUnit
import dev.himma.core.numberText
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
                Text(s(Res.string.of_steps, goal), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            when (support) {
                StepSupport.Ready -> Text(s(Res.string.counting_with_your_phone_s), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                StepSupport.NeedsPermission -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s(Res.string.himma_needs_permission_to_read), style = MaterialTheme.typography.bodyMedium)
                    Button(onClick = { store.platform.allowStepCounting() }) { Text(s(Res.string.allow_step_counting)) }
                }
                StepSupport.NoSensor -> Text(s(Res.string.this_phone_has_no_step), style = MaterialTheme.typography.bodyMedium)
                StepSupport.Unavailable -> Text(s(Res.string.step_counting_is_only_available), style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            Panel {
                Text(s(Res.string.last_days), style = MaterialTheme.typography.titleSmall)
                BarChart(store.week.map { it.steps.toFloat() }, store.week.map { it.date }, goal.toFloat())
            }
        }
    }
}

@Composable
internal fun WeightTab(store: HimmaStore) {
    var text by remember { mutableStateOf("") }
    val unit = WeightUnit.fromCode(store.settings.weightUnit)
    val kg = text.trim().replace(',', '.').toDoubleOrNull()?.let { unit.toKg(it) }?.takeIf { it in 20.0..500.0 }
    val trend = weightTrend(store.weights, store.today, 28)
    val last30 = store.weights.filter { it.date >= kotlinx.datetime.LocalDate.fromEpochDays(store.today.toEpochDays() - 90) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = tabPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Panel {
                Text(trend.latestKg?.let { weightText(it, unit) } ?: s(Res.string.no_weight_logged), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                trend.changeKg?.let { Text(s(Res.string.over_weeks, if (it > 0) "+" else "", weightText(it, unit)), style = MaterialTheme.typography.bodyMedium) }
                trend.kgPerWeek?.let { Text(s(Res.string.trend_unit_a_week, if (it > 0) "+" else "", kotlin.math.round(unit.fromKg(it) * 100) / 100, s(if (unit == WeightUnit.Lb) Res.string.lb_label else Res.string.kg_label)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(text, { text = it }, Modifier.weight(1f), singleLine = true, isError = text.isNotBlank() && kg == null, label = { Text(s(Res.string.today_s_weight_unit, s(if (unit == WeightUnit.Lb) Res.string.lb_label else Res.string.kg_label))) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                Button(enabled = kg != null, onClick = { store.saveWeight(kg!!); text = "" }) { Text(s(Res.string.save)) }
            }
        }
        if (last30.size >= 2) item {
            Panel {
                Text(s(Res.string.last_days_a369), style = MaterialTheme.typography.titleSmall)
                LineChart(last30.map { unit.fromKg(it.kg).toFloat() }, smooth = movingAverage(last30, 7).map { unit.fromKg(it.kg).toFloat() })
            }
        }
        items(store.weights.reversed().take(20), key = { it.date.toString() }) { e ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(prettyDate(e.date), Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Text(weightText(e.kg, unit), style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = { store.saveWeight(0.0, e.date) }) { Icon(Icons.Rounded.Close, s(Res.string.remove)) }
            }
        }
    }
}
