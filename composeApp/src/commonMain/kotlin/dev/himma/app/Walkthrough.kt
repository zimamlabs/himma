package dev.himma.app

import dev.himma.app.res.dark
import dev.himma.app.res.done
import dev.himma.app.res.light
import dev.himma.app.res.next
import dev.himma.app.res.reminders
import dev.himma.app.res.skip
import dev.himma.app.res.system

import dev.himma.app.res.Res
import dev.himma.app.res.add_a_medicine_with_its
import dev.himma.app.res.allow_all_files_access
import dev.himma.app.res.allow_notifications
import dev.himma.app.res.android_asks_once_to_show
import dev.himma.app.res.daily_calorie_goal
import dev.himma.app.res.daily_water_goal_ml
import dev.himma.app.res.everything_stays_on_your_phone
import dev.himma.app.res.himma_keeps_plain_files_you
import dev.himma.app.res.light_dark_or_follow_the
import dev.himma.app.res.meals_and_calories_water_fasting
import dev.himma.app.res.pick_your_look
import dev.himma.app.res.ready_when_you_are
import dev.himma.app.res.set_your_daily_goals_you
import dev.himma.app.res.they_are_in_the_app
import dev.himma.app.res.they_are_in_together_with
import dev.himma.app.res.welcome_to_himma
import dev.himma.app.res.where_your_data_lives

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private const val PAGES = 5

/** The first-install tour: what Himma does, where the data lives, reminders, and your goals. Skip, Next, Done. */
@Composable
fun Walkthrough(store: HimmaStore, onDone: () -> Unit) {
    val pager = rememberPagerState { PAGES }
    val scope = rememberCoroutineScope()
    var theme by remember { mutableStateOf(store.settings.themeMode) }
    var kcal by remember { mutableStateOf(store.settings.calorieGoal.toString()) }
    var water by remember { mutableStateOf(store.settings.waterGoalMl.toString()) }
    val kcalValue = kcal.trim().toIntOrNull()?.takeIf { it in 1..20000 }
    val waterValue = water.trim().toIntOrNull()?.takeIf { it in 1..20000 }
    val last = pager.currentPage == PAGES - 1
    fun finish() {
        if (kcalValue != null && waterValue != null) store.saveSettings(store.settings.copy(calorieGoal = kcalValue, waterGoalMl = waterValue, themeMode = theme))
        onDone()
    }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { finish() }, enabled = !last) { Text(if (last) "" else s(Res.string.skip)) }
        }
        HorizontalPager(pager, Modifier.weight(1f)) { page ->
            Column(Modifier.fillMaxSize().padding(horizontal = 28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
                when (page) {
                    0 -> Page(s(Res.string.welcome_to_himma), s(Res.string.meals_and_calories_water_fasting), s(Res.string.everything_stays_on_your_phone))
                    1 -> {
                        Page(s(Res.string.where_your_data_lives), s(Res.string.himma_keeps_plain_files_you),
                            if (store.sharedFolder) s(Res.string.they_are_in_together_with, store.folderPath) else s(Res.string.they_are_in_the_app))
                        if (store.platform.canAskAllFiles && !store.sharedFolder) {
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { store.platform.allowAllFiles() }) { Text(s(Res.string.allow_all_files_access)) }
                        }
                    }
                    2 -> {
                        Page(s(Res.string.reminders), s(Res.string.add_a_medicine_with_its), s(Res.string.android_asks_once_to_show))
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = { store.platform.allowNotifications() }) { Text(s(Res.string.allow_notifications)) }
                    }
                    3 -> {
                        Page(s(Res.string.pick_your_look), s(Res.string.light_dark_or_follow_the), "")
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("system" to s(Res.string.system), "light" to s(Res.string.light), "dark" to s(Res.string.dark)).forEach { (k, label) ->
                                if (theme == k) Button(onClick = { theme = k; store.saveSettings(store.settings.copy(themeMode = k)) }) { Text(label) } else OutlinedButton(onClick = { theme = k; store.saveSettings(store.settings.copy(themeMode = k)) }) { Text(label) }
                            }
                        }
                    }
                    else -> {
                        Page(s(Res.string.ready_when_you_are), s(Res.string.set_your_daily_goals_you), "")
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(kcal, { kcal = it }, singleLine = true, isError = kcalValue == null, label = { Text(s(Res.string.daily_calorie_goal)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(water, { water = it }, singleLine = true, isError = waterValue == null, label = { Text(s(Res.string.daily_water_goal_ml)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(PAGES) { i ->
                    Box(Modifier.size(8.dp).clip(CircleShape).background(if (i == pager.currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh))
                }
            }
            Button(enabled = !last || (kcalValue != null && waterValue != null), onClick = { if (last) finish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }) { Text(if (last) s(Res.string.done) else s(Res.string.next)) }
        }
    }
}

@Composable
private fun Page(title: String, body: String, more: String) {
    Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(12.dp))
    Text(body, style = MaterialTheme.typography.bodyLarge)
    if (more.isNotEmpty()) { Spacer(Modifier.height(10.dp)); Text(more, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}
