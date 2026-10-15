package dev.himma.app

import dev.himma.app.res.of_fcd7
import dev.himma.app.res.kcal
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material3.FilledTonalButton
import dev.himma.core.HomeCards
import dev.himma.app.res.menu_open
import dev.himma.app.res.hide_this_card
import dev.himma.app.res.plus_glass
import dev.himma.app.res.steps_not_counting
import dev.himma.app.res.add_a_medicine
import dev.himma.app.res.no_medicines_yet





import dev.himma.app.res.n_of_m_taken

import dev.himma.app.res.percent_of_goals


import dev.himma.app.res.calories
import dev.himma.app.res.medicine
import dev.himma.app.res.steps
import dev.himma.app.res.water

import dev.himma.app.res.Res
import dev.himma.app.res.all_of_today_s_goals
import dev.himma.app.res.no_doses_today
import dev.himma.app.res.no_more_doses_today
import dev.himma.app.res.of
import dev.himma.app.res.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.himma.core.DoseMark
import dev.himma.core.Routine
import dev.himma.core.dayScore
import dev.himma.core.dayTotals
import dev.himma.core.stepProgress
import dev.himma.core.waterProgress

/** Where a Home card can send you. */
internal class Nav(
    val go: (Tab, TrackPage?) -> Unit,
    val logMeal: () -> Unit,
    val startWorkout: (Routine?) -> Unit,
)

/** The icon in a soft tile, as the small mark of a card. */
@Composable
private fun IconTile(icon: ImageVector) {
    Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
        Icon(icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

/**
 * One single-purpose Home card: what it is, one number, one line under it and a thin progress bar. A tap opens its
 * section; a long press offers Open and Hide this card.
 */
@Composable
private fun HomeCard(
    store: HimmaStore, id: String, icon: ImageVector, title: String, value: String, caption: String, progress: Float?,
    onOpen: () -> Unit, action: @Composable (() -> Unit)? = null,
) {
    val actions = listOf(
        MenuAction(s(Res.string.menu_open), Icons.Rounded.OpenInNew, onClick = onOpen),
        MenuAction(s(Res.string.hide_this_card), Icons.Rounded.VisibilityOff) { store.saveSettings(store.settings.copy(hiddenHomeCards = store.settings.hiddenHomeCards + id)) },
    )
    ContextMenuBox(actions, onOpen, Modifier.fillMaxWidth()) {
        Panel(diamond = false) {
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconTile(icon)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(caption, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                action?.invoke()
            }
            if (progress != null) LinearProgressIndicator(
                progress = { progress }, Modifier.fillMaxWidth().padding(top = 8.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh, color = MaterialTheme.colorScheme.primary, drawStopIndicator = {},
            )
        }
    }
}

/** Home: one hero with the day's progress, then at most four single-purpose cards (water, medicine, meals, steps). */
@Composable
internal fun HomeTab(store: HimmaStore, nav: Nav) {
    val s = store.settings
    val totals = dayTotals(store.todayMeals)
    val doses = store.todayDoses
    val taken = doses.count { store.doseMarks[it.key] == DoseMark.Taken }
    val next = doses.firstOrNull { it.at > store.now }
    val stepsReady = store.platform.stepSupport == StepSupport.Ready || store.stepsToday > 0
    val waterP = waterProgress(store.waterMl, s.waterGoalMl)
    val kcalP = (totals.kcal.toFloat() / s.calorieGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
    val stepP = stepProgress(store.stepsToday, s.stepGoal)
    val score = dayScore(listOf(waterP, if (store.todayMeals.isEmpty()) 0f else kcalP, if (stepsReady) stepP else null, if (doses.isEmpty()) null else taken.toFloat() / doses.size))
    LazyColumn(Modifier.fillMaxSize(), contentPadding = tabPadding(), verticalArrangement = Arrangement.spacedBy(rowGap)) {
        if (store.dataProblems.isNotEmpty()) item { DataProblemsCard(store.dataProblems, store::dismissDataProblems) }
        item {
            Glow(Modifier.clip(RoundedCornerShape(28.dp))) { Panel {
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(tr(dev.himma.core.greeting(store.now.hour)), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                        Text(prettyDate(store.today), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (score >= 1f) s(Res.string.all_of_today_s_goals) else s(Res.string.percent_of_goals, num("${(score * 100).toInt()}%")), style = MaterialTheme.typography.bodyMedium)
                    }
                    Ring(score, Modifier.size(104.dp)) { Text(num("${(score * 100).toInt()}%"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                }
            } }
        }
        HomeCards.visible(s.homeCardOrder, s.hiddenHomeCards).forEach { id ->
            item(key = "card-$id") {
                when (id) {
                    "water" -> HomeCard(store, id, Icons.Rounded.WaterDrop, s(Res.string.water), waterText(store.waterMl), s(Res.string.of_fcd7, waterText(s.waterGoalMl)), waterP, { nav.go(Tab.Track, TrackPage.Water) }) {
                        FilledTonalButton(onClick = { store.addWater(250) }) { Text(s(Res.string.plus_glass)) }
                    }
                    "medicine" -> HomeCard(
                        store, id, Icons.Rounded.Medication, s(Res.string.medicine),
                        when {
                            store.medicines.isEmpty() -> s(Res.string.no_medicines_yet)
                            doses.isEmpty() -> s(Res.string.no_doses_today)
                            next != null -> num(next.time) + "  " + next.medicine.name
                            else -> s(Res.string.no_more_doses_today)
                        },
                        if (doses.isEmpty()) s(Res.string.add_a_medicine) else s(Res.string.n_of_m_taken, taken, doses.size),
                        if (doses.isEmpty()) null else taken.toFloat() / doses.size, { nav.go(Tab.Medicine, null) },
                    )
                    "meals" -> HomeCard(store, id, Icons.Rounded.Restaurant, s(Res.string.calories), s(Res.string.kcal, totals.kcal), s(Res.string.of_fcd7, s(Res.string.kcal, s.calorieGoal)), kcalP, { nav.go(Tab.Meals, null) })
                    "steps" -> HomeCard(store, id, Icons.Rounded.DirectionsWalk, s(Res.string.steps), if (stepsReady) num(store.stepsToday) else "-", if (stepsReady) s(Res.string.of_fcd7, num(s.stepGoal)) else s(Res.string.steps_not_counting), if (stepsReady) stepP else null, { nav.go(Tab.Track, TrackPage.Steps) })
                }
            }
        }
    }
}
