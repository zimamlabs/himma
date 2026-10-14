package dev.himma.app

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

private const val PAGES = 4

/** The first-install tour: what Himma does, where the data lives, reminders, and your goals. Skip, Next, Done. */
@Composable
fun Walkthrough(store: HimmaStore, onDone: () -> Unit) {
    val pager = rememberPagerState { PAGES }
    val scope = rememberCoroutineScope()
    var kcal by remember { mutableStateOf(store.settings.calorieGoal.toString()) }
    var water by remember { mutableStateOf(store.settings.waterGoalMl.toString()) }
    val kcalValue = kcal.trim().toIntOrNull()?.takeIf { it in 1..20000 }
    val waterValue = water.trim().toIntOrNull()?.takeIf { it in 1..20000 }
    val last = pager.currentPage == PAGES - 1
    fun finish() {
        if (kcalValue != null && waterValue != null) store.saveSettings(store.settings.copy(calorieGoal = kcalValue, waterGoalMl = waterValue))
        onDone()
    }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { finish() }, enabled = !last) { Text(if (last) "" else "Skip") }
        }
        HorizontalPager(pager, Modifier.weight(1f)) { page ->
            Column(Modifier.fillMaxSize().padding(horizontal = 28.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
                when (page) {
                    0 -> Page("Welcome to Himma", "Meals and calories, water, a fasting window and medicine reminders, in one calm app.", "Everything stays on your phone. There is no account and nothing is sent anywhere.")
                    1 -> {
                        Page("Where your data lives", "Himma keeps plain files you can open in any editor.",
                            if (store.sharedFolder) "They are in ${store.folderPath}, together with your other Zimam apps." else "They are in the app's private folder now. Allow All files access and Himma moves a verified copy to Zimam/Himma on your phone, so your files survive a reinstall and sit next to the other Zimam apps. The old copy is kept until you remove it.")
                        if (store.platform.canAskAllFiles && !store.sharedFolder) {
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { store.platform.allowAllFiles() }) { Text("Allow All files access") }
                        }
                    }
                    2 -> {
                        Page("Reminders", "Add a medicine with its times and days and Himma tells you when a dose is due. Mark it Taken or Skip right from the notification.", "Android asks once to show notifications. If exact alarms are not allowed, a reminder can come a few minutes late.")
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = { store.platform.allowNotifications() }) { Text("Allow notifications") }
                    }
                    else -> {
                        Page("Ready when you are", "Set your daily goals. You can change them later in Settings.", "")
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(kcal, { kcal = it }, singleLine = true, isError = kcalValue == null, label = { Text("Daily calorie goal") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(water, { water = it }, singleLine = true, isError = waterValue == null, label = { Text("Daily water goal (ml)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
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
            Button(enabled = !last || (kcalValue != null && waterValue != null), onClick = { if (last) finish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }) { Text(if (last) "Done" else "Next") }
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
