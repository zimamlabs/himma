package dev.himma.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.himma.app.res.Res
import dev.himma.app.res.allow_exact_alarms
import dev.himma.app.res.exact_alarms_off_body
import dev.himma.app.res.notification_settings
import dev.himma.app.res.notifications_off_body
import dev.himma.app.res.reminders_may_not_work
import dev.himma.core.ReminderProblem

/** One card for everything on the phone that can stop a reminder: what is wrong, and a button to the right settings screen. */
@Composable
fun RemindersCard(problems: List<ReminderProblem>, onNotificationSettings: () -> Unit, onExactAlarmSettings: () -> Unit) {
    Panel {
        Text(s(Res.string.reminders_may_not_work), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
        problems.forEach { p ->
            when (p) {
                ReminderProblem.NotificationsOff -> Problem(s(Res.string.notifications_off_body), s(Res.string.notification_settings), onNotificationSettings)
                ReminderProblem.ExactAlarmsOff -> Problem(s(Res.string.exact_alarms_off_body), s(Res.string.allow_exact_alarms), onExactAlarmSettings)
            }
        }
    }
}

@Composable
private fun Problem(text: String, button: String, onClick: () -> Unit) {
    Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedButton(onClick = onClick) { Text(button) } }
}
