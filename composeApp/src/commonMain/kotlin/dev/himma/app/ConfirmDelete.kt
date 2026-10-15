package dev.himma.app

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.himma.app.res.Res
import dev.himma.app.res.delete
import dev.himma.app.res.keep_it

/** Asks before something that cannot be undone is removed; [text] says what is lost. */
@Composable
fun ConfirmDelete(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(s(Res.string.delete), color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(s(Res.string.keep_it)) } },
    )
}
