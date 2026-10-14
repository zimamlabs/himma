package dev.himma.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.himma.core.ReleaseNote

/** Shown once after an update, with a Close button. */
@Composable
fun WhatsNewDialog(notes: List<ReleaseNote>, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("What's new") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                notes.forEach { n ->
                    Text("Version ${n.version}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    n.bullets.forEach { Text("•  $it", style = MaterialTheme.typography.bodyMedium) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Close") } },
    )
}
